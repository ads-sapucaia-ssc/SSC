# 05 · Pacotes de trabalho dos grupos

Cada grupo recebe um pacote desenhado para ser **equivalente em esforço**. Todo pacote tem:

- **1 funcionalidade P0 de correção** (🛠️) — código que **já existe** e está vulnerável,
  com **teste-semente** no pipeline: é o 🔴→🟢 objetivo do grupo;
- **1 funcionalidade de criação** (✨) — projetar e construir do zero, com **escopo mínimo
  definido** (ver "Escopo mínimo da PoC ✨", ao final);
- **2–3 funcionalidades de apoio** que fecham o tema.

Todos trabalham sobre o **mesmo** VidaPlus, o que permite comparar soluções. Cada pacote
atravessa **todas as fases do SSDLC** para as suas funcionalidades:
requisito → LINDDUN/STRIDE → design → correção (PR) → teste → evidência → rastreabilidade.

> **Pacotes independentes — mesmo quando compartilham uma funcionalidade.** Algumas
> funcionalidades aparecem em **dois pacotes**, de propósito, para equilibrar o esforço
> (há 3 P0 de correção e 4 de criação para 5 grupos). Quando isso ocorre, cada grupo tem um
> **recorte (lente) diferente e explícito**, e faz a análise e a PoC **no próprio
> repositório**: não há dependência nem espera entre grupos. Comparar as duas soluções na
> defesa é parte do valor.
>
> Onde uma funcionalidade **pressupõe** logicamente outra de outro grupo (ex.: o consentimento
> roda "atrás" do RBAC), isso entra na análise como **interface/suposição documentada**, não
> como bloqueio: a PoC é fechada de forma isolada (stub/mock ou log mínimo próprio).

---

## Grupo 1 — Identidade & Trilha de Acesso

| Func. | Criticidade | Ação | Recorte do grupo |
|-------|:-----------:|:----:|------------------|
| F-01 Autenticação (login) | 🔴 P0 | 🛠️ | *compartilhada com G5* — aqui a lente é o **fluxo de autenticação**: credencial (MD5→Argon2id), rate limit, enumeração |
| F-05 Trilha de auditoria | 🔴 P0 | ✨ | criar, com foco nos **eventos de autenticação e acesso** |
| F-11 Sessão / token | 🟠 P1 | 🛠️ | |
| F-12 Recuperação de senha | 🟠 P1 | 🛠️ | |
| F-17 CORS / headers de segurança | 🟡 P1 | 🛠️ | |

**Teste-semente:** `sqli_no_login` (a correção do login parametriza a query e o gate fecha).
**Foco teórico puxado:** Auth Failures, hashing (Argon2id), rate limiting, JWT/sessão,
enumeração, CORS com credenciais, CSP/HSTS; **não-repúdio** e trilha append-only.

---

## Grupo 2 — Prontuário: dado alheio × dado próprio

| Func. | Criticidade | Ação | Recorte do grupo |
|-------|:-----------:|:----:|------------------|
| F-02 Visualização/download de laudo (IDOR) | 🔴 P0 | 🛠️ | *compartilhada com G4* — aqui a lente é **autorização/ownership**: deny-by-default no acesso ao laudo |
| F-09 Exportação do titular (portabilidade) | 🟠 P1 | ✨⚖️ | criar — é o **espelho** do IDOR: negar o alheio, entregar o próprio |
| F-07 Publicação de laudo | 🟠 P1 | 🛠️ | |
| F-08 Armazenamento de PDFs | 🟠 P1 | 🛠️⚖️ | teste com um exame **não publicado** — ver nota em `docs/04` |

**Teste-semente:** `idor…403`.
**Foco teórico puxado:** Broken Access Control, IDOR, ownership server-side,
integridade/hash de laudo, URLs assinadas, acesso direto a arquivo; LGPD art. 18
(portabilidade) e art. 11.

---

## Grupo 3 — Papéis & Consentimento (DPO)

| Func. | Criticidade | Ação | Recorte do grupo |
|-------|:-----------:|:----:|------------------|
| F-03 RBAC (controle de acesso por papel) | 🔴 P0 | 🛠️ | deny-by-default por papel |
| F-04 Gestão de consentimento | 🔴 P0 | ✨⚖️ | criar — o consentimento roda "atrás" do RBAC: **quem pode** + **sob qual base legal** |
| F-15 Minimização no cadastro | 🟠 P1 | ⚖️ | |
| F-19 Perfil do usuário (autosserviço do titular) | 🟢 P2 | 🛠️ | |

**Teste-semente:** `rbac…403` (publicação de laudo negada a paciente).
**Foco teórico puxado:** RBAC deny-by-default; LGPD arts. 8, 9, 11, 18; **LINDDUN**;
minimização, pseudonimização, consentimento versionado/revogável; RIPD/DPIA.

