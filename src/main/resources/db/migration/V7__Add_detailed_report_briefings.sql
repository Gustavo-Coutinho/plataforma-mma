-- V7__Add_detailed_report_briefings.sql
-- Adiciona coluna de briefing detalhado e atualiza relatórios existentes com conteúdo realista

-- Adicionar coluna para briefing detalhado
ALTER TABLE relatorios ADD COLUMN IF NOT EXISTS briefing_detalhado TEXT;

-- Atualizar relatórios existentes com briefings detalhados e realistas

-- Relatório 1: Monitoramento de Agrotóxicos em Águas Superficiais
UPDATE relatorios SET briefing_detalhado = 
'SUMÁRIO EXECUTIVO

O presente relatório apresenta os resultados do monitoramento sistemático de resíduos de agrotóxicos em corpos hídricos superficiais brasileiros durante o período de janeiro a agosto de 2024.

METODOLOGIA
• 1.200 pontos de coleta distribuídos em 847 municípios
• Análise laboratorial de 47 princípios ativos prioritários
• Frequência mensal de coleta em bacias hidrográficas críticas

PRINCIPAIS ACHADOS
• 67% das amostras apresentaram resíduos de pelo menos um agrotóxico
• Glifosato detectado em 42% dos pontos (concentração média: 12,3 µg/L)
• Atrazina acima do limite em 23% das amostras da região Centro-Oeste
• 2,4-D encontrado em 156 amostras apesar da proibição vigente desde 2023

ÁREAS CRÍTICAS IDENTIFICADAS
1. Bacia do Rio São Francisco (Oeste da Bahia): 89% de contaminação
2. Afluentes do Xingu (Norte do Mato Grosso): 78% de contaminação
3. Rio Tietê (Interior de São Paulo): 71% de contaminação

IMPACTOS OBSERVADOS
• Mortalidade de peixes em 34 eventos registrados
• Redução de 40% na população de anfíbios em áreas próximas a plantações
• Contaminação de sistemas de captação de água para abastecimento público em 12 municípios

RECOMENDAÇÕES
1. Intensificação da fiscalização em áreas críticas identificadas
2. Revisão dos limites máximos permitidos para Glifosato e Atrazina
3. Implementação de zonas de proteção hídrica em áreas agrícolas
4. Programa emergencial de tratamento de água em municípios afetados

CONCLUSÃO
Os dados revelam um quadro alarmante de contaminação hídrica, especialmente nas principais regiões produtoras de grãos. A situação demanda ação governamental imediata para proteção dos recursos hídricos e saúde pública.'
WHERE id = 1;

-- Relatório 2: Mapa Nacional de Uso de Defensivos Agrícolas
UPDATE relatorios SET briefing_detalhado = 
'APRESENTAÇÃO

Este relatório geoespacial consolida dados de aplicação de defensivos agrícolas em território nacional, integrando informações do IBAMA, ANVISA e secretarias estaduais de agricultura.

ESCOPO TERRITORIAL
• Cobertura: 100% do território nacional
• Resolução espacial: 250 metros
• Período de referência: Janeiro a Julho de 2024

DADOS CONSOLIDADOS
Volume total de defensivos aplicados: 847.234 toneladas
• Herbicidas: 512.890 t (60,5%)
• Fungicidas: 189.123 t (22,3%)
• Inseticidas: 145.221 t (17,2%)

DISTRIBUIÇÃO REGIONAL
NORTE (78.234 t)
• Principal cultivo: Soja (89%)
• Produtos mais usados: Glifosato, Paraquat (uso irregular)
• Crescimento vs 2023: +18%

NORDESTE (134.567 t)
• Principal cultivo: Algodão (45%), Cana-de-açúcar (32%)
• Produtos mais usados: 2,4-D (irregular), Acefato
• Crescimento vs 2023: +12%

CENTRO-OESTE (423.890 t) - REGIÃO CRÍTICA
• Principal cultivo: Soja (67%), Milho (28%)
• Produtos mais usados: Glifosato, Atrazina, Mancozeb
• Crescimento vs 2023: +31%
• Concentração: 50% de todo uso nacional

SUDESTE (156.234 t)
• Principal cultivo: Cana-de-açúcar (52%), Café (23%)
• Produtos mais usados: Diuron, Hexazinona
• Crescimento vs 2023: +8%

SUL (54.309 t)
• Principal cultivo: Trigo (41%), Soja (35%)
• Produtos mais usados: Glifosato, Epoxiconazol
• Crescimento vs 2023: +15%

HOTSPOTS IDENTIFICADOS
1. Matopiba (MA/TO/PI/BA): 234.567 kg/km² - CRÍTICO
2. Oeste da Bahia: 189.432 kg/km² - ALTO
3. Norte do Mato Grosso: 167.234 kg/km² - ALTO
4. Triângulo Mineiro: 98.765 kg/km² - MÉDIO

OBSERVAÇÕES TÉCNICAS
• Aumento de 22% no uso de produtos proibidos ou restritos
• Aplicação irregular em áreas de preservação permanente: 12.340 hectares
• Deriva para áreas urbanas registrada em 67 municípios

ALERTAS
⚠️ Uso crescente de produtos banidos sugere fiscalização insuficiente
⚠️ Concentração extrema no Centro-Oeste excede padrões internacionais
⚠️ Aplicação em período de chuvas aumenta contaminação de aquíferos'
WHERE id = 2;

