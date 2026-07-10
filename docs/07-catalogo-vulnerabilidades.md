# 07 · Catálogo de vulnerabilidades plantadas — GABARITO DO PROFESSOR

> ⚠️ **Uso interno.** Não distribuir aos estudantes. Este é o mapa das falhas plantadas de
> propósito no VidaPlus, com localização, classe, correção esperada e a fase do SSDLC em
> que devem ser tratadas.

Cada falha segue o formato: **ID · onde · classe (OWASP/CWE) · STRIDE/LINDDUN/LGPD ·
correção esperada · funcionalidade**.

---

## V-01 · SQL Injection no login e na busca
- **Onde:** `backend/.../dao/UsuarioDao.java` (login por concatenação) e
  `backend/.../dao/PacienteDao.java` (busca por nome/CPF).
- **Classe:** A03 Injection · CWE-89.
- **Lentes:** STRIDE Tampering/EoP · LGPD art. 46.
- **Exploração:** `email = ' OR '1'='1' -- ` no login; `nome = ' UNION SELECT ...` na busca.
- **Correção esperada:** `PreparedStatement`/JPA com parâmetros; validação de entrada
  (allowlist para CPF/protocolo); erro genérico.
- **Funcionalidades:** F-01, F-13.

## V-02 · Senhas com hash fraco / sem salt
- **Onde:** `backend/.../security/LegacyHash.java` (MD5) e `seed.sql` (hashes MD5).
- **Classe:** A02 Cryptographic Failures · CWE-916.
- **Lentes:** STRIDE Spoofing · LGPD art. 46.
- **Correção esperada:** Argon2id (ou bcrypt) com salt individual; migração de hashes no
  próximo login; proibir MD5/SHA-1.
- **Funcionalidade:** F-01.

## V-03 · IDOR no acesso a exame/laudo
- **Onde:** `backend/.../controller/ExameController.java` → `GET /api/exames/{id}` e
  `/api/exames/{id}/pdf` retornam sem checar dono/vínculo.
- **Classe:** A01 Broken Access Control · CWE-639.
- **Lentes:** STRIDE Info Disclosure/EoP · LINDDUN Disclosure/Linkability · LGPD arts. 11, 46.
- **Exploração:** paciente A autenticado faz `GET /api/exames/222` (de B) → 200.
- **Correção esperada:** validar server-side que o recurso pertence ao usuário
  (ou vínculo médico); senão 403 sem vazar metadados; teste anti-IDOR no CI (DAST).
- **Funcionalidades:** F-02, F-03.

## V-04 · Ausência de RBAC / autorização
- **Onde:** `ExameController.publicar()`, `PacienteController.editar()` — sem checagem de role.
- **Classe:** A01 Broken Access Control · CWE-862.
- **Lentes:** STRIDE EoP · LGPD art. 46.
- **Correção esperada:** RBAC deny-by-default (`@PreAuthorize` / filtro); só `BIOQUIMICO`
  publica; só `RECEPCAO`/`ADMIN` edita cadastro.
- **Funcionalidades:** F-03, F-07.

## V-05 · Segredos hardcoded
- **Onde:** `backend/.../resources/application.properties` (senha do MySQL) e
  `security/JwtUtil.java` (`SECRET = "vidaplus-secret-123"`).
- **Classe:** A05/A07 · CWE-798.
- **Lentes:** STRIDE Spoofing/Tampering · LGPD art. 46.
- **Correção esperada:** variáveis de ambiente / cofre; secret scanning (Gitleaks) no CI;
  rotação.
- **Funcionalidade:** F-10.

## V-06 · Sem trilha de auditoria (não-repúdio)
- **Onde:** nenhum ponto do backend registra acessos/ações sobre laudos.
- **Classe:** A09 Security Logging & Monitoring Failures · CWE-778.
- **Lentes:** STRIDE Repudiation · LINDDUN Non-repudiation · LGPD arts. 37, 46.
- **Correção esperada:** trilha append-only (hash-chain), sem PII em texto claro; registra
  login, acesso, publicação, exclusão; teste de tampering no CI.
- **Funcionalidade:** F-05.

## V-07 · JWT/sessão inseguros
- **Onde:** `security/JwtUtil.java` — token sem expiração; aceita `alg:none`; segredo fixo.
- **Classe:** A07 Identification & Auth Failures · CWE-347.
- **Lentes:** STRIDE Spoofing · LGPD art. 46.
- **Correção esperada:** expiração curta + refresh; rejeitar `alg:none`; assinatura forte;
  invalidação no logout/troca de senha.
- **Funcionalidade:** F-11.

## V-08 · Recuperação de senha fraca
- **Onde:** `controller/AuthController.java` → `/api/auth/reset` token = `id+timestamp`;
  resposta diferente para e-mail inexistente.
- **Classe:** A07 · CWE-640.
- **Lentes:** STRIDE Spoofing · LINDDUN Detectability (enumeração) · LGPD art. 46.
- **Correção esperada:** token aleatório (≥128 bits), uso único, expira em 15 min; resposta
  genérica; invalida sessões após troca.
- **Funcionalidade:** F-12.

## V-09 · Arquivos de laudo em pasta pública / nomes previsíveis
- **Onde:** `ExameController` serve `/laudos/laudo-{id}.pdf` de diretório estático.
- **Classe:** A01/A05 · CWE-548/CWE-22.
- **Lentes:** LINDDUN Disclosure · LGPD arts. 11, 46.
- **Correção esperada:** bucket/pasta privada; download por autorização (stream
  autenticado ou URL assinada de curta duração).
