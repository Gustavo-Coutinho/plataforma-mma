package br.gov.mma.facial.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade para armazenar templates biométricos faciais
 * Armazena dados processados pelo OpenCV, não imagens cruas
 */
@Entity
@Table(name = "user_face_templates")
@EntityListeners(AuditingEntityListener.class)
public class FaceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Template biométrico criptografado
     * Contém os dados processados pelo LBPH do OpenCV
     */
    @Lob
    @Column(name = "template_bytes", nullable = false)
    private byte[] templateBytes;

    /**
     * Versão do algoritmo usado para gerar o template
     * Permite migração para algoritmos mais avançados no futuro
     */
    @Column(name = "algorithm_version", nullable = false)
    private String algorithmVersion = "LBPH-v1.0";

    /**
     * Metadados do template (JSON)
     * Pode conter parâmetros do algoritmo, qualidade da captura, etc.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Indica se este é o template principal (mais recente/melhor qualidade)
     */
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    /**
     * Score de qualidade do template (0.0 a 1.0)
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Constructors
    public FaceTemplate() {}

    public FaceTemplate(User user, byte[] templateBytes, String algorithmVersion) {
        this.user = user;
        this.templateBytes = templateBytes;
        this.algorithmVersion = algorithmVersion;
    }

    /**
     * Verifica se o template expirou
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Define como template principal, removendo flag de outros templates do mesmo usuário
     */
    public void setAsPrimary() {
        this.isPrimary = true;
        // Em uma implementação real, seria necessário atualizar outros templates do usuário
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public byte[] getTemplateBytes() { return templateBytes; }
    public void setTemplateBytes(byte[] templateBytes) { this.templateBytes = templateBytes; }

    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaceTemplate that = (FaceTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FaceTemplate{" +
                "id=" + id +
                ", algorithmVersion='" + algorithmVersion + "'" +
                ", isPrimary=" + isPrimary +
                ", qualityScore=" + qualityScore +
                ", createdAt=" + createdAt +
                '}';
    }
}