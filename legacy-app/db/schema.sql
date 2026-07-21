-- =====================================================================
-- VidaPlus - schema do banco (LEGADO, ~2015)
-- MySQL 8. Contém decisões de modelagem INSEGURAS de proposito:
--   * senha_hash em MD5 (V-02)
--   * sobrecoleta: religiao, estado_civil, renda (V-14 minimizacao)
--   * ausencia de tabela de consentimento (V-15)
--   * ausencia de tabela de auditoria (V-06)
--   * exclusao apenas logica via 'ativo' -> retencao infinita (V-16)
--   * IDs sequenciais previsiveis (LINDDUN Linkability)
-- Os grupos evoluem este schema ao longo do SSDLC.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS vidaplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vidaplus;

-- ---------------------------------------------------------------------
-- Usuarios (autenticacao) - papeis: PACIENTE, MEDICO, RECEPCAO, BIOQUIMICO, ADMIN
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  email        VARCHAR(150) NOT NULL UNIQUE,
  -- V-02: hash MD5 sem salt
  senha_hash   CHAR(32)     NOT NULL,
  papel        VARCHAR(20)  NOT NULL,
  paciente_id  BIGINT       NULL,      -- vinculo quando o usuario e um paciente
  ativo        TINYINT      NOT NULL DEFAULT 1,
  -- V-08: campos de reset fracos (token previsivel, sem expiracao real)
  reset_token  VARCHAR(64)  NULL,
  criado_em    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Pacientes (PII + V-14 sobrecoleta)
-- ---------------------------------------------------------------------
CREATE TABLE paciente (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(150) NOT NULL,
  cpf            VARCHAR(14)  NOT NULL,          -- sem cifra, texto claro
  data_nascimento DATE        NULL,
  email          VARCHAR(150) NULL,
  telefone       VARCHAR(20)  NULL,
  endereco       VARCHAR(255) NULL,
  -- V-14: coletados sem finalidade -> violam minimizacao (art. 6)
  religiao       VARCHAR(50)  NULL,
  estado_civil   VARCHAR(30)  NULL,
  renda_mensal   DECIMAL(10,2) NULL,
  ativo          TINYINT      NOT NULL DEFAULT 1,  -- V-16: "exclusao" só oculta
  criado_em      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Medicos e vinculo medico-paciente (para RBAC/IDOR - F-02/F-03)
-- ---------------------------------------------------------------------
CREATE TABLE medico (
  id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  crm  VARCHAR(20)  NOT NULL,
  usuario_id BIGINT NULL
);

CREATE TABLE atendimento (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  medico_id   BIGINT NOT NULL,
  paciente_id BIGINT NOT NULL,
  data        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Exames / laudos (ativo mais critico - saude, art. 11)
-- V-09: caminho do PDF previsivel e publico
-- ---------------------------------------------------------------------
CREATE TABLE exame (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  paciente_id   BIGINT NOT NULL,
  medico_id     BIGINT NULL,               -- solicitante
  tipo          VARCHAR(80) NOT NULL,
  resultado     TEXT NULL,                  -- conteudo clinico sensivel
  laudo_pdf     VARCHAR(255) NULL,          -- V-09: '/laudos/laudo-{id}.pdf'
  publicado     TINYINT NOT NULL DEFAULT 0, -- V-04: quem publica nao e checado
  publicado_por BIGINT NULL,
  hash_laudo    CHAR(64) NULL,              -- F-07: hoje NULL (sem integridade)
  criado_em     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Notas clinicas (V-10 stored XSS: conteudo salvo sem sanitizacao)
-- ---------------------------------------------------------------------
CREATE TABLE nota_clinica (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  paciente_id BIGINT NOT NULL,
  autor_id    BIGINT NULL,
  conteudo    TEXT NOT NULL,               -- renderizado com innerHTML no front
  criado_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- TABELAS QUE NAO EXISTEM NO LEGADO (os grupos DEVEM criar):
--   * consentimento        (F-04, LGPD arts. 8/9)  -> base_legal, versao, revogado_em
--   * auditoria            (F-05, append-only, hash-chain, sem PII)
--   * solicitacao_titular  (F-06/F-09, direitos art. 18: export/erasure)
-- Modelos de referencia comentados abaixo para o professor.
--
-- Ja existe uma entity + repository Spring Data prontos para cada uma
-- destas 3 (backend/.../entity/ e .../repository/) - CRUD sem SQL escrito
-- a mao. O grupo ainda precisa CRIAR a tabela de verdade (decidir o schema
-- e rodar o CREATE TABLE) - isso continua sendo trabalho de analise/design,
-- o Spring Data so poupa a camada de acesso a dados repetitiva.
-- =====================================================================
-- CREATE TABLE consentimento (
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   paciente_id BIGINT NOT NULL,
--   finalidade VARCHAR(255) NOT NULL,
--   base_legal VARCHAR(80) NOT NULL,
--   termo_versao VARCHAR(20) NOT NULL,
--   concedido_em DATETIME NOT NULL,
--   revogado_em  DATETIME NULL
-- );
-- CREATE TABLE auditoria (
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   ts DATETIME NOT NULL,
--   evento VARCHAR(40) NOT NULL,
--   user_id BIGINT NULL,          -- id opaco, NUNCA CPF/nome (V-17)
--   recurso VARCHAR(80) NULL,
--   prev_hash CHAR(64) NOT NULL,
--   hash CHAR(64) NOT NULL
-- );
-- CREATE TABLE solicitacao_titular (      -- rascunho, NAO e solucao pronta
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   paciente_id BIGINT NOT NULL,
--   tipo VARCHAR(20) NOT NULL,             -- EXPORTACAO | ELIMINACAO
--   status VARCHAR(20) NOT NULL,           -- PENDENTE | CONCLUIDA | NEGADA
--   solicitado_em DATETIME NOT NULL,
--   concluido_em  DATETIME NULL
-- );
