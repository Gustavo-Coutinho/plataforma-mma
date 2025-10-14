-- V1__Create_base_tables.sql
-- Criação das tabelas base do sistema de autenticação biométrica
-- Ministério do Meio Ambiente - Brasil

-- Extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de roles/perfis
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de usuários
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    orgao VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    failed_attempts INTEGER DEFAULT 0,
    account_locked BOOLEAN DEFAULT FALSE,
    lock_time TIMESTAMP,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO', 'PENDENTE_ATIVACAO'))
);

-- Tabela de relacionamento usuário-role (many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Tabela de templates biométricos faciais
CREATE TABLE user_face_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    template_bytes OID NOT NULL,
    algorithm_version VARCHAR(50) NOT NULL DEFAULT 'LBPH-v1.0',
    metadata TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    quality_score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Tabela de logs de auditoria
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(100),
    user_matricula VARCHAR(20),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(200) NOT NULL,
    result VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    correlation_id VARCHAR(50),
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    category VARCHAR(30) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_severity CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    CONSTRAINT chk_category CHECK (category IN ('AUTHENTICATION', 'AUTHORIZATION', 'DATA_ACCESS', 'DATA_MODIFICATION', 'BIOMETRIC', 'SYSTEM', 'SECURITY', 'LGPD_COMPLIANCE'))
);

-- Índices para performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_matricula ON users(matricula);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_face_templates_user_id ON user_face_templates(user_id);
CREATE INDEX idx_face_templates_primary ON user_face_templates(is_primary);
CREATE INDEX idx_face_templates_algorithm ON user_face_templates(algorithm_version);

CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_resource ON audit_logs(resource);
CREATE INDEX idx_audit_category ON audit_logs(category);
CREATE INDEX idx_audit_severity ON audit_logs(severity);

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comentários nas tabelas
COMMENT ON TABLE users IS 'Usuários do sistema de autenticação biométrica';
COMMENT ON TABLE roles IS 'Perfis de acesso do sistema (ROLE_PERFIL_1, ROLE_PERFIL_2, ROLE_MINISTRO)';
COMMENT ON TABLE user_face_templates IS 'Templates biométricos faciais processados pelo OpenCV';
COMMENT ON TABLE audit_logs IS 'Logs de auditoria para conformidade com LGPD';

-- Inserir roles padrão
INSERT INTO roles (name, description) VALUES
('ROLE_PERFIL_1', 'Acesso a informações públicas'),
('ROLE_PERFIL_2', 'Acesso a informações para diretores ambientais'),
('ROLE_MINISTRO', 'Acesso completo para o ministro do MA');