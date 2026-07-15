# 05 · Pacotes de trabalho dos grupos

Cada grupo recebe um **pacote** com funcionalidades de **criticidades variadas** (ao menos
uma P0). Todos trabalham sobre o **mesmo** VidaPlus, o que permite comparar soluções e
integrar as correções num só pipeline.

Cada pacote atravessa **todas as fases do SSDLC** para as suas funcionalidades:
requisito → LINDDUN/STRIDE → design → correção (PR) → teste → evidência → rastreabilidade.

> **Pacotes independentes.** Os cinco pacotes são **disjuntos e autossuficientes**: cada
> funcionalidade tem **um único dono**. O entregável de um grupo (análise de todas as suas
> funcionalidades + 1–2 provas de conceito) **nunca depende de outro grupo ter terminado** —
> depende só do próprio pacote e da base legada comum. Onde uma funcionalidade **pressupõe**
> logicamente outra de outro grupo (ex.: o módulo de consentimento roda "atrás" do RBAC, ou
> qualquer grupo se beneficiaria da trilha de auditoria), isso entra na análise como uma
> **interface/suposição documentada**, não como bloqueio: a prova de conceito é fechada de
> forma isolada (com stub/mock ou log mínimo próprio, se necessário).

---

## Grupo 1 — Identidade & Autenticação

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-01 Autenticação (login) | 🔴 P0 | 🛠️ |
| F-11 Sessão / token | 🟠 P1 | 🛠️ |
| F-12 Recuperação de senha | 🟠 P1 | 🛠️ |
| F-17 CORS / headers de segurança | 🟡 P1 | 🛠️ |

**Foco teórico puxado:** Auth Failures, hashing (Argon2id), rate limiting, JWT/sessão,
enumeração, anti-brute-force, CORS com credenciais, CSP/HSTS.

---

## Grupo 2 — Autorização & Prontuário

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-02 Visualização/download de laudo (IDOR) | 🔴 P0 | 🛠️ |
| F-03 RBAC (controle de acesso por papel) | 🔴 P0 | 🛠️ |
| F-07 Publicação de laudo | 🟠 P1 | 🛠️ |
| F-08 Armazenamento de PDFs | 🟠 P1 | 🛠️⚖️ |

**Foco teórico puxado:** Broken Access Control, IDOR, RBAC deny-by-default,
integridade/hash de laudo, URLs assinadas, acesso direto a arquivo.

---

## Grupo 3 — Consentimento & Direitos do Titular (DPO)

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-04 Gestão de consentimento | 🔴 P0 | ✨⚖️ |
| F-09 Exportação (portabilidade) | 🟠 P1 | ✨⚖️ |
| F-15 Minimização no cadastro | 🟡 P1 | ⚖️ |
| F-19 Perfil do usuário (autosserviço do titular) | 🟢 P2 | 🛠️ |

**Foco teórico puxado:** LGPD arts. 8, 9, 11, 18; **LINDDUN**; minimização,
pseudonimização, consentimento versionado/revogável, portabilidade; RIPD/DPIA.

---

## Grupo 4 — Auditoria & Prevenção de Vazamento

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-05 Trilha de auditoria | 🔴 P0 | ✨ |
| F-18 Notificação (e-mail/SMS) com link | 🟠 P1 | 🛠️⚖️ |
| F-16 Tratamento de erros | 🟡 P2 | 🛠️ |
| F-20 Conteúdo institucional (site) | 🟢 P2 | 🛠️ |

**Foco teórico puxado:** não-repúdio e trilha de auditoria (arts. 37/46), Information
Disclosure, erro seguro (sem stack trace ao cliente), links autenticados/expiráveis,
hardening/anti-defacement de conteúdo público.

---

## Grupo 5 — Injeção & Anonimização de Dados

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-06 Eliminação / anonimização | 🔴 P0 | ✨⚖️ |
| F-13 Busca de pacientes (SQLi) | 🟡 P1 | 🛠️ |
| F-14 Notas clínicas (XSS) | 🟡 P1 | 🛠️ |

**Foco teórico puxado:** Injection (SQLi) e consultas parametrizadas; XSS e
sanitização/output encoding; anonimização/pseudonimização e ciclo de vida do dado
(retenção → eliminação), respeitando a retenção legal.

---

## Higiene contínua — responsabilidade de **todos** os grupos

Não são "de um dono": **cada grupo** aplica ao **próprio** serviço/deploy, para o próprio
código. Não geram dependência entre equipes.

| Func. | Criticidade | Quem faz |
|-------|:-----------:|----------|
| F-10 Gestão de segredos e configuração | 🟠 P1 | cada grupo, no seu módulo (nada de segredo em código) |
| F-21 Gestão de dependências (SCA/SBOM) | 🟢 P2 | cada grupo, nas dependências que introduz/toca |

---

## Dois níveis de "pronto" (o foco é análise; a implementação é prova de conceito)

Como a disciplina avalia o **processo**, cada funcionalidade do pacote é **analisada**, mas
só **1–2 controles** (ao menos 1 P0) são **implementados** como prova de conceito.

### A) Funcionalidade ANALISADA — obrigatório para **todas** do pacote
1. **Requisito** (RFS + RGS) com **base legal** (LGPD) e **ameaça** (STRIDE e/ou LINDDUN).
2. **Controle proposto** com comportamento sob falha (fail-secure).
3. **Entrada na matriz de rastreabilidade** e, se de privacidade, no **RIPD**.
4. **Plano de remediação**: o quê, como, esforço estimado, prioridade.

### B) Funcionalidade IMPLEMENTADA (prova de conceito) — **1–2 por grupo**
Além de A, fecha o fio completo:
5. **Correção** num PR revisado e mergeado.
6. **Teste** que a exercita + evidência (log do CI/print/relatório).
7. Status **"Verificado"** na matriz; o **gate** da classe de falha vai de 🔴 → 🟢.

> **Por que assim:** o objetivo não é corrigir muito código, e sim **provar que o grupo
> sabe fechar o ciclo** de engenharia de segurança/privacidade — do ativo à evidência — e
> **planejar** a adequação do restante como faria num projeto real.