---

## Grupo 4 — Vazamento & Ciclo de Vida do Dado

| Func. | Criticidade | Ação | Recorte do grupo |
|-------|:-----------:|:----:|------------------|
| F-02 Visualização/download de laudo (IDOR) | 🔴 P0 | 🛠️ | *compartilhada com G2* — aqui a lente é **canal de vazamento**: por onde o laudo escapa (link, arquivo direto, mensagem de erro) |
| F-06 Eliminação / anonimização | 🔴 P0 | ✨⚖️ | criar — lente **retenção → eliminação**: ciclo de vida e prazo legal |
| F-16 Tratamento de erros | 🟡 P2 | 🛠️ | |
| F-18 Notificação (e-mail/SMS) com link | 🟠 P1 | 🛠️⚖️ | |
| F-20 Conteúdo institucional (site) | 🟢 P2 | 🛠️ | |

**Teste-semente:** `idor…403`.
**Foco teórico puxado:** Information Disclosure, erro seguro (sem stack trace), links
autenticados/expiráveis, hardening/anti-defacement; retenção legal × eliminação (art. 18).

---

## Grupo 5 — Injeção & Anonimização de Dados

| Func. | Criticidade | Ação | Recorte do grupo |
|-------|:-----------:|:----:|------------------|
| F-01 Autenticação (login) | 🔴 P0 | 🛠️ | *compartilhada com G1* — aqui a lente é a **classe de injeção**: a mesma parametrização vale para login e busca |
| F-06 Eliminação / anonimização | 🔴 P0 | ✨⚖️ | criar — lente **anonimização/pseudonimização**: a transformação técnica do dado |
| F-13 Busca de pacientes (SQLi) | 🟡 P1 | 🛠️ | |
| F-14 Notas clínicas (XSS) | 🟡 P1 | 🛠️ | |

**Teste-semente:** `sqli_no_login`.
**Foco teórico puxado:** Injection (SQLi) e consultas parametrizadas; XSS e
sanitização/output encoding; anonimização × pseudonimização e reidentificação.

---

## Higiene contínua — responsabilidade de **todos** os grupos

Não são "de um dono": **cada grupo** aplica ao **próprio** módulo/deploy. Não geram
dependência entre equipes.

| Func. | Criticidade | Quem faz |
|-------|:-----------:|----------|
| F-10 Gestão de segredos e configuração | 🟠 P1 | cada grupo, no seu módulo (nada de segredo em código) |
| F-21 Gestão de dependências (SCA/SBOM) | 🟢 P2 | cada grupo, nas dependências que introduz/toca |

---

## Dois níveis de "pronto" (o foco é análise; a implementação é prova de conceito)

Como a disciplina avalia o **processo**, cada funcionalidade do pacote é **analisada**, mas
só **1–2 controles** são **implementados** como prova de conceito — obrigatoriamente
**o P0 de correção** e, quando o grupo optar por dois, a **funcionalidade de criação**.

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

### Escopo mínimo da PoC ✨ (criação) — para equiparar o esforço

Construir do zero custa mais que corrigir. Por isso o **mínimo aceitável é pequeno e
fechado** — não se espera o módulo completo:

| Criação | Escopo mínimo da PoC |
|---------|----------------------|
| **F-04** Consentimento | **1 finalidade**, **1 versão** e **revogação**, aplicada em **1 fluxo** |
| **F-05** Trilha de auditoria | **1 ação sensível** instrumentada + **verificação de integridade** (append-only) daquele log |
| **F-06** Eliminação / anonimização | **1 entidade**, com regra de **retenção → anonimização**, respeitando a retenção legal |
| **F-09** Portabilidade | exportação dos dados de **1 titular** em formato estruturado (JSON/CSV), **autenticada** |

> **Por que assim:** o objetivo não é corrigir muito código, e sim **provar que o grupo
> sabe fechar o ciclo** de engenharia de segurança/privacidade — do ativo à evidência — e
> **planejar** a adequação do restante como faria num projeto real.

> **E os gates dos outros pacotes?** Cada grupo trabalha no **seu próprio repositório** e
> torna bloqueante **apenas o gate da classe que corrigiu**. O pipeline **já conclui verde**
> desde o 1º push (gates informativos); o placar vermelho das funcionalidades de **outros
> pacotes** — ou das suas que ficaram só **analisadas** — é **esperado** e **não** derruba o
> build nem a nota (nota = dossiê + defesa; pipeline = evidência). Nenhum grupo precisa
> corrigir teste de outro pacote. Detalhe de como fazer isso com o job de testes de
> segurança: [08-setup-grupos.md](08-setup-grupos.md) §5.
