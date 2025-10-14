package br.gov.mma.facial.entity;

import br.gov.mma.facial.enums.RiscoClassificacao;
import br.gov.mma.facial.enums.StatusProibicao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade representando agrotóxicos monitorados pelo sistema
 */
@Entity
@Table(name = "agrotoxicos", indexes = {
    @Index(name = "idx_agrotoxico_status", columnList = "status_proibicao"),
    @Index(name = "idx_agrotoxico_risco", columnList = "risco_classificacao")
})
@EntityListeners(AuditingEntityListener.class)
public class Agrotoxico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do agrotóxico é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nome;

    @Size(max = 500, message = "Princípio ativo deve ter no máximo 500 caracteres")
    @Column(name = "principio_ativo", length = 500)
    private String principioAtivo;

    /**
     * Status de proibição do agrotóxico
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_proibicao", nullable = false)
    private StatusProibicao statusProibicao = StatusProibicao.NAO_PROIBIDO;

    /**
     * Classificação de risco ambiental
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risco_classificacao", nullable = false)
    private RiscoClassificacao riscoClassificacao = RiscoClassificacao.BAIXO;

    /**
     * Observações sobre o agrotóxico
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Agrotoxico() {}

    public Agrotoxico(String nome, String principioAtivo, StatusProibicao statusProibicao, RiscoClassificacao riscoClassificacao) {
        this.nome = nome;
        this.principioAtivo = principioAtivo;
        this.statusProibicao = statusProibicao;
        this.riscoClassificacao = riscoClassificacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPrincipioAtivo() { return principioAtivo; }
    public void setPrincipioAtivo(String principioAtivo) { this.principioAtivo = principioAtivo; }

    public StatusProibicao getStatusProibicao() { return statusProibicao; }
    public void setStatusProibicao(StatusProibicao statusProibicao) { this.statusProibicao = statusProibicao; }

    public RiscoClassificacao getRiscoClassificacao() { return riscoClassificacao; }
    public void setRiscoClassificacao(RiscoClassificacao riscoClassificacao) { this.riscoClassificacao = riscoClassificacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agrotoxico that = (Agrotoxico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Agrotoxico{" +
                "id=" + id +
                ", nome='" + nome + "'" +
                ", principioAtivo='" + principioAtivo + "'" +
                ", statusProibicao=" + statusProibicao +
                ", riscoClassificacao=" + riscoClassificacao +
                '}';
    }
}