-- Relatório 3: Irregularidades por Propriedade Rural
UPDATE relatorios SET briefing_detalhado = 
'RELATÓRIO DE FISCALIZAÇÃO - CONFIDENCIAL
DIRETORIA DE FISCALIZAÇÃO AMBIENTAL

SUMÁRIO
Consolidação das atividades de fiscalização realizadas no período de julho a setembro de 2024, com foco em propriedades rurais da região Centro-Oeste com histórico de infrações ambientais.

ESTATÍSTICAS GERAIS
• Propriedades fiscalizadas: 847
• Autos de infração lavrados: 423 (49,9%)
• Valor total de multas aplicadas: R$ 127.850.000,00
• Áreas embargadas: 23.456 hectares

CATEGORIAS DE INFRAÇÕES

1. USO DE AGROTÓXICOS PROIBIDOS (178 autuações)
Valor médio de multa: R$ 89.000,00
Principais produtos identificados:
• Paraquat (banido desde 2020): 89 casos
• 2,4-D (proibido desde 2023): 67 casos
• Carbofurano (banido): 22 casos

2. APLICAÇÃO IRREGULAR (156 autuações)
Valor médio de multa: R$ 45.000,00
• Aplicação aérea em área urbana: 45 casos
• Aplicação em APP: 78 casos
• Deriva para propriedades vizinhas: 33 casos

3. DESCARTE INADEQUADO (89 autuações)
Valor médio de multa: R$ 35.000,00
• Queima de embalagens: 34 casos
• Descarte em corpos hídricos: 28 casos
• Armazenamento irregular: 27 casos

TOP 20 PROPRIEDADES IRREGULARES

1. Fazenda Boa Vista (MT) - CNPJ 12.345.678/0001-90
   Área: 8.900 ha | Multas: R$ 2.340.000 | Infrações: 7
   Situação: Uso de Paraquat, aplicação em APP, contaminação de rio

2. Agropecuária Palmeiras (GO) - CNPJ 23.456.789/0001-12
   Área: 6.700 ha | Multas: R$ 1.890.000 | Infrações: 5
   Situação: Estoque de 2,4-D, aplicação sem licença

3. Fazenda Grande Ltda (MT) - CNPJ 12.345.678/0001-90
   Área: 1.500 ha | Multas: R$ 75.000 | Infrações: 3
   Situação: Uso irregular, descarte inadequado

[Lista completa disponível no Anexo A - páginas 45-67]

ANÁLISE DE REINCIDÊNCIA
• 34% dos autuados são reincidentes
• Média de infrações por reincidente: 3,2
• Crescimento de reincidência vs 2023: +28%

PRODUTOS APREENDIDOS
• Paraquat: 4.567 litros (89 propriedades)
• 2,4-D: 3.234 litros (67 propriedades)
• Outros produtos banidos: 1.890 litros

EMBARGOS APLICADOS
23 propriedades com embargo total (23.456 ha)
Principais motivos:
• Contaminação grave de mananciais: 12 casos
• Uso continuado após autuação: 8 casos
• Dano ambiental irreversível: 3 casos

PROCESSOS JUDICIAIS
• Ações civis públicas: 45
• Processos criminais: 23
• Termos de ajustamento de conduta: 156

RECOMENDAÇÕES URGENTES
1. Reforço de fiscalização nas 20 propriedades críticas
2. Monitoramento mensal de reincidentes
3. Intensificação de operações em MT e GO
4. Articulação com Polícia Federal para casos graves'
WHERE id = 3;

-- Relatório 4: Análise de Impacto em Lençóis Freáticos
UPDATE relatorios SET briefing_detalhado = 
'RELATÓRIO TÉCNICO-CIENTÍFICO
ANÁLISE DE CONTAMINAÇÃO DE ÁGUAS SUBTERRÂNEAS

CONTEXTO
O bioma Cerrado abriga importantes aquíferos (Guarani, Bambuí, Urucuia) que abastecem milhões de pessoas. O avanço da fronteira agrícola nas últimas décadas levanta preocupações sobre impactos na qualidade dessas águas subterrâneas.

OBJETIVO
Avaliar a correlação entre intensidade de uso de agrotóxicos e contaminação de lençóis freáticos em áreas de recarga hídrica do Cerrado.

METODOLOGIA
• Período: Junho a Agosto de 2024
• Pontos de amostragem: 340 poços (78 artesianos, 262 freáticos)
• Profundidade média: 45 metros
• Parâmetros analisados: 52 princípios ativos + metabólitos

ÁREA DE ESTUDO
Municípios de alto uso agrícola:
• Oeste da Bahia: 89 poços
• Goiás (Sul e Sudoeste): 112 poços
• Tocantins (Sul): 67 poços
• Mato Grosso do Sul (Norte): 72 poços

RESULTADOS ALARMANTES

TAXA DE CONTAMINAÇÃO GERAL: 71%
• 242 poços com presença de pelo menos um agrotóxico
• 156 poços acima dos limites de potabilidade
• 89 poços com múltiplos contaminantes (3+ substâncias)

PRINCIPAIS CONTAMINANTES DETECTADOS

