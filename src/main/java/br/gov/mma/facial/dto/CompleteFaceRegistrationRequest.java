package br.gov.mma.facial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * DTO for completing registration with facial enrollment
 */
public class CompleteFaceRegistrationRequest {
    
    @NotBlank(message = "Session token is required")
    private String sessionToken;
    
    @NotEmpty(message = "At least one face image is required")
    @Size(min = 1, max = 5, message = "Entre 1 e 5 imagens faciais são necessárias")
    private List<String> faceImagesBase64;
    
    private Map<String, Object> metadata;

    // Constructors
    public CompleteFaceRegistrationRequest() {}

    public CompleteFaceRegistrationRequest(String sessionToken, List<String> faceImagesBase64, Map<String, Object> metadata) {
        this.sessionToken = sessionToken;
        this.faceImagesBase64 = faceImagesBase64;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public List<String> getFaceImagesBase64() { return faceImagesBase64; }
    public void setFaceImagesBase64(List<String> faceImagesBase64) { this.faceImagesBase64 = faceImagesBase64; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
