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
| F-11 | **Sessão / token** | Token sem expiração nem rotação; segredo fixo no código; "logout" só limpa o navegador | 🛠️ | Session hijacking |
| F-12 | **Recuperação de senha** | Token previsível, sem expiração, resposta que enumera e-mail | 🛠️ | Account takeover, enumeração |
| F-22 | **Cifra de dados em repouso** | `cpf`, `exame.resultado` e `nota_clinica.conteudo` gravados em texto claro | 🛠️ | Disclosure em caso de vazamento do banco/backup (art. 46; ASVS V6) |
| F-23 | **Painel estatístico (indicadores agregados)** | Não existe | ✨⚖️ | Inferência individual a partir de agregados (LINDDUN `L.2.2.3`); arts. 6º III, 12 |

> ⚠️ **F-07 e F-08 não são independentes.** O status de "publicado" (F-07) é um **rótulo
> de fluxo de trabalho** no banco — ele não controla o acesso ao arquivo (F-08). No
> legado, um exame **ainda em rascunho** (`publicado = 0`, ex.: sorologia não liberada)
> é baixável do mesmo jeito, sem nenhuma autenticação. Ao escrever o RGS de F-08, teste
> justamente um exame **não publicado**: se ele também vazar, corrigir só um dos dois
> (RBAC na publicação **ou** acesso ao arquivo) deixa a lacuna real — os dois requisitos
> precisam ser corrigidos e verificados **juntos**.

---

## 🟡 Criticidade MÉDIA (P1/P2)

| ID | Funcionalidade | Estado no legado | Ação | Falhas / motivação |
|----|----------------|------------------|:----:|--------------------|
| F-13 | **Busca de pacientes (nome/CPF)** | Query por concatenação | 🛠️ | SQL Injection |
| F-14 | **Notas/observações clínicas** | Renderizadas com `dangerouslySetInnerHTML` (React) | 🛠️ | Stored XSS |
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
| F-21 | **Gestão de dependências (SCA/SBOM)** | Manter dependências atualizadas e sem CVE crítica (higiene contínua) | ⚙️ | Supply chain (OWASP A06/A03) |

---

## Mapa criticidade → threat modeling → LGPD

| Funcionalidade | STRIDE dominante | LINDDUN dominante | Base LGPD |
|----------------|------------------|-------------------|-----------|
| F-01 Login | Falsificação de identidade (Spoofing) | Identificabilidade (`I.1.1` — e-mail reutilizado como identificador único) | art. 46 |
| F-02 Laudo (IDOR) | Divulgação de informação (Information Disclosure) / Elevação de privilégio | Divulgação + Vinculação (Linking, `L.1.1` — ID sequencial do exame) | arts. 11, 46 |
| F-03 RBAC | Elevação de privilégio | Não-conformidade (`Nc.3` — ausência de controle de acesso interno) | art. 46 |
| F-04 Consentimento | — | Incapacidade de intervenção (Unintervenability, `U.2.1` — sem mecanismo de consentimento) | arts. 8, 9 |
| F-05 Auditoria | Repúdio | Não-repúdio (`Nr.2`) | arts. 37, 46 |
| F-06 Eliminação | — | Não-conformidade (`Nc.1.1.4` — retenção além do necessário) | art. 18 |
| F-13 Busca | Adulteração (Tampering) | Identificabilidade (`I.2.3` — filtro estreito isola um único registro) | art. 46 |
| F-15 Cadastro | — | Divulgação (`DD.2.1`, volume) + Desconhecimento (Unawareness) + Não-conformidade | art. 6 (minimização) |
| F-19 Perfil | — | Incapacidade de retificação/acesso (Unintervenability, `U.2.2`/`U.2.3`) | art. 18 |
| F-22 Cifra em repouso | Divulgação de informação (Information Disclosure) | Divulgação (`DD`) — dado legível em backup/dump | art. 46 |
| F-23 Painel estatístico | — | Vinculação (Linking, `L.2.2.3` — inferência por consulta agregada) | arts. 6º III, 12 |