1. ATRAZINA (detectada em 58% dos poços)
   Concentração média: 18,7 µg/L
   Concentração máxima: 127,3 µg/L (Limite: 2 µg/L)
   Origem: Cultivo de milho e sorgo
   Status: Persistência elevada em meio anaeróbico

2. GLIFOSATO e AMPA (48% dos poços)
   Glifosato médio: 12,3 µg/L
   AMPA (metabólito) médio: 34,6 µg/L
   Preocupação: AMPA mais tóxico que composto original

3. IMIDACLOPRIDO (34% dos poços)
   Concentração média: 8,9 µg/L
   Origem: Tratamento de sementes
   Alerta: Alto risco para abelhas e polinizadores

4. HEXAZINONA (23% dos poços)
   Concentração média: 15,2 µg/L
   Origem: Cultivo de cana-de-açúcar
   Risco: Elevada mobilidade no solo

ÁREAS CRÍTICAS (Contaminação >80%)

1. Luis Eduardo Magalhães (BA)
   Taxa de contaminação: 94%
   Poços afetados: 33 de 35
   Principais: Atrazina (127 µg/L), 2,4-D (67 µg/L)
   População em risco: 87.000 habitantes

2. Rio Verde (GO)
   Taxa de contaminação: 87%
   Poços afetados: 39 de 45
   Principais: Glifosato (89 µg/L), Imidacloprido (34 µg/L)
   População em risco: 245.000 habitantes

3. Porto Nacional (TO)
   Taxa de contaminação: 82%
   Poços afetados: 28 de 34
   Principais: Atrazina (98 µg/L), Hexazinona (45 µg/L)
   População em risco: 56.000 habitantes

CORRELAÇÃO ESTATÍSTICA
Análise de regressão múltipla demonstrou:
• r² = 0,87 entre volume de agrotóxicos/hectare e contaminação
• Tempo de percolação até lençol: 18-36 meses
• Contaminação detectada em áreas tratadas há 2-3 anos

IMPACTOS NA SAÚDE PÚBLICA
Municípios com abastecimento público comprometido:
• 12 municípios em situação crítica
• 478.000 pessoas expostas a água contaminada
• Aumento de 34% em casos de intoxicação (2023-2024)

Correlação epidemiológica observada:
• Malformações congênitas: +28% em áreas críticas
• Doenças renais crônicas: +45%
• Distúrbios endócrinos: +52%

PROJEÇÕES E CENÁRIOS

CENÁRIO ATUAL (2024):
71% de contaminação, tendência de agravamento

CENÁRIO PESSIMISTA (2030):
89% de contaminação se mantido padrão atual
Colapso de sistemas de abastecimento em 34 municípios

CENÁRIO OTIMISTA (2030):
48% de contaminação com implementação de medidas restritivas
Requer: Redução de 60% no uso de agrotóxicos críticos

RECOMENDAÇÕES TÉCNICAS

MEDIDAS EMERGENCIAIS:
1. Suspensão imediata do uso de Atrazina em zonas de recarga
2. Instalação de sistemas de tratamento avançado em 12 municípios
3. Monitoramento trimestral de todos os poços públicos

MEDIDAS ESTRUTURAIS:
1. Criação de zonas de proteção hídrica (raio de 5 km de áreas de recarga)
2. Proibição de cultivos intensivos em áreas críticas
3. Programa de conversão para agricultura orgânica
4. Investimento em pesquisa de alternativas aos agrotóxicos

MEDIDAS REGULATÓRIAS:
1. Revisão emergencial dos LMR (Limites Máximos de Resíduos)
2. Reclassificação de Atrazina e Glifosato para uso controlado
3. Tributação diferenciada para produtos de alto risco ambiental

CONCLUSÃO
Os dados apresentam evidência científica inequívoca de contaminação crítica dos aquíferos do Cerrado. A situação constitui emergência de saúde pública e ambiental, demandando ação governamental coordenada e imediata. A inação resultará em comprometimento permanente das fontes de água subterrânea, com impactos irreversíveis para gerações futuras.'
WHERE id = 4;

-- Relatório 5: Relatório Executivo - Crise Ambiental Nacional
UPDATE relatorios SET briefing_detalhado = 
'🔒 DOCUMENTO ULTRASSECRETO 🔒
GABINETE DO ROLE_MINISTRO DO MEIO AMBIENTE
CLASSIFICAÇÃO: ULTRASSECRETO
ACESSO RESTRITO: ROLE_MINISTRO E ASSESSORIA DIRETA

PANORAMA ESTRATÉGICO DA CRISE AMBIENTAL
Análise Confidencial - Agosto/Setembro 2024

SUMÁRIO EXECUTIVO PARA DECISÃO MINISTERIAL

A presente análise consolida informações classificadas sobre a situação ambiental nacional relacionada ao uso de agrotóxicos, com foco em implicações políticas, econômicas e estratégicas para o governo.

══════════════════════════════════════════════════

I. MAGNITUDE DA CRISE

DIMENSÃO AMBIENTAL
• 71% dos aquíferos do Cerrado contaminados
• 67% dos rios e lagos com resíduos acima do limite
• 847.234 toneladas de agrotóxicos aplicadas em 7 meses
• 23.456 hectares embargados por contaminação grave

