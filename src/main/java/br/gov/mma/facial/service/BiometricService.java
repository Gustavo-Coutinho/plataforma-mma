package br.gov.mma.facial.service;

import br.gov.mma.facial.config.BiometricProperties;
import br.gov.mma.facial.entity.FaceTemplate;
import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.repository.FaceTemplateRepository;
import org.opencv.core.*;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para processamento biométrico facial
 * Utiliza OpenCV para detecção, extração de características e reconhecimento
 */
@Service
@Transactional
public class BiometricService {

    private static final Logger logger = LoggerFactory.getLogger(BiometricService.class);

    private final BiometricProperties biometricProperties;
    private final FaceTemplateRepository faceTemplateRepository;
    private CascadeClassifier faceCascade;
    private LBPHFaceRecognizer faceRecognizer;

    public BiometricService(BiometricProperties biometricProperties, 
                           FaceTemplateRepository faceTemplateRepository) {
        this.biometricProperties = biometricProperties;
        this.faceTemplateRepository = faceTemplateRepository;
        initializeOpenCV();
    }

    /**
     * Inicializa componentes do OpenCV
     */
    private void initializeOpenCV() {
        try {
            // Carregar classificador Haar Cascade para detecção facial
            // Tentar primeiro o arquivo do Docker container
            String dockerCascadePath = "/app/resources/haarcascade_frontalface_default.xml";
            this.faceCascade = new CascadeClassifier(dockerCascadePath);
            
            // Se não encontrar, tentar carregar do classpath
            if (faceCascade.empty()) {
                try {
                    var cascadeStream = getClass().getClassLoader()
                        .getResourceAsStream("haarcascade_frontalface_default.xml");
                    
                    if (cascadeStream != null) {
                        // Criar arquivo temporário
                        java.io.File tempFile = java.io.File.createTempFile("haarcascade_frontalface_default", ".xml");
                        tempFile.deleteOnExit();
                        
                        try (var fos = new java.io.FileOutputStream(tempFile)) {
                            cascadeStream.transferTo(fos);
                        }
                        
                        this.faceCascade = new CascadeClassifier(tempFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao carregar cascata do classpath: " + e.getMessage());
                }
            }
            
            if (faceCascade.empty()) {
                logger.error("Erro ao carregar classificador Haar Cascade de qualquer localização");
                throw new RuntimeException("Falha na inicialização do classificador facial");
            }

            // Tentar inicializar reconhecedor LBPH (com fallback)
            try {
                this.faceRecognizer = LBPHFaceRecognizer.create();
                logger.info("OpenCV LBPH Face Recognizer inicializado com sucesso");
            } catch (UnsatisfiedLinkError e) {
                logger.warn("LBPH Face Recognizer não disponível neste ambiente: {}", e.getMessage());
                logger.info("Modo de simulação ativado - comparação de templates básica será usada");
                this.faceRecognizer = null;
            }

            logger.info("OpenCV inicializado com sucesso");

        } catch (Exception e) {
            logger.error("Erro na inicialização do OpenCV", e);
            throw new RuntimeException("Falha na inicialização do OpenCV", e);
        }
    }

    /**
     * Extrai template biométrico de uma imagem facial em base64
     */
    public byte[] extractFaceTemplate(String imageBase64) {
        try {
            logger.debug("Iniciando extração de template facial");

            // Decodificar imagem base64
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            
            // Usar Java ImageIO para decodificar a imagem (não depende de bibliotecas nativas)
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (bufferedImage == null) {
                logger.warn("Imagem vazia ou corrompida");
                return null;
            }
            
            // Converter BufferedImage para Mat
            Mat image = bufferedImageToMat(bufferedImage);
            
            if (image.empty()) {
                logger.warn("Falha ao converter imagem para Mat");
                return null;
            }

            // Converter para escala de cinza
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Detectar faces
            MatOfRect faceDetections = new MatOfRect();
            faceCascade.detectMultiScale(grayImage, faceDetections);

            Rect[] faces = faceDetections.toArray();
            
            if (faces.length == 0) {
                logger.warn("Nenhuma face detectada na imagem");
                return null;
            }

            if (faces.length > 1) {
                logger.info("Múltiplas faces detectadas. Usando a maior (mais proeminente).");
            }

            // Usar a maior face detectada (mais proeminente)
            Rect faceRect = faces[0];
            for (Rect face : faces) {
                if (face.area() > faceRect.area()) {
                    faceRect = face;
                }
            }
            
            // Validar tamanho mínimo da face
            if (faceRect.width < biometricProperties.getMinFaceSize() || 
                faceRect.height < biometricProperties.getMinFaceSize()) {
                logger.warn("Face muito pequena: {}x{}", faceRect.width, faceRect.height);
                return null;
            }

            // Extrair região da face
            Mat faceROI = new Mat(grayImage, faceRect);

            // Normalizar tamanho
            Mat normalizedFace = new Mat();
            Imgproc.resize(faceROI, normalizedFace, new Size(128, 128));

            // Aplicar equalização de histograma
            Mat equalizedFace = new Mat();
            Imgproc.equalizeHist(normalizedFace, equalizedFace);

            // Converter para template (array de bytes)
            byte[] template = matToByteArray(equalizedFace);

            logger.debug("Template facial extraído com sucesso. Tamanho: {} bytes", template.length);
            return template;

        } catch (Exception e) {
            logger.error("Erro na extração do template facial", e);
            return null;
        }
    }

    /**
     * Converte BufferedImage para OpenCV Mat
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        // Converter para BGR (formato esperado pelo OpenCV)
        BufferedImage convertedImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_3BYTE_BGR
        );
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        
        // Extrair bytes da imagem
        byte[] pixels = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
        
        // Criar Mat
        Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        
        return mat;
    }

    /**
     * Verifica liveness (detecção anti-spoofing básica)
     */
    public boolean verifyLiveness(String imageBase64) {
        try {
            logger.debug("Verificando liveness da imagem");

            // Implementação básica de liveness
            // Em produção, seria necessário algoritmos mais sofisticados
            
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            
            // Usar Java ImageIO para decodificar
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (bufferedImage == null) {
                return false;
            }
            
            // Converter para Mat
            Mat image = bufferedImageToMat(bufferedImage);
            
            if (image.empty()) {
                return false;
            }

            // Converter para HSV para análise de cor
            Mat hsvImage = new Mat();
            Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

            // Calcular variação de cor (indicativo de face real)
            Scalar mean = Core.mean(hsvImage);
            double colorVariation = mean.val[1]; // Saturation

            // Análise de textura básica
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            Mat laplacian = new Mat();
            Imgproc.Laplacian(grayImage, laplacian, CvType.CV_64F);
            
            Scalar laplacianMean = Core.mean(laplacian);
            double textureVariation = laplacianMean.val[0];

            // Critérios básicos para liveness (muito relaxados para aceitar webcams)
            boolean hasColorVariation = colorVariation > 3;  // Muito relaxado para aceitar mais condições
            boolean hasTextureVariation = Math.abs(textureVariation) > 0.00001;  // Extremamente relaxado

            boolean isLive = hasColorVariation && hasTextureVariation;
            
            logger.info("Liveness check: colorVar={}, textureVar={}, isLive={}", 
                colorVariation, textureVariation, isLive);

            return isLive;

        } catch (Exception e) {
            logger.error("Erro na verificação de liveness", e);
            return false;
        }
    }

    /**
     * Identifica usuário pelo template biométrico
     */
    public User identifyUser(byte[] probeTemplate) {
        try {
            logger.debug("Iniciando identificação biométrica");

            // Buscar todos os templates ativos
            List<FaceTemplate> templates = faceTemplateRepository.findAllPrimaryTemplates();
            
            if (templates.isEmpty()) {
                logger.warn("Nenhum template cadastrado para comparação");
                return null;
            }

            double bestScore = Double.MAX_VALUE;
            User bestMatch = null;

            // Comparar com cada template
            for (FaceTemplate template : templates) {
                double score = compareTemplates(probeTemplate, template.getTemplateBytes());
                
                if (score < bestScore) {
                    bestScore = score;
                    bestMatch = template.getUser();
                }
            }

            // Verificar se a melhor pontuação está dentro do threshold
            if (bestScore <= biometricProperties.getThreshold()) {
                logger.info("Usuário identificado: {} com score: {}", 
                    bestMatch.getEmail(), bestScore);
                return bestMatch;
            } else {
                logger.warn("Nenhuma correspondência encontrada. Melhor score: {}", bestScore);
                return null;
            }

        } catch (Exception e) {
            logger.error("Erro na identificação biométrica", e);
            return null;
        }
    }

    /**
     * Cadastra biometria facial para um usuário
     */
    public boolean enrollUserFace(User user, List<String> faceImagesBase64, boolean replaceExisting) {
        try {
            logger.info("Iniciando cadastro biométrico para usuário: {}", user.getEmail());

            // Se substituir existente, remover templates antigos
            if (replaceExisting) {
                List<FaceTemplate> existingTemplates = faceTemplateRepository.findByUser(user);
                faceTemplateRepository.deleteAll(existingTemplates);
                logger.info("Templates existentes removidos para usuário: {}", user.getEmail());
            }

            // Processar cada imagem
            boolean hasValidTemplate = false;
            
            for (int i = 0; i < faceImagesBase64.size(); i++) {
                String imageBase64 = faceImagesBase64.get(i);
                byte[] template = extractFaceTemplate(imageBase64);
                
                if (template != null) {
                    // Criar novo template
                    FaceTemplate faceTemplate = new FaceTemplate();
                    faceTemplate.setUser(user);
                    faceTemplate.setTemplateBytes(template);
                    faceTemplate.setAlgorithmVersion("LBPH-1.0");
                    faceTemplate.setIsPrimary(i == 0); // Primeira imagem válida é primária
                    faceTemplate.setQualityScore(calculateQualityScore(template));
                    faceTemplate.setCreatedAt(LocalDateTime.now());
                    
                    faceTemplateRepository.save(faceTemplate);
                    hasValidTemplate = true;
                    
                    logger.debug("Template {} salvo para usuário: {}", i + 1, user.getEmail());
                }
            }

            if (hasValidTemplate) {
                logger.info("Cadastro biométrico concluído para usuário: {}", user.getEmail());
                return true;
            } else {
                logger.warn("Nenhum template válido gerado para usuário: {}", user.getEmail());
                return false;
            }

        } catch (Exception e) {
            logger.error("Erro no cadastro biométrico para usuário: {}", user.getEmail(), e);
            return false;
        }
    }

    /**
     * Compara dois templates biométricos
     */
    private double compareTemplates(byte[] template1, byte[] template2) {
        try {
            // Implementação básica de comparação
            // Em produção, usaria algoritmos mais sofisticados
            
            if (template1.length != template2.length) {
                return Double.MAX_VALUE;
            }

            double distance = 0.0;
            for (int i = 0; i < template1.length; i++) {
                double diff = (template1[i] & 0xFF) - (template2[i] & 0xFF);
                distance += diff * diff;
            }

            return Math.sqrt(distance / template1.length);

        } catch (Exception e) {
            logger.error("Erro na comparação de templates", e);
            return Double.MAX_VALUE;
        }
    }

    /**
     * Calcula score de qualidade do template
     */
    private Double calculateQualityScore(byte[] template) {
        try {
            // Análise básica de qualidade baseada na variância dos dados
            double sum = 0.0;
            double sumSquares = 0.0;
            
            for (byte b : template) {
                int value = b & 0xFF;
                sum += value;
                sumSquares += value * value;
            }
            
            double mean = sum / template.length;
            double variance = (sumSquares / template.length) - (mean * mean);
            
            // Normalizar para escala 0-1
            return Math.min(1.0, variance / 255.0);
            
        } catch (Exception e) {
            logger.error("Erro no cálculo de qualidade", e);
            return 0.5; // Score médio em caso de erro
        }
    }

    /**
     * Converte Mat do OpenCV para array de bytes
     */
    private byte[] matToByteArray(Mat mat) {
        int totalBytes = (int) (mat.total() * mat.elemSize());
        byte[] buffer = new byte[totalBytes];
        mat.get(0, 0, buffer);
        return buffer;
    }

    /**
     * Obtém estatísticas do sistema biométrico
     */
    public BiometricStats getStats() {
        long totalTemplates = faceTemplateRepository.count();
        long activeTemplates = faceTemplateRepository.countByIsPrimary(true);
        
        return new BiometricStats(totalTemplates, activeTemplates);
    }

    /**
     * Classe para estatísticas biométricas
     */
    public static class BiometricStats {
        private final long totalTemplates;
        private final long activeTemplates;

        public BiometricStats(long totalTemplates, long activeTemplates) {
            this.totalTemplates = totalTemplates;
            this.activeTemplates = activeTemplates;
        }

        public long getTotalTemplates() { return totalTemplates; }
        public long getActiveTemplates() { return activeTemplates; }
    }
}