> Os nós entre parênteses (ex.: `I.2.3`, `Nc.3`) referenciam a árvore de ameaças oficial do
> LINDDUN — mais específicos que a categoria de topo, úteis para justificar a escolha na
> defesa. Ver `docs/03` para os exemplos completos e a distinção Desconhecimento ×
> Incapacidade de intervenção (as duas metades do "U").
>
> **Por que "Divulgação" e "Adulteração" aparecem com o nome em inglês ao lado?** As
> letras do STRIDE e do LINDDUN vêm das palavras em **inglês** (Spoofing, Tampering,
> Information Disclosure...). Quando a tradução para português não começa com a mesma
> letra do acrônimo, o nome original acompanha entre parênteses.

> ⚠️ **Duas falhas que parecem uma só, mas não são.** (1) F-13: corrigir a injeção de SQL da
> busca **não** resolve o problema de um filtro estreito devolver um registro único e
> completo — são requisitos e testes separados (ver `docs/03`). (2) "Desativar" uma conta
> (ex.: o toggle do ADMIN em F-03) só bloqueia login — **não é** a eliminação exigida pelo
> F-06; os dados do titular continuam intactos e processados.

> Cada funcionalidade é um "pacote de trabalho" que atravessa **todas as fases do SSDLC**
> (requisito → threat model → design → correção → teste → evidência). A distribuição entre
> os grupos está em [05-pacotes-trabalho-estudantes.md](05-pacotes-trabalho-estudantes.md).

---

## Lacunas identificadas, fora do catálogo formal (não implementadas, mas necessárias)

> Estas lacunas são reais e exigidas por lei (mesmo critério do "✨ Criar", linha 6) —
> mas **não** entram como funcionalidade numerada (F-XX): não alteram os 5 pacotes já
> balanceados nem a rubrica. Ficam aqui como referência; um grupo pode citá-las na
> análise/dossiê como observação, **sem obrigação de implementar ou entregar prova de
> conceito**.

| Lacuna | Por que é necessária | Onde foi identificada |
|--------|------------------------|------------------------|
| **Canal de comunicação com o titular / encarregado** | LGPD **art. 41** exige um encarregado (DPO) e um canal para o titular se comunicar — inclusive para relatar **suspeita de uso indevido dos próprios dados** por um funcionário (ex.: acesso a prontuário sem necessidade). O VidaPlus não tem nada disso hoje. | Cruzamento com o caso real "vazamento de rastreamento de contato" (`docs/03`, seção *Casos reais*) — acesso **legítimo** por insiders, sem invasão; um canal de denúncia é a mitigação que falta. |
| ~~Cifra de dados em repouso~~ | **Promovida a F-22** (2026-10) — ver a tabela de criticidade ALTA. | — |

> **Candidatas a versão futura.** Esta lista é o backlog natural para quando o VidaPlus
> ganhar uma nova rodada de funcionalidades planejadas (F-22 em diante) — cada lacuna
> aqui já vem com a base legal e a onde foi identificada, prontas para virar entrada
> formal do catálogo (com dono, RFS/RGS e teste) quando isso acontecer.

### Ameaças de privacidade (LINDDUN) candidatas a versão futura

> Estas **não** são controles faltando (como as duas acima) — são **ameaças de privacidade**
> extraídas do **catálogo oficial de exemplos do LINDDUN** (`linddunexamples` e
> `lindduncomplexexamples`, os PDFs de referência) que o VidaPlus **ainda não modela**. Hoje
> o app é rico em falhas **STRIDE** (IDOR, RBAC, injeção de SQL, XSS…) e pobre nas ameaças que
> **só o LINDDUN enxerga**: aquelas em que **não há invasão**, o sistema "funciona", e mesmo
> assim a privacidade do titular é atingida. Uma versão futura pode **plantá-las** como
> vulnerabilidades didáticas (novos V-XX no gabarito) **ou** um grupo pode citá-las na análise
> do próprio pacote. **Não estão plantadas hoje.**