DIMENSÃO DE SAÚDE PÚBLICA
• 478.000 pessoas consumindo água contaminada
• +34% em casos de intoxicação aguda (2023-2024)
• +28% em malformações congênitas em áreas críticas
• 12 municípios em situação de emergência sanitária

DIMENSÃO ECONÔMICA
• Agronegócio representa 27% do PIB nacional
• Setores afetados movimentam R$ 2,1 trilhões/ano
• Multas aplicadas: R$ 127,8 milhões (apenas 3º trimestre)
• Custo de remediação estimado: R$ 12 bilhões

══════════════════════════════════════════════════

II. ANÁLISE DE ATORES E INTERESSES

BANCADA RURALISTA (209 deputados, 31 senadores)
Posicionamento: Contrários a restrições adicionais
Argumentos: Segurança alimentar, competitividade, emprego
Poder de pressão: ALTO
Risco político: CRÍTICO
⚠️ Articulação em curso para derrubar portarias restritivas

SETOR PRODUTIVO (Grandes Produtores)
Principais grupos:
• ABAG (Associação Brasileira do Agronegócio)
• APROSOJA (Associação dos Produtores de Soja)
• CNA (Confederação Nacional da Agricultura)
Investimento em lobby: R$ 340 milhões (2024)
Estratégia: Pressão via mídia e congressistas aliados

INDÚSTRIA QUÍMICA (Fabricantes de Agrotóxicos)
Principais empresas: Bayer, BASF, Syngenta, Corteva
Faturamento Brasil: R$ 89 bilhões (2023)
Estratégia: Contestação judicial, estudos técnicos contrários
Status: 34 ações judiciais em curso contra restrições

SOCIEDADE CIVIL E ONGs
Organizações ativas: Greenpeace, WWF, SOS Mata Atlântica
Pressão pública: CRESCENTE
Cobertura midiática: +67% em setembro
Risco reputacional do governo: ALTO

COMUNIDADE CIENTÍFICA
Posicionamento: Unanimidade pela restrição
Base: 234 estudos publicados em 2024
Credibilidade: ELEVADA
Influência política: MODERADA

MINISTÉRIO DA AGRICULTURA
Posicionamento: Contrário a restrições severas
Argumentação: Impacto na produção, falta de alternativas
Conflito interministerial: ATIVO
⚠️ Risco de crise institucional

══════════════════════════════════════════════════

III. CENÁRIOS POLÍTICOS E ESTRATÉGIAS

CENÁRIO 1: INAÇÃO (Probabilidade 35%)
Descrição: Manter status quo, fiscalização pontual

VANTAGENS:
✓ Evita confronto com bancada ruralista
✓ Mantém relação com setor produtivo
✓ Não afeta produção agrícola

DESVANTAGENS:
✗ Agravamento da crise ambiental e sanitária
✗ Responsabilização futura do governo
✗ Perda de credibilidade internacional
✗ Ações judiciais contra omissão (MPF já notificou)

RISCO POLÍTICO: ALTO (médio prazo)
RECOMENDAÇÃO: NÃO RECOMENDADO

---

CENÁRIO 2: RESTRIÇÃO GRADUAL (Probabilidade 45%)
Descrição: Implementação progressiva de restrições em 3 anos

MEDIDAS:
• Ano 1: Banimento de 5 produtos mais críticos
• Ano 2: Restrição em zonas de recarga hídrica
• Ano 3: Redução de 30% no uso geral

VANTAGENS:
✓ Tempo para adaptação do setor
✓ Minimiza impacto produtivo imediato
✓ Demonstra ação governamental
✓ Menor resistência política

DESVANTAGENS:
✗ Pode ser insuficiente para reverter contaminação
✗ Pressão contínua de ambos os lados
✗ Risco de judicialização do processo

RISCO POLÍTICO: MÉDIO
RECOMENDAÇÃO: VIÁVEL COM ARTICULAÇÃO

---

CENÁRIO 3: AÇÃO DRÁSTICA IMEDIATA (Probabilidade 20%)
Descrição: Banimento emergencial e restrições severas

MEDIDAS:
• Banimento imediato de 15 produtos
• Embargo de áreas críticas
• Multas elevadas e responsabilização criminal

VANTAGENS:
✓ Resposta efetiva à crise
✓ Proteção imediata da saúde pública
✓ Alinhamento com comunidade científica
✓ Protagonismo internacional

DESVANTAGENS:
✗ Confronto direto com bancada ruralista
✗ Risco de impeachment ou CPI
✗ Impacto econômico de curto prazo
✗ Crise política institucional

RISCO POLÍTICO: CRÍTICO
RECOMENDAÇÃO: APENAS COM AMPLA COALIZÃO

══════════════════════════════════════════════════

IV. ARTICULAÇÕES POLÍTICAS EM CURSO

INFORMAÇÕES CONFIDENCIAIS:

1. Senador [NOME OMITIDO] (MT) - Presidente Comissão Agricultura
   Recebeu R$ 3,4 milhões em doações de empresas do setor
   Preparando PL para flexibilizar uso de agrotóxicos
   Status: Articulação avançada, previsão votação nov/2024

2. Governadores Centro-Oeste (Reunião 12/09/2024)
   Acordo firmado para pressionar contra restrições
   Ameaça de não apoio em pautas prioritárias do governo
   Estratégia: Usar argumento de "fome e desemprego"

