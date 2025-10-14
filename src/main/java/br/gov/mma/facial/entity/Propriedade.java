package br.gov.mma.facial.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade representando propriedades rurais monitoradas
 * Contém informações sobre localização, proprietário e status ambiental
 */
@Entity
@Table(name = "propriedades", indexes = {
    @Index(name = "idx_propriedade_uf_municipio", columnList = "uf, municipio"),
    @Index(name = "idx_propriedade_status", columnList = "status_regularidade"),
    @Index(name = "idx_propriedade_cpf_cnpj", columnList = "cpf_cnpj_proprietario")
})
@EntityListeners(AuditingEntityListener.class)
public class Propriedade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Município é obrigatório")
    @Size(max = 100, message = "Município deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String municipio;

    @NotBlank(message = "UF é obrigatória")
    @Size(min = 2, max = 2, message = "UF deve ter exatamente 2 caracteres")
    @Column(nullable = false, length = 2)
    private String uf;

    /**
     * Geolocalização da propriedade (formato: latitude,longitude)
     */
    @Size(max = 50)
    @Column(name = "geolocalizacao", length = 50)
    private String geolocalizacao;

    /**
     * CPF ou CNPJ do proprietário (dados sensíveis - mascarados para ROLE_PERFIL_1)
     */
    @NotBlank(message = "CPF/CNPJ do proprietário é obrigatório")
    @Size(max = 20)
    @Column(name = "cpf_cnpj_proprietario", nullable = false, length = 20)
    private String cpfCnpjProprietario;

    /**
     * Nome do proprietário (dados sensíveis)
     */
    @Size(max = 200)
    @Column(name = "nome_proprietario", length = 200)
    private String nomeProprietario;

    /**
     * Área da propriedade em hectares
     */
    @NotNull(message = "Área é obrigatória")
    @Positive(message = "Área deve ser positiva")
    @Column(name = "area_ha", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHa;

    /**
     * Status de regularidade ambiental
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_regularidade", nullable = false)
    private StatusRegularidade statusRegularidade = StatusRegularidade.PENDENTE;

    /**
     * Número de infrações ambientais registradas
     */
    @Column(name = "numero_infracoes")
    private Integer numeroInfracoes = 0;

    /**
     * Observações sobre a propriedade
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    /**
     * Fiscalizações realizadas na propriedade
     */
    @OneToMany(mappedBy = "propriedade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Fiscalizacao> fiscalizacoes = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Propriedade() {}

    public Propriedade(String municipio, String uf, String cpfCnpjProprietario, BigDecimal areaHa) {
        this.municipio = municipio;
        this.uf = uf;
        this.cpfCnpjProprietario = cpfCnpjProprietario;
        this.areaHa = areaHa;
    }

    /**
     * Retorna CPF/CNPJ mascarado para ROLE_PERFIL_1
     */
    public String getCpfCnpjMascarado() {
        if (cpfCnpjProprietario == null || cpfCnpjProprietario.length() < 8) {
            return "***.***.***-**";
        }

        return cpfCnpjProprietario.substring(0, 3) + 
               ".***." + 
               cpfCnpjProprietario.substring(cpfCnpjProprietario.length() - 2) + 
               "-**";
    }

    /**
     * Adiciona fiscalização à propriedade
     */
    public void addFiscalizacao(Fiscalizacao fiscalizacao) {
        this.fiscalizacoes.add(fiscalizacao);
        fiscalizacao.setPropriedade(this);
    }

    /**
     * Remove fiscalização da propriedade
     */
    public void removeFiscalizacao(Fiscalizacao fiscalizacao) {
        this.fiscalizacoes.remove(fiscalizacao);
        fiscalizacao.setPropriedade(null);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getGeolocalizacao() { return geolocalizacao; }
    public void setGeolocalizacao(String geolocalizacao) { this.geolocalizacao = geolocalizacao; }

    public String getCpfCnpjProprietario() { return cpfCnpjProprietario; }
    public void setCpfCnpjProprietario(String cpfCnpjProprietario) { this.cpfCnpjProprietario = cpfCnpjProprietario; }

    public String getNomeProprietario() { return nomeProprietario; }
    public void setNomeProprietario(String nomeProprietario) { this.nomeProprietario = nomeProprietario; }

    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }

    public StatusRegularidade getStatusRegularidade() { return statusRegularidade; }
    public void setStatusRegularidade(StatusRegularidade statusRegularidade) { this.statusRegularidade = statusRegularidade; }

    public Integer getNumeroInfracoes() { return numeroInfracoes; }
    public void setNumeroInfracoes(Integer numeroInfracoes) { this.numeroInfracoes = numeroInfracoes; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Set<Fiscalizacao> getFiscalizacoes() { return fiscalizacoes; }
    public void setFiscalizacoes(Set<Fiscalizacao> fiscalizacoes) { this.fiscalizacoes = fiscalizacoes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propriedade that = (Propriedade) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Propriedade{" +
                "id=" + id +
                ", municipio='" + municipio + "'" +
                ", uf='" + uf + "'" +
                ", areaHa=" + areaHa +
                ", statusRegularidade=" + statusRegularidade +
                ", numeroInfracoes=" + numeroInfracoes +
                '}';
    }
}

/**
 * Enum para status de regularidade da propriedade
 */
enum StatusRegularidade {
    REGULAR,
    IRREGULAR,
    PENDENTE,
    EMBARGADA,
    LICENCIADA
}