- **Funcionalidade:** F-08.

## V-10 · Stored XSS em notas clínicas
- **Onde:** backend salva nota sem sanitizar; `frontend/.../prontuario` renderiza com
  `[innerHTML]` / `bypassSecurityTrustHtml`.
- **Classe:** A03 Injection (XSS) · CWE-79.
- **Lentes:** STRIDE Tampering · LGPD art. 46.
- **Correção esperada:** output encoding; remover `bypassSecurityTrust`; sanitização
  server-side; CSP.
- **Funcionalidade:** F-14.

## V-11 · Erros verbosos (stack trace ao cliente)
- **Onde:** `config/` sem `@ControllerAdvice`; exceções sobem cruas.
- **Classe:** A05 · CWE-209.
- **Lentes:** STRIDE Info Disclosure.
- **Correção esperada:** handler global; mensagem genérica ao cliente; detalhe só em log
  interno.
- **Funcionalidade:** F-16.

## V-12 · CORS aberto + headers ausentes
- **Onde:** `config/WebConfig.java` → `allowedOrigins("*")` com credenciais; sem CSP/HSTS.
- **Classe:** A05 Security Misconfiguration · CWE-942.
- **Correção esperada:** origem explícita; sem `*` com credenciais; adicionar CSP, HSTS,
  X-Content-Type-Options, etc.
- **Funcionalidade:** F-17.

## V-13 · Cleartext / sem TLS
- **Onde:** frontend fala HTTP com a API; sem redirecionamento para HTTPS.
- **Classe:** A02 · CWE-319.
- **Correção esperada:** TLS ponta-a-ponta; HSTS; proxy/ingress com certificado.
- **Funcionalidade:** F-11/F-17.

## V-14 · Sobrecoleta de dados (violação de minimização)
- **Onde:** `model/Paciente.java` + `schema.sql` coletam `religiao`, `estado_civil`,
  `renda`, sem finalidade.
- **Classe:** Privacidade · LGPD art. 6 (necessidade).
- **Lentes:** LINDDUN Unawareness/Non-compliance.
- **Correção esperada:** remover campos sem finalidade; documentar finalidade dos demais;
  consentimento.
- **Funcionalidade:** F-15.

## V-15 · Sem consentimento nem base legal
- **Onde:** não há tabela/registro de consentimento.
- **Classe:** LGPD arts. 8, 9, 11.
- **Lentes:** LINDDUN Unawareness.
- **Correção esperada:** módulo de consentimento versionado e revogável; campo `base_legal`.
- **Funcionalidade:** F-04.

## V-16 · Retenção infinita / sem eliminação
- **Onde:** não há exclusão/anonimização; `ativo=0` apenas oculta.
- **Classe:** LGPD art. 18.
- **Lentes:** LINDDUN Non-compliance.
- **Correção esperada:** rotina de retenção + anonimização; direito de eliminação.
- **Funcionalidade:** F-06.

## V-17 · Logs com PII (identifiability)
- **Onde:** onde houver logging, grava CPF/nome (quando os grupos adicionarem auditoria,
  não podem repetir o erro).
- **Classe:** A09 · LGPD art. 46 · LINDDUN Identifiability.
- **Correção esperada:** `user_id` opaco; mascaramento; sem conteúdo clínico no log.
- **Funcionalidade:** F-05.

## V-18 · Dependências desatualizadas (supply chain)
- **Onde:** `pom.xml` (Spring Boot antigo, libs com CVE) e `package.json` (Angular antigo).
- **Classe:** A06 Vulnerable & Outdated Components · CWE-1104.
- **Correção esperada:** atualizar; SCA (Dependency-Check/Trivy/Dependabot) no CI;
  gerar SBOM.
- **Funcionalidade:** F-21.

---

## Matriz falha → fase do SSDLC → gate do CI

| Falha | Fase que trata | Gate do CI que fica verde |
|-------|:--------------:|---------------------------|
| V-01 SQLi | 4 | SAST (Semgrep/CodeQL) + teste de injeção |
| V-02 hash fraco | 4 | SAST + teste de auth |
| V-03 IDOR | 2→4 | DAST (ZAP) + teste anti-IDOR |
| V-04 RBAC | 4 | teste de autorização |
| V-05 segredos | 4/6 | Secret scanning (Gitleaks) |
| V-06 auditoria | 4/7 | teste de auditoria (append-only) |
| V-07 JWT | 4 | SAST + teste de sessão |
| V-08 reset | 4 | teste de reset (uso único/expiração) |
| V-09 arquivos | 4 | DAST + teste de download autorizado |
| V-10 XSS | 4 | SAST + DAST |
| V-11 erros | 4 | teste de erro genérico |
| V-12 CORS/headers | 4 | DAST (headers) |
| V-13 TLS | 6 | config/infra + DAST |
| V-14 minimização | 2→4 | revisão LINDDUN + schema |
| V-15 consentimento | 4 | teste de consentimento |
| V-16 eliminação | 4 | teste de erasure/retenção |
| V-17 logs PII | 4/7 | teste de mascaramento |
| V-18 dependências | 5/6 | SCA + SBOM |

> **Meta pedagógica:** o pipeline começa com ~18 gates vermelhos. O "placar" de gates
> verdes é o termômetro objetivo do progresso da turma ao longo do semestre.