3. Mídia e Opinião Pública
   Campanha publicitária pró-agrotóxicos: R$ 45 milhões
   Mensagem: "Defensivos garantem comida na mesa"
   Alcance estimado: 89 milhões de pessoas

4. Judiciário
   STF com 3 processos sobre competência ambiental
   Tendência: Favorável a restrições (6x5 na última votação)
   Janela de oportunidade: Março 2025

══════════════════════════════════════════════════

V. RECOMENDAÇÃO ESTRATÉGICA DO GABINETE

PROPOSTA: CENÁRIO 2 MODIFICADO (Restrição Gradual Acelerada)

FASE 1 - IMEDIATA (Outubro 2024):
□ Banimento emergencial de 3 produtos mais críticos
  (Paraquat, 2,4-D, Carbofurano)
□ Criação de Grupo de Trabalho Interministerial
□ Diálogo com setor produtivo (negociar alternativas)

FASE 2 - CURTO PRAZO (Nov 2024 - Mar 2025):
□ Publicar Portaria de Zonas de Proteção Hídrica
□ Intensificar fiscalização nas 50 propriedades críticas
□ Lançar Programa de Agricultura de Baixo Impacto
□ Articular apoio no Congresso para blindar medidas

FASE 3 - MÉDIO PRAZO (Abr 2025 - Dez 2025):
□ Banimento adicional de 5 produtos
□ Incentivos fiscais para agricultura orgânica
□ Revisão completa dos LMR (Limites Máximos)
□ Sistema nacional de monitoramento em tempo real

FASE 4 - LONGO PRAZO (2026-2027):
□ Meta de redução de 40% no uso de agrotóxicos
□ Programa de remediação de áreas contaminadas
□ Investimento em pesquisa de biodefensivos

CUSTO POLÍTICO ESTIMADO:
• Resistência bancada ruralista: ALTA
• Apoio sociedade civil: ALTA
• Impacto eleitoral 2026: NEUTRO/POSITIVO (depende comunicação)

CUSTO FINANCEIRO:
• Investimento governo: R$ 4,8 bilhões (4 anos)
• Compensações setor: R$ 2,1 bilhões
• Total: R$ 6,9 bilhões

ARTICULAÇÃO NECESSÁRIA:
• Ministério da Agricultura (acordo fundamental)
• Governadores (pelo menos 3 do Centro-Oeste)
• Lideranças Congresso (50 votos garantidos)
• Comunicação massiva para opinião pública

══════════════════════════════════════════════════

VI. PRAZOS E DECISÕES REQUERIDAS

⏰ URGENTE - ATÉ 15/OUTUBRO:
Decisão sobre banimento dos 3 produtos críticos

⏰ IMPORTANTE - ATÉ 30/OUTUBRO:
Definição da composição do GT Interministerial

⏰ ESTRATÉGICO - ATÉ 15/NOVEMBRO:
Reunião com governadores do Centro-Oeste

══════════════════════════════════════════════════

CONCLUSÃO

A crise ambiental relacionada ao uso de agrotóxicos atingiu ponto crítico que demanda decisão ministerial estratégica. A inação não é opção viável dado o agravamento dos indicadores e pressão jurídica (MPF). Recomenda-se abordagem gradual mas firme, com articulação política intensa e comunicação eficaz para opinião pública.

O momento político é desafiador mas existe janela de oportunidade se houver habilidade na negociação com setor produtivo e firmeza nas medidas de proteção ambiental e saúde pública.

Aguardo orientação de Vossa Excelência para dar seguimento às articulações propostas.

Respeitosamente,
Assessoria Estratégica - Gabinete do Ministro'
WHERE id = 5;

-- Relatório 6: Dossiê Grandes Produtores Irregulares
UPDATE relatorios SET briefing_detalhado = 
'🔒 DOSSIÊ CONFIDENCIAL 🔒
CLASSIFICAÇÃO: ULTRASSECRETO
OPERAÇÃO: CERRADO LIMPO
DIRETORIA DE INTELIGÊNCIA AMBIENTAL

DOSSIÊ: GRANDES PRODUTORES COM USO IRREGULAR DE AGROTÓXICOS
Investigação Período: Junho a Setembro 2024

══════════════════════════════════════════════════

CONTEXTO DA INVESTIGAÇÃO

A Operação Cerrado Limpo foi deflagrada em junho/2024 com objetivo de identificar e documentar grandes produtores rurais envolvidos em uso sistemático e deliberado de agrotóxicos proibidos, aplicação irregular e crimes ambientais graves.

METODOLOGIA:
• Cruzamento de dados fiscais (IBAMA, Receita Federal)
• Análise de imagens satelitais (aplicação aérea)
• Fiscalizações in loco (84 propriedades)
• Interceptação de comunicações (autorizações judiciais)
• Análise de compras irregulares (rastreamento de origem)

══════════════════════════════════════════════════

PERFIL DOS INVESTIGADOS

UNIVERSO:
• 847 propriedades fiscalizadas
• 126 grandes produtores (área >5.000 hectares)
• 43 identificados com irregularidades graves
• 18 sob investigação criminal ativa

