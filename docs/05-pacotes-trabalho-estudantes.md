# 05 · Pacotes de trabalho dos grupos

Cada grupo recebe um **pacote** com funcionalidades de **criticidades variadas** (ao menos
uma P0). Todos trabalham sobre o **mesmo** VidaPlus, o que permite comparar soluções e
integrar as correções num só pipeline.

Cada pacote atravessa **todas as fases do SSDLC** para as suas funcionalidades:
requisito → LINDDUN/STRIDE → design → correção (PR) → teste → evidência → rastreabilidade.

> Sugestão para turma de ~4 grupos. Ajuste conforme o número de equipes. Funcionalidades
> transversais (F-05 auditoria, F-10 segredos, F-17 CORS/headers, F-21 SCA) são
> **compartilhadas**: um grupo "dono" implementa e os demais consomem.

---

## Grupo A — Identidade & Acesso

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-01 Autenticação (login) | 🔴 P0 | 🛠️ |
| F-11 Sessão / token | 🟠 P1 | 🛠️ |
| F-12 Recuperação de senha | 🟠 P1 | 🛠️ |
| F-03 RBAC (co-dono com Grupo B) | 🔴 P0 | 🛠️ |

**Foco teórico puxado:** Auth Failures, hashing (Argon2id), rate limiting, JWT/sessão,
enumeração, anti-brute-force.

---

## Grupo B — Prontuário & Autorização

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-02 Visualização/download de laudo (IDOR) | 🔴 P0 | 🛠️ |
| F-03 RBAC (co-dono com Grupo A) | 🔴 P0 | 🛠️ |
| F-07 Publicação de laudo | 🟠 P1 | 🛠️ |
| F-08 Armazenamento de PDFs | 🟠 P1 | 🛠️⚖️ |

**Foco teórico puxado:** Broken Access Control, IDOR, RBAC deny-by-default,
integridade/hash de laudo, URLs assinadas.

---

## Grupo C — Privacidade & LGPD (DPO)

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-04 Gestão de consentimento | 🔴 P0 | ✨⚖️ |
| F-06 Eliminação / anonimização | 🔴 P0 | ✨⚖️ |
| F-09 Exportação (portabilidade) | 🟠 P1 | ✨⚖️ |
| F-15 Minimização no cadastro | 🟡 P1 | ⚖️ |

**Foco teórico puxado:** LGPD arts. 8, 9, 11, 18; **LINDDUN**; minimização,
pseudonimização, anonimização, retenção; RIPD/DPIA.

---

## Grupo D — Injeção, Dados & Robustez

| Func. | Criticidade | Ação |
|-------|:-----------:|:----:|
| F-13 Busca de pacientes (SQLi) | 🟡 P1 | 🛠️ |
| F-14 Notas clínicas (XSS) | 🟡 P1 | 🛠️ |
| F-16 Tratamento de erros | 🟡 P2 | 🛠️ |
| F-18 Notificação com link | 🟡 P1 | 🛠️⚖️ |

**Foco teórico puxado:** Injection (SQLi), consultas parametrizadas, XSS e
sanitização/output encoding, erro seguro, links seguros.

---

## Transversais (donos rotativos / compartilhadas)

| Func. | Criticidade | Dono sugerido |
|-------|:-----------:|---------------|
| F-05 Trilha de auditoria | 🔴 P0 | Grupo B (consumida por todos) |
| F-10 Gestão de segredos | 🟠 P1 | DevSecOps de cada grupo |
| F-17 CORS / headers de segurança | 🟡 P1 | Grupo A |
| F-19 Perfil do usuário | 🟢 P2 | Grupo D |
| F-20 Conteúdo institucional | 🟢 P2 | Grupo D |
| F-21 Dependências (SCA) | 🟢 P2 | DevSecOps de cada grupo |

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
