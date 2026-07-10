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

## Definition of Done de um pacote (o que fecha uma funcionalidade)

Uma funcionalidade só está "pronta" quando:

1. Há **requisito** (RFS + RGS) no catálogo, com base legal/ameaça.
2. Há **ameaça mapeada** em STRIDE **e/ou** LINDDUN.
3. A **correção/implementação** está num PR revisado e mergeado.
4. Há **teste** que a exercita (unitário/integração/BDD) e, quando aplicável, cobertura
   por **SAST/DAST/SCA** no CI.
5. Há **evidência** real (log do CI, print, relatório) anexada.
6. A linha correspondente na **matriz de rastreabilidade** está com status "Verificado".
7. O **gate** do pipeline referente àquela classe de falha passa de 🔴 para 🟢.

> **Entrega incremental:** a cada sprint, cada grupo abre PRs e demonstra gates que
> ficaram verdes. Ao final, o VidaPlus integrado deve subir do estado "legado vulnerável"
> para "adequado", com o pipeline documentando a evolução.