CARACTERÍSTICAS COMUNS:
• Área média: 12.340 hectares
• Faturamento médio: R$ 87 milhões/ano
• Exportadores diretos: 89%
• Conexões políticas: 67% (doações de campanha)

══════════════════════════════════════════════════

RANKING DOS 10 MAIORES INFRATORES

═══ POSIÇÃO 1 ═══
GRUPO AGRO IMPÉRIO S/A
CNPJ: [OMITIDO] | Sócios: [OMITIDO]
Propriedades: 7 fazendas (89.340 hectares total)
Localização: MT (4), GO (2), BA (1)
Faturamento 2023: R$ 1,2 bilhões

INFRAÇÕES DOCUMENTADAS:
⚠️ Uso de Paraquat (banido): 4.567 litros apreendidos
⚠️ Aplicação aérea irregular: 23 operações detectadas
⚠️ Contaminação Rio das Mortes: Laudo técnico confirmou
⚠️ Trabalho análogo à escravidão: 34 trabalhadores resgatados

MULTAS APLICADAS: R$ 12,8 milhões
PROCESSOS: 3 criminais, 7 cíveis
CONEXÕES POLÍTICAS: Doou R$ 3,4 mi para 12 parlamentares

EVIDÊNCIAS:
• Interceptação telefônica (Proc. 2024.xxx): Discussão sobre compra clandestina de Paraquat
• Imagens de satélite: Aplicação em área embargada (Set/2024)
• Laudos laboratoriais: Contaminação comprovada

STATUS: Processo criminal em andamento - Justiça Federal

---

═══ POSIÇÃO 2 ═══
FAZENDAS REUNIDAS OURO VERDE LTDA
CNPJ: [OMITIDO] | Sócio Principal: [OMITIDO] - Ex-Secretário Agricultura
Propriedades: 5 fazendas (67.890 hectares)
Localização: GO (3), BA (2)
Faturamento 2023: R$ 840 milhões

INFRAÇÕES:
⚠️ Estoque ilegal de 2,4-D: 3.234 litros (produto proibido)
⚠️ Aplicação em APP: 890 hectares em área protegida
⚠️ Descarte criminoso: 2.340 embalagens em corpo hídrico
⚠️ Corrupção ativa: Tentativa de suborno a fiscal (R$ 50 mil)

MULTAS: R$ 8,9 milhões
CONEXÕES: Ex-secretário estadual, deputado federal (filho do sócio)

SITUAÇÃO CRÍTICA:
Interceptação revelou articulação para pressionar cancelamento de multas via influência política. Deputado [NOME OMITIDO] contatou diretor do IBAMA solicitando "revisão técnica".

---

═══ POSIÇÃO 3 ═══
AGROPECUÁRIA GRANDE HORIZONTE
CNPJ: [OMITIDO]
Propriedades: 4 fazendas (52.340 hectares)
Localização: MT (3), MS (1)
Faturamento: R$ 670 milhões

INFRAÇÕES:
⚠️ Carbofurano (banido): 890 litros encontrados
⚠️ Aplicação em manancial: Contaminação de 3 nascentes
⚠️ Fraude em licenças ambientais: Documentos falsificados
⚠️ Ameaça a fiscal: Investigação criminal

GRAVIDADE ESPECIAL:
Contaminação causou mortandade de 12.000 peixes no Rio Teles Pires
Água de 2 comunidades indígenas afetada
Caso ganhou repercussão nacional em Agosto/2024

---

═══ POSIÇÕES 4-10 ═══
Dados completos no Anexo Confidencial (páginas 78-145)
Total de multas (Top 10): R$ 47,3 milhões
Área total fiscalizada: 456.789 hectares
Processos criminais: 23 ativos

══════════════════════════════════════════════════

MODUS OPERANDI IDENTIFICADO

1. COMPRA CLANDESTINA
Esquema de importação ilegal via Paraguai e Bolívia
Produtos entram por fronteira seca, sem fiscalização
Volume estimado: 23.000 litros/ano (produtos banidos)
Valor de mercado: R$ 34 milhões

2. CORRUPÇÃO DE FISCAIS
18 casos documentados de tentativa de suborno
Valores oferecidos: R$ 20.000 a R$ 150.000
Taxa de sucesso: 11% (2 fiscais sob investigação)

3. PRESSÃO POLÍTICA
Uso de conexões para:
• Cancelamento de multas (23 casos identificados)
• Transferência de fiscais incômodos (7 casos)
• Atraso em processos judiciais

4. LAVAGEM DE CERTIFICAÇÕES
Falsificação de laudos técnicos
Empresas de consultoria participantes: 12 identificadas
Certificações fraudulentas: 67 documentos

══════════════════════════════════════════════════

CONEXÕES POLÍTICAS - ANÁLISE DE RISCO

DOAÇÕES DE CAMPANHA (2020-2022):
Total doado pelos investigados: R$ 23,4 milhões
Beneficiários: 67 candidatos (45 eleitos)
• Deputados Federais: 28
• Senadores: 7
• Deputados Estaduais: 10

PARLAMENTARES COM DOAÇÕES >R$ 500.000:
[Lista omitida por segurança - Anexo Ultra-Classificado]

RISCO DE RETALIAÇÃO POLÍTICA:
• CPI contra o MMA: Risco ALTO
• Corte de orçamento: Risco MÉDIO
• Pressão via mídia: Risco ALTO

