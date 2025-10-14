-- V2__Create_environmental_tables.sql
-- Criação das tabelas do domínio ambiental
-- Propriedades, agrotóxicos, fiscalizações e relatórios

-- Tabela de propriedades rurais
CREATE TABLE propriedades (
    id BIGSERIAL PRIMARY KEY,
    municipio VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    geolocalizacao VARCHAR(50),
    cpf_cnpj_proprietario VARCHAR(20) NOT NULL,
    nome_proprietario VARCHAR(200),
    area_ha NUMERIC(10,2) NOT NULL,
    status_regularidade VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    numero_infracoes INTEGER DEFAULT 0,
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status_regularidade CHECK (status_regularidade IN ('REGULAR', 'IRREGULAR', 'PENDENTE', 'EMBARGADA', 'LICENCIADA')),
    CONSTRAINT chk_area_positiva CHECK (area_ha > 0),
    CONSTRAINT chk_infracoes_nao_negativa CHECK (numero_infracoes >= 0)
);

-- Tabela de agrotóxicos
CREATE TABLE agrotoxicos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    principio_ativo VARCHAR(500),
    status_proibicao VARCHAR(20) NOT NULL DEFAULT 'LIBERADO',
    risco_classificacao VARCHAR(20) NOT NULL DEFAULT 'BAIXO',
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status_proibicao CHECK (status_proibicao IN ('LIBERADO', 'RESTRITO', 'PROIBIDO', 'BANIDO', 'EM_ANALISE')),
    CONSTRAINT chk_risco_classificacao CHECK (risco_classificacao IN ('BAIXO', 'MEDIO', 'ALTO', 'EXTREMO', 'CRITICO'))
);

-- Tabela de fiscalizações
CREATE TABLE fiscalizacoes (
    id BIGSERIAL PRIMARY KEY,
    propriedade_id BIGINT NOT NULL REFERENCES propriedades(id) ON DELETE CASCADE,
    data_fiscalizacao DATE NOT NULL,
    responsavel VARCHAR(200) NOT NULL,
    matricula_responsavel VARCHAR(20),
    irregularidades TEXT,
    provas_hash VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    resultado VARCHAR(30),
    valor_multa NUMERIC(15,2),
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status_fiscalizacao CHECK (status IN ('AGENDADA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA', 'PENDENTE_RECURSO')),
    CONSTRAINT chk_resultado_fiscalizacao CHECK (resultado IN ('CONFORME', 'NAO_CONFORME', 'PARCIALMENTE_CONFORME', 'NECESSITA_ADEQUACAO', 'MULTADO', 'EMBARGADO')),
    CONSTRAINT chk_valor_multa_positiva CHECK (valor_multa IS NULL OR valor_multa >= 0)
);

-- Tabela de relatórios
CREATE TABLE relatorios (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(300) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    periodo_inicio TIMESTAMP,
    periodo_fim TIMESTAMP,
    escopo VARCHAR(200),
    resumo TEXT,
    conteudo TEXT,
    confidencialidade VARCHAR(20) NOT NULL DEFAULT 'PUBLICO',
    autor VARCHAR(200),
    matricula_autor VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    tags VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,

    CONSTRAINT chk_tipo_relatorio CHECK (tipo IN ('AGREGADO', 'TECNICO', 'EXECUTIVO', 'FISCALIZACAO', 'INVESTIGATIVO', 'TERRITORIAL', 'ESTATISTICO', 'COMPLIANCE')),
    CONSTRAINT chk_confidencialidade CHECK (confidencialidade IN ('PUBLICO', 'RESTRITO', 'ULTRASSECRETO')),
    CONSTRAINT chk_status_relatorio CHECK (status IN ('RASCUNHO', 'EM_REVISAO', 'APROVADO', 'PUBLICADO', 'ARQUIVADO'))
);

-- Índices para performance
CREATE INDEX idx_propriedade_uf_municipio ON propriedades(uf, municipio);
CREATE INDEX idx_propriedade_status ON propriedades(status_regularidade);
CREATE INDEX idx_propriedade_cpf_cnpj ON propriedades(cpf_cnpj_proprietario);

CREATE INDEX idx_agrotoxico_status ON agrotoxicos(status_proibicao);
CREATE INDEX idx_agrotoxico_risco ON agrotoxicos(risco_classificacao);

CREATE INDEX idx_fiscalizacao_propriedade ON fiscalizacoes(propriedade_id);
CREATE INDEX idx_fiscalizacao_data ON fiscalizacoes(data_fiscalizacao);
CREATE INDEX idx_fiscalizacao_responsavel ON fiscalizacoes(responsavel);

CREATE INDEX idx_relatorio_tipo ON relatorios(tipo);
CREATE INDEX idx_relatorio_confidencialidade ON relatorios(confidencialidade);
CREATE INDEX idx_relatorio_periodo ON relatorios(periodo_inicio, periodo_fim);

-- Triggers para updated_at
CREATE TRIGGER update_propriedades_updated_at BEFORE UPDATE ON propriedades
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agrotoxicos_updated_at BEFORE UPDATE ON agrotoxicos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comentários
COMMENT ON TABLE propriedades IS 'Propriedades rurais monitoradas pelo sistema';
COMMENT ON TABLE agrotoxicos IS 'Agrotóxicos catalogados e seu status de regulamentação';
COMMENT ON TABLE fiscalizacoes IS 'Fiscalizações realizadas nas propriedades rurais';
COMMENT ON TABLE relatorios IS 'Relatórios do sistema com diferentes níveis de acesso';