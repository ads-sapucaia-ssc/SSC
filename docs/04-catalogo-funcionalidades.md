# 04 · Catálogo de funcionalidades por criticidade

Cada funcionalidade tem um **estado no legado** e uma **ação esperada**:
- **Corrigir** — existe, mas é vulnerável.
- **Adequar** — existe, mas viola LGPD/privacidade.
- **Criar** — não existe e é exigida por lei/segurança.

A **criticidade** guia a priorização (P0 bloqueia release) e a distribuição entre os grupos.

> ⚠️ **Este catálogo é a superfície de ANÁLISE, não uma lista de tarefas de código.** Como
> o foco é o processo (SSDLC + LGPD + privacidade), cada grupo **corrige apenas 1–2
> controles ponta-a-ponta (prova de conceito)** e, para as demais funcionalidades do seu
> pacote, entrega **análise + plano de remediação** (ameaça, controle proposto, esforço,
> prioridade) — **sem precisar implementar**. Todas as funcionalidades devem ser
> **analisadas** (requisito, STRIDE/LINDDUN, base legal); poucas são **implementadas**.

Legenda de ação: 🛠️ corrigir · ⚖️ adequar (LGPD/privacidade) · ✨ criar.

---

## 🔴 Criticidade CRÍTICA (P0 — bloqueia o release)

| ID | Funcionalidade | Estado no legado | Ação | Falhas / motivação |
|----|----------------|------------------|:----:|--------------------|
| F-01 | **Autenticação (login)** | Senha em MD5/texto; SQL por concatenação; sem rate limit | 🛠️ | Broken Auth, SQLi, brute force |
| F-02 | **Visualização/download de laudo** | `GET /api/exames/{id}` sem checar dono | 🛠️ | **IDOR** (art. 11) |
| F-03 | **Controle de acesso por papel (RBAC)** | Endpoints sem verificação de role | 🛠️ | Broken Access Control |
| F-04 | **Gestão de consentimento** | Não existe | ✨⚖️ | LGPD arts. 8–9 |
| F-05 | **Trilha de auditoria** | Não existe | ✨ | Não-repúdio (art. 37/46) |
| F-06 | **Eliminação / anonimização de dados** | Não existe (dados ficam para sempre) | ✨⚖️ | LGPD art. 18; retenção |

---

## 🟠 Criticidade ALTA (P1 — essencial)

| ID | Funcionalidade | Estado no legado | Ação | Falhas / motivação |
|----|----------------|------------------|:----:|--------------------|
| F-07 | **Publicação de laudo pelo laboratório** | Qualquer usuário publica; sem hash/versão | 🛠️ | RBAC + integridade/não-repúdio |
| F-08 | **Armazenamento de PDFs de exame** | Pasta pública, nomes sequenciais | 🛠️⚖️ | Acesso direto a arquivo (art. 46) |
| F-09 | **Exportação de dados do titular (portabilidade)** | Não existe | ✨⚖️ | LGPD art. 18 |
| F-10 | **Gestão de segredos e configuração** | Credenciais e JWT secret no código | 🛠️ | Hardcoded secrets |
| F-11 | **Sessão / token** | Token longo, sem expiração/rotação; `alg:none` aceito | 🛠️ | Session hijacking |
| F-12 | **Recuperação de senha** | Token previsível, sem expiração, resposta que enumera e-mail | 🛠️ | Account takeover, enumeração |

---

## 🟡 Criticidade MÉDIA (P1/P2)

| ID | Funcionalidade | Estado no legado | Ação | Falhas / motivação |
|----|----------------|------------------|:----:|--------------------|
| F-13 | **Busca de pacientes (nome/CPF)** | Query por concatenação | 🛠️ | SQL Injection |
| F-14 | **Notas/observações clínicas** | Renderizadas com `innerHTML`/bypassSecurity | 🛠️ | Stored XSS |
| F-15 | **Cadastro de paciente** | Coleta religião, estado civil, etc. sem finalidade | ⚖️ | Minimização (LINDDUN-U/N) |
| F-16 | **Tratamento de erros** | Stack trace devolvido ao cliente | 🛠️ | Information disclosure |
| F-17 | **CORS / cabeçalhos de segurança** | `Allow-Origin: *` com credenciais; sem HSTS/CSP | 🛠️ | Misconfiguration |
| F-18 | **Notificação (e-mail/SMS) com link** | Link direto ao laudo, sem autenticação | 🛠️⚖️ | Disclosure |

---

## 🟢 Criticidade BAIXA (P2 — robustez/melhoria)

| ID | Funcionalidade | Estado no legado | Ação | Motivação |
|----|----------------|------------------|:----:|-----------|
| F-19 | **Perfil do usuário** | Sem validação de entrada | 🛠️ | Hardening |
| F-20 | **Conteúdo institucional (site)** | Sem proteção anti-defacement | 🛠️ | Integridade |
| F-21 | **Dependências desatualizadas** | Libs antigas com CVEs | 🛠️ | SCA (supply chain) |

---

## Mapa criticidade → threat modeling → LGPD

| Funcionalidade | STRIDE dominante | LINDDUN dominante | Base LGPD |
|----------------|------------------|-------------------|-----------|
| F-01 Login | Spoofing | Identifiability | art. 46 |
| F-02 Laudo (IDOR) | Info Disclosure / EoP | Disclosure / Linkability | arts. 11, 46 |
| F-04 Consentimento | — | Unawareness | arts. 8, 9 |
| F-05 Auditoria | Repudiation | Non-repudiation | arts. 37, 46 |
| F-06 Eliminação | — | Non-compliance | art. 18 |
| F-13 Busca | Tampering | Detectability | art. 46 |
| F-15 Cadastro | — | Unawareness / Non-compliance | art. 6 (minimização) |

> Cada funcionalidade é um "pacote de trabalho" que atravessa **todas as fases do SSDLC**
> (requisito → threat model → design → correção → teste → evidência). A distribuição entre
> os grupos está em [05-pacotes-trabalho-estudantes.md](05-pacotes-trabalho-estudantes.md).
