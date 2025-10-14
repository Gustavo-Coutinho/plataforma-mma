package br.gov.mma.facial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * DTO para requisição de login biométrico facial
 */
public class BiometricLoginRequest {

    /**
     * Email ou matrícula do usuário (opcional para validação)
     */
    private String emailOrMatricula;

    /**
     * Senha do usuário (opcional para validação)
     */
    private String password;

    /**
     * Imagem facial base64 (obrigatória para autenticação, opcional para reset com userId pré-verificado)
     */
    private String faceImageBase64;

    @Size(max = 50, message = "Session ID deve ter no máximo 50 caracteres")
    private String sessionId;

    /**
     * Metadados adicionais da captura (opcional)
     */
    private Map<String, Object> metadata;

    /**
     * Flag para indicar se deve verificar liveness
     */
    private Boolean enableLivenessCheck = true;

    // Constructors
    public BiometricLoginRequest() {}

    public BiometricLoginRequest(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }

    // Getters e Setters
    public String getEmailOrMatricula() { return emailOrMatricula; }
    public void setEmailOrMatricula(String emailOrMatricula) { this.emailOrMatricula = emailOrMatricula; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFaceImageBase64() { return faceImageBase64; }
    public void setFaceImageBase64(String faceImageBase64) { this.faceImageBase64 = faceImageBase64; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Boolean getEnableLivenessCheck() { return enableLivenessCheck; }
    public void setEnableLivenessCheck(Boolean enableLivenessCheck) { this.enableLivenessCheck = enableLivenessCheck; }

    @Override
    public String toString() {
        return "BiometricLoginRequest{" +
                "sessionId='" + sessionId + "'" +
                ", enableLivenessCheck=" + enableLivenessCheck +
                '}';
    }
}