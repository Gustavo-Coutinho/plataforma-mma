package br.gov.mma.facial.security;

import br.gov.mma.facial.config.BiometricProperties;
import br.gov.mma.facial.entity.FaceTemplate;
import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.repository.FaceTemplateRepository;
import br.gov.mma.facial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Provider responsável por validar credenciais biométricas faciais.
 */
@Component
public class BiometricAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(BiometricAuthenticationProvider.class);

    private final FaceTemplateRepository faceTemplateRepository;
    private final UserRepository userRepository;
    private final BiometricProperties biometricProperties;

    public BiometricAuthenticationProvider(FaceTemplateRepository faceTemplateRepository,
                                           UserRepository userRepository,
                                           BiometricProperties biometricProperties) {
        this.faceTemplateRepository = faceTemplateRepository;
        this.userRepository = userRepository;
        this.biometricProperties = biometricProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof BiometricAuthenticationToken biometricToken)) {
            return null;
        }

        String username = biometricToken.getName();
        byte[] probeTemplate = biometricToken.getProbeTemplate();

        if (!StringUtils.hasText(username) || probeTemplate == null) {
            throw new BadCredentialsException("Credenciais biométricas inválidas");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        FaceTemplate referenceTemplate = faceTemplateRepository.findPrimaryTemplateByUser(user)
            .orElseThrow(() -> new BadCredentialsException("Template biométrico não encontrado para o usuário"));

        // Compare templates using the same algorithm as BiometricService
        double score = compareTemplates(probeTemplate, referenceTemplate.getTemplateBytes());
        double threshold = biometricProperties.getThreshold();
        
        logger.debug("Comparação biométrica - Score: {}, Threshold: {}", score, threshold);
        
        if (score > threshold) {
            logger.warn("Falha na validação biométrica para usuário: {} - Score: {} > Threshold: {}", 
                username, score, threshold);
            throw new BadCredentialsException("Falha na validação biométrica");
        }

        logger.info("Validação biométrica bem-sucedida para usuário: {} - Score: {}", username, score);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        BiometricAuthenticationToken authenticatedToken =
            new BiometricAuthenticationToken(userDetails, userDetails.getAuthorities(), probeTemplate);
        authenticatedToken.setDetails(authentication.getDetails());
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BiometricAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Compara dois templates biométricos usando a mesma lógica do BiometricService
     */
    private double compareTemplates(byte[] template1, byte[] template2) {
        try {
            if (template1 == null || template2 == null) {
                return Double.MAX_VALUE;
            }
            
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
}
