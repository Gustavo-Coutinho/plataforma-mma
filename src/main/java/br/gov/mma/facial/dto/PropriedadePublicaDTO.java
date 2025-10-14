package br.gov.mma.facial.dto;

import java.math.BigDecimal;

/**
 * DTO para dados públicos de propriedades (dados sensíveis mascarados)
 */
public class PropriedadePublicaDTO {

    private Long id;
    private String municipio;
    private String uf;
    private BigDecimal areaHa;
    private String statusRegularidade;
    private Integer numeroInfracoes;

    // Constructor usado pela query do repositório
    public PropriedadePublicaDTO(Long id, String municipio, String uf, BigDecimal areaHa, 
                                String statusRegularidade, Integer numeroInfracoes) {
        this.id = id;
        this.municipio = municipio;
        this.uf = uf;
        this.areaHa = areaHa;
        this.statusRegularidade = statusRegularidade;
        this.numeroInfracoes = numeroInfracoes;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }

    public String getStatusRegularidade() { return statusRegularidade; }
    public void setStatusRegularidade(String statusRegularidade) { this.statusRegularidade = statusRegularidade; }

    public Integer getNumeroInfracoes() { return numeroInfracoes; }
    public void setNumeroInfracoes(Integer numeroInfracoes) { this.numeroInfracoes = numeroInfracoes; }
}