| Ameaça de privacidade (nó LINDDUN) | Como apareceria no VidaPlus | Por que o STRIDE não a vê | Fonte (exemplo LINDDUN) |
|---|---|---|---|
| **Detectabilidade** — oráculo de existência (`D.3`) | `GET /api/exames/{id}` responder **403** quando o exame existe (é de outro) e **404** quando não existe — a diferença revela *quais* IDs existem, sem ler o conteúdo | é o **complemento** do IDOR (V-03): devolver 403 "resolve" o acesso, mas 403 ≠ 404 continua vazando a existência dos registros | *Wrong password* / *DB access denied* (`linddunexamples` §4.9, §4.11) |
| **Vinculação (Linking)** — inferência por consulta agregada (`L.2.2.3`) — **promovida a F-23** (2026-10) | um endpoint de estatística/painel (ex.: `/api/estatisticas?diagnostico=…&unidade=…&idade=…`) que devolve contagem/média **sem exigir coorte mínima** (k-anonimato): filtro estreito isola **um** paciente | **parametrizar a query (corrigir a injeção de SQL / V-01) não corrige** — a falha é o *design da consulta*, não a injeção de código | *DB Query inference* / *Salary statistics queries* (`lindduncomplexexamples` §5; `linddunexamples` §1.17) |
| **Identificabilidade** — metadados em arquivo exportado (`I.1.2`) | o PDF do laudo (`laudo-{id}.pdf`) carregar, nos **metadados do arquivo** (Author/Creator/Keywords), o nome do médico revisor, IDs internos e timestamps — dado invisível no corpo do laudo | o vazamento acontece **fora do conteúdo** que os controles de conteúdo inspecionam; só some ao *sanitizar* o arquivo | *Author information in metadata* / *Geo-tagged photos* / *Whistleblower* (`linddunexamples` §2.3; `lindduncomplexexamples` §10, §8) |
| **Divulgação de informação** — payload da notificação (`DD.4.2`) | o **texto** do e-mail/SMS/push dizer *"seu resultado de sorologia HIV está pronto"* — o dado sensível vaza para o provedor da mensagem e para a **tela de bloqueio**, mesmo com o link protegido | distinto do link sem autenticação (já coberto em F-18): aqui a sobre-exposição está no **corpo da mensagem**, sem nenhuma falha "técnica" | *Push notification payloads* (`lindduncomplexexamples` §23) |
| **Vinculação (Linking)** — enumeração por IDs sequenciais (`L.1.1`) | IDs sequenciais e previsíveis (`101, 102, 103…`) permitirem **percorrer e correlacionar** todos os exames de um mesmo titular; combina com o oráculo de existência acima para "colher" a base | STRIDE vê o acesso **individual** (IDOR), não a **correlação em massa** habilitada pela previsibilidade do identificador | *Facebook data leak* — número de telefone como identificador (`lindduncomplexexamples` §2) |
| **Não-repúdio** — auditoria que atribui demais ao titular (`Nr.2`) | a trilha de auditoria (F-05) registrar *"paciente X acessou o próprio resultado de HIV às 22h"* de forma amplamente legível — o **controle de segurança** vira ameaça de privacidade **contra o próprio titular** | inverte a intuição do STRIDE (onde não-repúdio é sempre desejável): aqui a **super-atribuição** fere a privacidade | *Whistleblower* / *E-vote* / *Logging actions* (`lindduncomplexexamples` §8, §9, §20) |

> **Por que registrar aqui.** As três primeiras (`D.3`, `L.2.2.3`, `I.1.2`) são as mais
> demonstráveis e cobrem categorias LINDDUN hoje **órfãs** no VidaPlus (Detectabilidade,
> inferência, metadados). Todas reforçam a tese central do projeto — *parametrizar a query ou
> devolver 403 não é o fim; a privacidade tem falhas que o STRIDE não vê* (ver a distinção
> F-13 em `docs/03` e as árvores de ameaça da Aula 4).
