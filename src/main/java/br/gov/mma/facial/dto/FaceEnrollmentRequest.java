package br.gov.mma.facial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para requisição de cadastro biométrico facial
 */
public class FaceEnrollmentRequest {

    @NotEmpty(message = "Pelo menos uma imagem facial é obrigatória")
    @Size(min = 3, max = 10, message = "Entre 3 e 10 imagens são necessárias para cadastro")
    private List<String> faceImagesBase64;

    @NotBlank(message = "ID do usuário é obrigatório")
    private String userId;

    /**
     * Metadados da captura
     */
    private String metadata;

    /**
     * Substitui template existente se true
     */
    private Boolean replaceExisting = false;

    // Constructors
    public FaceEnrollmentRequest() {}

    public FaceEnrollmentRequest(List<String> faceImagesBase64, String userId) {
        this.faceImagesBase64 = faceImagesBase64;
        this.userId = userId;
    }

    // Getters e Setters
    public List<String> getFaceImagesBase64() { return faceImagesBase64; }
    public void setFaceImagesBase64(List<String> faceImagesBase64) { this.faceImagesBase64 = faceImagesBase64; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Boolean getReplaceExisting() { return replaceExisting; }
    public void setReplaceExisting(Boolean replaceExisting) { this.replaceExisting = replaceExisting; }

    @Override
    public String toString() {
        return "FaceEnrollmentRequest{" +
                "userId='" + userId + "'" +
                ", imagesCount=" + (faceImagesBase64 != null ? faceImagesBase64.size() : 0) +
                ", replaceExisting=" + replaceExisting +
                '}';
    }
}