⚠️ ALERTA: 3 parlamentares contactaram Gabinete do Ministro solicitando "esclarecimentos" sobre fiscalizações

══════════════════════════════════════════════════

PROVAS E EVIDÊNCIAS COLETADAS

MATERIAIS APREENDIDOS:
• Produtos proibidos: 12.340 litros
• Documentos falsos: 234 unidades
• Equipamentos irregulares: 45 pulverizadores

LAUDOS TÉCNICOS:
• Análises de solo: 340 amostras
• Análises de água: 567 amostras
• Contaminação comprovada: 78% das amostras

INTERCEPTAÇÕES:
• Autorizações judiciais: 12 processos
• Conversas gravadas: 890 horas
• Evidências criminais: 67 trechos incriminatórios

TESTEMUNHAS:
• Trabalhadores rurais: 145 depoimentos
• Fiscais: 34 relatos
• Moradores locais: 89 denúncias

══════════════════════════════════════════════════

IMPACTOS AMBIENTAIS DOCUMENTADOS

CONTAMINAÇÃO HÍDRICA:
• Rios afetados: 23
• Nascentes comprometidas: 67
• Aquíferos contaminados: 12 pontos

MORTANDADE DE FAUNA:
• Peixes: 45.678 indivíduos (7 eventos)
• Abelhas: Colapso de 234 colmeias
• Aves: 890 indivíduos encontrados mortos

DANOS À SAÚDE HUMANA:
• Intoxicações agudas: 67 casos registrados
• Comunidades afetadas: 12 (população total: 8.900)
• Internações hospitalares: 23 casos graves

══════════════════════════════════════════════════

AÇÕES JUDICIAIS EM CURSO

ESFERA CRIMINAL:
• Processos ativos: 23
• Denúncias apresentadas: 18
• Réus: 34 pessoas físicas
• Crimes: Ambientais, corrupção, associação criminosa

ESFERA CÍVEL:
• Ações civis públicas: 45
• Ações de reparação: 89
• Valor total das ações: R$ 340 milhões

MEDIDAS CAUTELARES:
• Embargos: 23.456 hectares
• Bloqueio de contas: R$ 127 milhões
• Suspensão de atividades: 12 propriedades

══════════════════════════════════════════════════

RECOMENDAÇÕES ESTRATÉGICAS

CURTO PRAZO (Imediato):
1. Intensificar fiscalização nas propriedades Top 10
2. Solicitar prisões preventivas (5 casos mais graves)
3. Blindagem de fiscais ameaçados
4. Articulação com Polícia Federal

MÉDIO PRAZO (3-6 meses):
1. Expansão da Operação Cerrado Limpo
2. Criação de força-tarefa específica
3. Monitoramento por satélite em tempo real
4. Sistema de denúncias protegidas

LONGO PRAZO (1-2 anos):
1. Reforma do sistema de licenciamento
2. Endurecimento de penas (projeto de lei)
3. Programa de proteção a testemunhas
4. Capacitação de fiscais em investigação

══════════════════════════════════════════════════

CONSIDERAÇÕES FINAIS

Este dossiê revela esquema sistemático e organizado de descumprimento da legislação ambiental por grandes produtores rurais. A situação vai além de infrações isoladas, configurando verdadeira associação para práticas criminosas.

A conexão com agentes políticos e tentativas de corrupção demonstram necessidade de abordagem coordenada envolvendo múltiplas instituições (PF, MPF, CGU).

O risco político é elevado, mas a gravidade dos crimes e impactos documentados justificam ação firme e coordenada do Estado.

Recomenda-se elevação do caso para Presidência da República e articulação com AGU para estratégia jurídica robusta.

SIGILO: Este documento é classificado como ULTRASSECRETO
Acesso restrito: Ministro, Secretários Executivos, Diretoria de Inteligência
Cópias controladas: 3 unidades
Violação sujeita a sanções previstas na Lei de Acesso à Informação'
WHERE id = 6;

-- Inserir novos relatórios com briefings detalhados

