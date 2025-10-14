package br.gov.mma.facial.entity;

import br.gov.mma.facial.enums.NivelConfidencialidade;
import br.gov.mma.facial.enums.StatusRelatorio;
import br.gov.mma.facial.enums.TipoRelatorio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade representando relatórios do sistema
 * Diferentes tipos de relatórios para diferentes níveis de acesso
 */
@Entity
@Table(name = "relatorios", indexes = {
    @Index(name = "idx_relatorio_tipo", columnList = "tipo"),
    @Index(name = "idx_relatorio_confidencialidade", columnList = "confidencialidade"),
    @Index(name = "idx_relatorio_periodo", columnList = "periodo_inicio, periodo_fim")
})
@EntityListeners(AuditingEntityListener.class)
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título do relatório é obrigatório")
    @Size(max = 300, message = "Título deve ter no máximo 300 caracteres")
    @Column(nullable = false, length = 300)
    private String titulo;

    /**
     * Tipo do relatório
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRelatorio tipo;

    /**
     * Período de início dos dados do relatório
     */
    @Column(name = "periodo_inicio")
    private LocalDateTime periodoInicio;

    /**
     * Período de fim dos dados do relatório
     */
    @Column(name = "periodo_fim")
    private LocalDateTime periodoFim;

    /**
     * Escopo geográfico do relatório
     */
    @Size(max = 200)
    @Column(length = 200)
    private String escopo;

    /**
     * Resumo executivo do relatório
     */
    @Column(columnDefinition = "TEXT")
    private String resumo;

    /**
     * Conteúdo completo do relatório (JSON ou texto)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String conteudo;

    /**
     * Briefing detalhado do relatório (análise completa e aprofundada)
     */
    @Lob
    @Column(name = "briefing_detalhado", columnDefinition = "TEXT")
    private String briefingDetalhado;

    /**
     * Nível de confidencialidade
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelConfidencialidade confidencialidade = NivelConfidencialidade.PUBLICO;

    /**
     * Autor do relatório
     */
    @Size(max = 200)
    @Column(length = 200)
    private String autor;

    /**
     * Matrícula do autor
     */
    @Size(max = 20)
    @Column(name = "matricula_autor", length = 20)
    private String matriculaAutor;

    /**
     * Status do relatório
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRelatorio status = StatusRelatorio.RASCUNHO;

    /**
     * Tags para categorização
     */
    @Size(max = 500)
    @Column(length = 500)
    private String tags;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Constructors
    public Relatorio() {}

    public Relatorio(String titulo, TipoRelatorio tipo, NivelConfidencialidade confidencialidade) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.confidencialidade = confidencialidade;
    }

    // Métodos utilitários
    public boolean isPublico() {
        return confidencialidade == NivelConfidencialidade.PUBLICO;
    }

    public boolean isRestrito() {
        return confidencialidade == NivelConfidencialidade.RESTRITO;
    }

    public boolean isUltraSecreto() {
        return confidencialidade == NivelConfidencialidade.ULTRASSECRETO;
    }

    public boolean isPublished() {
        return status == StatusRelatorio.PUBLICADO && publishedAt != null;
    }

    public void publish() {
        this.status = StatusRelatorio.PUBLICADO;
        this.publishedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public TipoRelatorio getTipo() { return tipo; }
    public void setTipo(TipoRelatorio tipo) { this.tipo = tipo; }

    public LocalDateTime getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDateTime periodoInicio) { this.periodoInicio = periodoInicio; }

    public LocalDateTime getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(LocalDateTime periodoFim) { this.periodoFim = periodoFim; }

    public String getEscopo() { return escopo; }
    public void setEscopo(String escopo) { this.escopo = escopo; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getBriefingDetalhado() { return briefingDetalhado; }
    public void setBriefingDetalhado(String briefingDetalhado) { this.briefingDetalhado = briefingDetalhado; }

    public NivelConfidencialidade getConfidencialidade() { return confidencialidade; }
    public void setConfidencialidade(NivelConfidencialidade confidencialidade) { this.confidencialidade = confidencialidade; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getMatriculaAutor() { return matriculaAutor; }
    public void setMatriculaAutor(String matriculaAutor) { this.matriculaAutor = matriculaAutor; }

    public StatusRelatorio getStatus() { return status; }
    public void setStatus(StatusRelatorio status) { this.status = status; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relatorio relatorio = (Relatorio) o;
        return Objects.equals(id, relatorio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Relatorio{" +
                "id=" + id +
                ", titulo='" + titulo + "'" +
                ", tipo=" + tipo +
                ", confidencialidade=" + confidencialidade +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}