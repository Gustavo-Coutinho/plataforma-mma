-- V3__Insert_sample_data.sql
-- Inserção de dados de exemplo para demonstração e testes

-- Inserir usuários de exemplo
-- Senhas geradas: Ana=MMA@2024!, Carlos=Fiscaliza#2024, Maria=Monitor$2024, João=Atende&2024
INSERT INTO users (nome, email, matricula, orgao, password_hash, status) VALUES
('Ana Silva', 'ana.silva@mma.gov.br', 'MIN001', 'Ministério do Meio Ambiente', '$2a$10$NU/2IdkwGxnTkGSvTiruQObWlIVG0fALw8GTQnFc3TF8nLNqCvdu2', 'ATIVO'),
('Carlos Santos', 'carlos.santos@mma.gov.br', 'DIR001', 'Diretoria de Fiscalização', '$2a$10$U8mD7Itt5wqdb7hxa8jhgeLWKjc2fDsVVELP9gAyYopVmr0HP45LG', 'ATIVO'),
('Maria Costa', 'maria.costa@mma.gov.br', 'DIR002', 'Diretoria de Monitoramento', '$2a$10$AReSvX7w1J8/XDWADDF0v.kLsOuYvXTjrjGwSCRAU4T.Shq4ufdYq', 'ATIVO'),
('João Oliveira', 'joao.oliveira@mma.gov.br', 'USR001', 'Setor de Atendimento', '$2a$10$yV4BIi9WzjWrJ54CCOKm1eIDzjmkRsC0bUlEO.DS5wzvzDf61CwHy', 'ATIVO');

-- Associar usuários às roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 3), -- Ana Silva -> ROLE_MINISTRO
(2, 2), -- Carlos Santos -> ROLE_PERFIL_2
(3, 2), -- Maria Costa -> ROLE_PERFIL_2
(4, 1); -- João Oliveira -> ROLE_PERFIL_1

-- Inserir propriedades de exemplo
INSERT INTO propriedades (municipio, uf, geolocalizacao, cpf_cnpj_proprietario, nome_proprietario, area_ha, status_regularidade, numero_infracoes) VALUES
('Sorriso', 'MT', '-12.5475,-55.7175', '12.345.678/0001-90', 'Fazenda Grande Ltda', 1500.50, 'IRREGULAR', 3),
('Rio Verde', 'GO', '-17.7973,-50.9249', '98.765.432/0001-10', 'Agropecuária Verde S/A', 800.20, 'REGULAR', 0),
('Luís Eduardo Magalhães', 'BA', '-12.0969,-45.7825', '11.222.333/0001-44', 'Produtora Rural BA', 2300.80, 'IRREGULAR', 1),
('Campo Grande', 'MS', '-20.4428,-54.6464', '44.555.666/0001-77', 'Fazenda Pantanal', 950.00, 'REGULAR', 0),
('Sinop', 'MT', '-11.8649,-55.5039', '77.888.999/0001-22', 'Agro Sinop Ltda', 1200.75, 'PENDENTE', 0);

-- Inserir agrotóxicos de exemplo
INSERT INTO agrotoxicos (nome, principio_ativo, status_proibicao, risco_classificacao) VALUES
('Glifosato', 'N-(fosfonometil)glicina', 'RESTRITO', 'ALTO'),
('2,4-D', 'Ácido 2,4-diclorofenoxiacético', 'PROIBIDO', 'EXTREMO'),
('Atrazina', '6-cloro-N-etil-N''-(1-metiletil)-1,3,5-triazina-2,4-diamina', 'RESTRITO', 'MEDIO'),
('Paraquat', '1,1''-dimetil-4,4''-bipiridínio', 'BANIDO', 'CRITICO'),
('Carbendazim', 'Metil benzimidazol-2-ilcarbamato', 'LIBERADO', 'BAIXO');

-- Inserir fiscalizações de exemplo
INSERT INTO fiscalizacoes (propriedade_id, data_fiscalizacao, responsavel, matricula_responsavel, irregularidades, status, resultado, valor_multa) VALUES
(1, '2024-08-15', 'Carlos Santos', 'DIR001', 'Uso de agrotóxico proibido (2,4-D) em área próxima a manancial', 'CONCLUIDA', 'MULTADO', 50000.00),
(1, '2024-07-20', 'Maria Costa', 'DIR002', 'Aplicação sem licença ambiental', 'CONCLUIDA', 'NAO_CONFORME', 25000.00),
(3, '2024-09-01', 'Carlos Santos', 'DIR001', 'Descarte inadequado de embalagens', 'CONCLUIDA', 'NECESSITA_ADEQUACAO', 15000.00),
(2, '2024-08-30', 'Maria Costa', 'DIR002', NULL, 'CONCLUIDA', 'CONFORME', NULL),
(5, '2024-09-20', 'Carlos Santos', 'DIR001', NULL, 'EM_ANDAMENTO', NULL, NULL);