-- Relatório 7: ROLE_PERFIL_1 - Legislação Ambiental
INSERT INTO relatorios (titulo, tipo, periodo_inicio, periodo_fim, escopo, resumo, confidencialidade, autor, matricula_autor, status, tags, published_at, briefing_detalhado) VALUES
('Compilação da Legislação de Agrotóxicos - 2024', 'TECNICO', '2024-01-01', '2024-09-30', 'Nacional', 'Consolidação de toda legislação federal vigente sobre registro e uso de agrotóxicos', 'PUBLICO', 'João Oliveira', 'USR001', 'PUBLICADO', 'legislacao,normas,regulamentacao', '2024-09-28 11:00:00',
'GUIA COMPLETO DA LEGISLAÇÃO DE AGROTÓXICOS NO BRASIL

Este documento consolida toda a legislação federal vigente sobre registro, comercialização e uso de agrotóxicos no território nacional, atualizado até setembro de 2024.

═══ MARCO LEGAL PRINCIPAL ═══

LEI Nº 7.802/1989 - Lei dos Agrotóxicos
Dispositivo fundamental que regulamenta toda a cadeia de agrotóxicos

Principais determinações:
• Registro obrigatório em três órgãos: MAPA, ANVISA, IBAMA
• Proibição de produtos cancerígenos, teratogênicos e mutagênicos
• Responsabilidade do fabricante por danos à saúde e meio ambiente
• Receituário agronômico obrigatório
• Sistema de fiscalização e penalidades

DECRETO Nº 4.074/2002
Regulamenta a Lei 7.802/1989

Estabelece:
• Procedimentos para registro de produtos
• Classificação toxicológica (I a IV)
• Rótulos e bulas obrigatórias
• Controles de comercialização
• Sistema de logística reversa de embalagens

═══ LEGISLAÇÃO COMPLEMENTAR ═══

PORTARIA IBAMA Nº 84/1996
• Avaliação de impacto ambiental
• Classificação de potencial de periculosidade

RESOLUÇÃO ANVISA RDC 296/2019
• Reavaliação de ingredientes ativos
• Limites máximos de resíduos (LMR)

INSTRUÇÃO NORMATIVA MAPA Nº 42/2022
• Critérios para uso em agricultura orgânica
• Restrições em áreas específicas

═══ PRODUTOS PROIBIDOS (Atualizados) ═══

2020: Paraquat (banimento total)
2023: 2,4-D em formulações específicas
2024: Carbofurano, Aldicarbe, Metamidofós

Lista completa: 47 ingredientes ativos banidos
Consulta: www.gov.br/agricultura/agrotoxicos

═══ PENALIDADES ═══

Uso de produto não registrado: Multa R$ 500 a R$ 1.000.000
Aplicação irregular: Multa R$ 1.000 a R$ 50.000
Crime ambiental: Reclusão de 1 a 4 anos

Para consultar a legislação completa e atualizada, acesse o portal oficial do IBAMA.');

-- Relatório 8: ROLE_PERFIL_1 - Estatísticas Públicas
INSERT INTO relatorios (titulo, tipo, periodo_inicio, periodo_fim, escopo, resumo, confidencialidade, autor, matricula_autor, status, tags, published_at, briefing_detalhado) VALUES
('Estatísticas Nacionais de Agrotóxicos 2024', 'AGREGADO', '2024-01-01', '2024-08-31', 'Nacional', 'Dados consolidados sobre registro, comercialização e fiscalização de defensivos agrícolas', 'PUBLICO', 'Maria Costa', 'DIR002', 'PUBLICADO', 'estatisticas,dados,public', '2024-09-25 14:00:00',
'ANUÁRIO ESTATÍSTICO - AGROTÓXICOS NO BRASIL 2024

Dados oficiais consolidados pelo Ministério do Meio Ambiente em parceria com IBAMA, ANVISA e MAPA.

═══ REGISTRO DE PRODUTOS ═══

PRODUTOS REGISTRADOS (Total):
2024: 2.847 produtos comerciais
2023: 2.634 produtos
Crescimento: +8,1%

Por categoria:
• Herbicidas: 1.234 (43%)
• Fungicidas: 789 (28%)
• Inseticidas: 624 (22%)
• Outros: 200 (7%)

NOVOS REGISTROS (Jan-Ago 2024):
Total: 213 novos produtos
• Ingredientes ativos inéditos: 12
• Produtos genéricos: 156
• Produtos biológicos: 45 (+34% vs 2023)

REAVALIAÇÕES CONCLUÍDAS:
• Produtos reavaliados: 67
• Mantidos: 34
• Restritos: 21
• Banidos: 12

═══ COMERCIALIZAÇÃO ═══

VOLUME TOTAL COMERCIALIZADO:
2024 (Jan-Ago): 847.234 toneladas
2023 (mesmo período): 693.456 toneladas
Crescimento: +22,2%

FATURAMENTO DO SETOR:
2024: R$ 89,4 bilhões (projetado)
2023: R$ 76,3 bilhões
Crescimento: +17,2%

IMPORTAÇÕES:
Volume: 234.567 toneladas
Origem principal: China (67%), EUA (18%), Europa (15%)

═══ FISCALIZAÇÃO ═══

OPERAÇÕES REALIZADAS:
Total de fiscalizações: 12.847
Propriedades vistoriadas: 8.934
Estabelecimentos comerciais: 3.913

AUTUAÇÕES:
Autos de infração lavrados: 1.234
Valor total de multas: R$ 127,8 milhões
Multa média: R$ 103.562

PRODUTOS APREENDIDOS:
Volume total: 45.678 litros/kg
Produtos falsificados: 12.340 litros
Produtos contrabandeados: 18.234 litros

═══ LOGÍSTICA REVERSA ═══

EMBALAGENS DEVOLVIDAS:
Total: 34.567 toneladas (87% do consumido)
Unidades de recebimento: 456 em todo Brasil
Taxa de reciclagem: 91%

═══ IMPACTOS AMBIENTAIS ═══

ÁREAS MONITORADAS:
Pontos de monitoramento hídrico: 1.200
Análises realizadas: 14.400
Taxa de conformidade: 33%

OCORRÊNCIAS AMBIENTAIS:
Contaminações documentadas: 234
Mortandade de fauna: 89 eventos
Intoxicações humanas: 1.567 casos

Para mais informações: www.gov.br/mma/agrotoxicos');

-- Atualizar a sequência
SELECT setval('relatorios_id_seq', 20);
