package br.gov.mma.facial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Propriedades de configuração para autenticação biométrica
 */
@ConfigurationProperties(prefix = "app.biometric.face")
@Validated
public class BiometricProperties {

    /**
     * Limiar de similaridade para reconhecimento facial (0.0 a 1.0)
     */
    @DecimalMin(value = "0.0", message = "Threshold deve ser no mínimo 0.0")
    @DecimalMax(value = "90.0", message = "Threshold deve ser no máximo 90.0")
    private Double threshold = 0.75;

    /**
     * Habilita verificação de liveness
     */
    private Boolean enableLiveness = true;
    
    /**
     * Habilita autenticação biométrica
     */
    private Boolean enabled = true;

    /**
     * Configurações do algoritmo LBPH
     */
    private LbphConfig lbph = new LbphConfig();

    /**
     * Caminho para o arquivo de classificador Haar Cascade
     */
    @NotBlank(message = "Caminho do classificador é obrigatório")
    private String cascadeClassifierPath = "haarcascade_frontalface_default.xml";

    /**
     * Tamanho mínimo da face detectada em pixels
     */
    @Min(value = 50, message = "Tamanho mínimo da face deve ser pelo menos 50 pixels")
    private Integer minFaceSize = 128;

    /**
     * Número de frames necessários para enrollment
     */
    @Min(value = 1, message = "Pelo menos 1 frame é necessário para enrollment")
    private Integer enrollmentFramesRequired = 5;

    /**
     * Configurações específicas do algoritmo LBPH
     */
    public static class LbphConfig {
        private Integer radius = 2;
        private Integer neighbors = 16;
        private Integer gridX = 8;
        private Integer gridY = 8;

        public Integer getRadius() { return radius; }
        public void setRadius(Integer radius) { this.radius = radius; }

        public Integer getNeighbors() { return neighbors; }
        public void setNeighbors(Integer neighbors) { this.neighbors = neighbors; }

        public Integer getGridX() { return gridX; }
        public void setGridX(Integer gridX) { this.gridX = gridX; }

        public Integer getGridY() { return gridY; }
        public void setGridY(Integer gridY) { this.gridY = gridY; }
    }

    // Getters e Setters
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public Boolean getEnableLiveness() { return enableLiveness; }
    public void setEnableLiveness(Boolean enableLiveness) { this.enableLiveness = enableLiveness; }

    public LbphConfig getLbph() { return lbph; }
    public void setLbph(LbphConfig lbph) { this.lbph = lbph; }

    public String getCascadeClassifierPath() { return cascadeClassifierPath; }
    public void setCascadeClassifierPath(String cascadeClassifierPath) { this.cascadeClassifierPath = cascadeClassifierPath; }

    public Integer getMinFaceSize() { return minFaceSize; }
    public void setMinFaceSize(Integer minFaceSize) { this.minFaceSize = minFaceSize; }

    public Integer getEnrollmentFramesRequired() { return enrollmentFramesRequired; }
    public void setEnrollmentFramesRequired(Integer enrollmentFramesRequired) { this.enrollmentFramesRequired = enrollmentFramesRequired; }
    
    public Boolean isEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}