-- Inserir relatórios de exemplo
INSERT INTO relatorios (titulo, tipo, periodo_inicio, periodo_fim, escopo, resumo, confidencialidade, autor, matricula_autor, status, tags, published_at) VALUES
('Monitoramento de Agrotóxicos em Águas Superficiais - 2024', 'AGREGADO', '2024-01-01', '2024-08-31', 'Nacional', 'Análise de 1.200 pontos de coleta em rios e lagos brasileiros', 'PUBLICO', 'Maria Costa', 'DIR002', 'PUBLICADO', 'agua,agrotoxicos,monitoramento', '2024-09-15 10:00:00'),
('Mapa Nacional de Uso de Defensivos Agrícolas', 'TERRITORIAL', '2024-01-01', '2024-07-31', 'Nacional', 'Dados georreferenciados de aplicação por região', 'PUBLICO', 'João Oliveira', 'USR001', 'PUBLICADO', 'mapa,defensivos,territorial', '2024-08-30 14:30:00'),
('Irregularidades por Propriedade Rural - Detalhado', 'FISCALIZACAO', '2024-07-01', '2024-09-30', 'Centro-Oeste', 'Lista completa de propriedades com infrações ambientais', 'RESTRITO', 'Carlos Santos', 'DIR001', 'PUBLICADO', 'fiscalizacao,infraçoes,propriedades', '2024-09-20 09:15:00'),
('Análise de Impacto em Lençóis Freáticos', 'TECNICO', '2024-06-01', '2024-08-31', 'Cerrado', 'Correlação entre uso de agrotóxicos e contaminação de águas subterrâneas', 'RESTRITO', 'Maria Costa', 'DIR002', 'PUBLICADO', 'lencol,freatico,contaminacao', '2024-09-18 16:45:00'),
('Relatório Executivo - Crise Ambiental Nacional', 'EXECUTIVO', '2024-08-01', '2024-09-30', 'Nacional', 'Panorama completo dos riscos ambientais e estratégias governamentais', 'ULTRASSECRETO', 'Ana Silva', 'MIN001', 'PUBLICADO', 'executivo,crise,estrategia', '2024-09-25 11:00:00'),
('Dossiê: Grandes Produtores Irregulares', 'INVESTIGATIVO', '2024-06-01', '2024-09-30', 'Nacional', 'Identificação de grandes produtores com uso irregular de agrotóxicos', 'ULTRASSECRETO', 'Ana Silva', 'MIN001', 'PUBLICADO', 'dossie,produtores,investigativo', '2024-09-22 13:20:00');

-- Inserir log de auditoria de exemplo
INSERT INTO audit_logs (user_id, user_name, user_matricula, action, resource, result, details, ip_address, severity, category) VALUES
(1, 'Ana Silva', 'MIN001', 'LOGIN', 'Sistema', 'SUCCESS', 'Login realizado com sucesso via autenticação biométrica', '192.168.1.100', 'INFO', 'AUTHENTICATION'),
(2, 'Carlos Santos', 'DIR001', 'DATA_ACCESS', 'Relatórios Restritos', 'SUCCESS', 'Acesso a relatório de fiscalização', '192.168.1.101', 'INFO', 'DATA_ACCESS'),
(4, 'João Oliveira', 'USR001', 'DATA_ACCESS', 'Relatórios Ultrasecretos', 'BLOCKED', 'Tentativa de acesso negada - permissão insuficiente', '192.168.1.102', 'WARNING', 'AUTHORIZATION'),
(3, 'Maria Costa', 'DIR002', 'BIOMETRIC_ENROLLMENT', 'Template Facial', 'SUCCESS', 'Cadastro de template biométrico realizado', '192.168.1.103', 'INFO', 'BIOMETRIC');

-- Atualizar sequências para evitar conflitos
SELECT setval('users_id_seq', 10);
SELECT setval('propriedades_id_seq', 10);
SELECT setval('agrotoxicos_id_seq', 10);
SELECT setval('fiscalizacoes_id_seq', 10);
SELECT setval('relatorios_id_seq', 10);
SELECT setval('audit_logs_id_seq', 10);