package br.gov.mma.facial.entity;

import br.gov.mma.facial.enums.ResultadoFiscalizacao;
import br.gov.mma.facial.enums.StatusFiscalizacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade representando fiscalizações realizadas em propriedades rurais
 */
@Entity
@Table(name = "fiscalizacoes", indexes = {
    @Index(name = "idx_fiscalizacao_propriedade", columnList = "propriedade_id"),
    @Index(name = "idx_fiscalizacao_data", columnList = "data_fiscalizacao"),
    @Index(name = "idx_fiscalizacao_responsavel", columnList = "responsavel")
})
@EntityListeners(AuditingEntityListener.class)
public class Fiscalizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Propriedade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    @NotNull(message = "Data da fiscalização é obrigatória")
    @Column(name = "data_fiscalizacao", nullable = false)
    private LocalDate dataFiscalizacao;

    @NotBlank(message = "Responsável pela fiscalização é obrigatório")
    @Size(max = 200, message = "Nome do responsável deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String responsavel;

    /**
     * Matrícula do responsável pela fiscalização
     */
    @Size(max = 20)
    @Column(name = "matricula_responsavel", length = 20)
    private String matriculaResponsavel;

    /**
     * Descrição das irregularidades encontradas
     */
    @Column(name = "irregularidades", columnDefinition = "TEXT")
    private String irregularidades;

    /**
     * Hash das provas/evidências coletadas
     * Por segurança, armazenamos apenas hash, não os arquivos originais
     */
    @Size(max = 500)
    @Column(name = "provas_hash", length = 500)
    private String provasHash;

    /**
     * Status da fiscalização
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFiscalizacao status = StatusFiscalizacao.EM_ANDAMENTO;

    /**
     * Resultado da fiscalização
     */
    @Enumerated(EnumType.STRING)
    @Column
    private ResultadoFiscalizacao resultado;

    /**
     * Valor da multa aplicada (se houver)
     */
    @Column(name = "valor_multa", precision = 15, scale = 2)
    private java.math.BigDecimal valorMulta;

    /**
     * Observações adicionais
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Fiscalizacao() {}

    public Fiscalizacao(Propriedade propriedade, LocalDate dataFiscalizacao, String responsavel) {
        this.propriedade = propriedade;
        this.dataFiscalizacao = dataFiscalizacao;
        this.responsavel = responsavel;
    }

    // Métodos utilitários
    public boolean temIrregularidades() {
        return irregularidades != null && !irregularidades.trim().isEmpty();
    }

    public boolean temMulta() {
        return valorMulta != null && valorMulta.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }

    public LocalDate getDataFiscalizacao() { return dataFiscalizacao; }
    public void setDataFiscalizacao(LocalDate dataFiscalizacao) { this.dataFiscalizacao = dataFiscalizacao; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getMatriculaResponsavel() { return matriculaResponsavel; }
    public void setMatriculaResponsavel(String matriculaResponsavel) { this.matriculaResponsavel = matriculaResponsavel; }

    public String getIrregularidades() { return irregularidades; }
    public void setIrregularidades(String irregularidades) { this.irregularidades = irregularidades; }

    public String getProvasHash() { return provasHash; }
    public void setProvasHash(String provasHash) { this.provasHash = provasHash; }

    public StatusFiscalizacao getStatus() { return status; }
    public void setStatus(StatusFiscalizacao status) { this.status = status; }

    public ResultadoFiscalizacao getResultado() { return resultado; }
    public void setResultado(ResultadoFiscalizacao resultado) { this.resultado = resultado; }

    public java.math.BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(java.math.BigDecimal valorMulta) { this.valorMulta = valorMulta; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fiscalizacao that = (Fiscalizacao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Fiscalizacao{" +
                "id=" + id +
                ", dataFiscalizacao=" + dataFiscalizacao +
                ", responsavel='" + responsavel + "'" +
                ", status=" + status +
                ", resultado=" + resultado +
                '}';
    }
}

