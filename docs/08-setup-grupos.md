# 08 · Setup dos grupos

Como o grupo **forma o repositório** no Classroom 50 e o que já vem configurado nele. O resto do
começo (instalar as ferramentas, clonar, subir o VidaPlus, Git e pipeline) está, passo a passo,
nos roteiros comuns — veja a [§2](#2-depois-de-formar-o-grupo-siga-os-roteiros-comuns).

---

## 1. Gerar o repositório do grupo

**Você (aluno) tem um caminho só:** o professor compartilha um **link de convite** do
assignment (Classroom 50); ao aceitar, o repositório **privado** do grupo é criado
**automaticamente sob a organização**, a partir do template e já com acesso **admin** — no
lugar certo (sob a org), com as permissões corretas, sem vocês precisarem criar nada à mão.

O repositório do grupo é **um só, compartilhado** (não um por pessoa). Para formá-lo:

1. **Só o líder** do grupo abre o link de convite e **aceita** — isso **cria o repositório
   compartilhado** e o deixa como **owner** (dono). Os **demais não** clicam no link de aceite
   (senão tentariam criar outro repo); eles entram como colaboradores (passos 2–5).
2. O **owner** **adiciona os demais** como **colaboradores**. Na página do assignment do grupo,
   clique no **lápis de edição**
   (canto superior direito) para abrir **Edit Assignment**:

   ![Editar o assignment do grupo no Classroom 50](img/web_assignment_edit_student.png)

3. Clique em **Manage collaborators** (gerenciar colaboradores):

   ![Janela Manage collaborators do Classroom 50](img/web_assignment_manage_collaborators_student.png)

4. Na janela **Group members**, o owner digita o **usuário do GitHub** de cada colega no campo
   *Add a GitHub username* e clica **+ Add**; ao terminar, clica **Save collaborators**.
   (Este assignment permite até **4** estudantes além do owner.)
5. Cada colega adicionado **aceita o convite da organização** da turma que chega do GitHub
   (e-mail ou `github.com/settings/organizations`) — só então ganha acesso ao repositório. Se o
   colega "não for encontrado" ao ser adicionado, ele ainda não está na organização: peça para
   aceitar o convite e autorizar o **Classroom 50** em `github.com/settings/applications`.

> Pronto: todos passam a ter acesso ao **mesmo** repositório. Cada um que for programar
> **clona** esse repositório ([`roteiros/comum/00` §4](../roteiros/comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo)).

> **Seu entregável já vem no repo:** [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md) — o
> **dossiê**, que vocês **preenchem ao longo das fases** (não precisa criar nada; cada seção
> mapeia uma fase). Visão geral em [docs/09](09-dossie-template.md).

---

## 2. Depois de formar o grupo: siga os roteiros comuns

| Para… | Roteiro |
|---|---|
| instalar as ferramentas, criar o token e **clonar** o repositório | [`comum/00`](../roteiros/comum/00-antes-de-comecar.md) |
| **subir** o VidaPlus, entrar no sistema, zerar o banco | [`comum/01`](../roteiros/comum/01-subir-o-vidaplus.md) |
| o ciclo **branch → commit → PR → revisão → merge** | [`comum/03`](../roteiros/comum/03-git-e-github.md) |
| rodar os **testes** de segurança e achar o seu no pipeline | [`comum/04`](../roteiros/comum/04-testes-e-pipeline.md) |
| entender o **pipeline** e por que ele nasce vermelho | [`comum/06`](../roteiros/comum/06-pipeline-do-zero.md) |
| tirar os **segredos do código** (F-10, aula guiada da Fase 6) | [`comum/08`](../roteiros/comum/08-segredos-fora-do-codigo.md) |

> **No primeiro push**, o gate do **Gitleaks** acusa os segredos falsos plantados no legado — é
> **esperado**. Não corrija nem reescreva o histórico agora: isso é o F-10, feito em aula guiada
> na Fase 6. Se o GitHub bloquear o push (pouco provável: os segredos são genéricos), permita
> pelo link "allow secret" da própria mensagem de erro.

---

## 3. O que o professor configura no repositório

O grupo **não** executa estas configurações; leia para **entender** e **explicar** no dossiê
(§8, Fase 6).

### 3.1 Proteção da `main` (ruleset)

Uma regra do GitHub (**Settings → Rules → Rulesets**) sobre a `main`:

- **Pull request obrigatório, com 1 aprovação** — ninguém junta código na `main` sem que **outro**
  integrante revise (o autor não aprova o próprio PR);
- **Block force pushes** — o histórico da `main` fica **append-only**: ninguém reescreve ou apaga
  commits, o que protege as evidências.

Os **gates do pipeline são informativos** (`continue-on-error`): nenhum é exigido para o merge.
Cada gate cobre o repositório inteiro (as falhas de **todos** os pacotes) e segue vermelho mesmo
depois que o seu P0 é corrigido; exigi-lo travaria a `main`.

### 3.2 Ambiente `production` com aprovação

O pipeline já builda, assina, atesta e promove a imagem (`release-sign` → `deploy-staging` →
`deploy-prod`). O *environment* `production` (**Settings → Environments**) tem **1 revisor
obrigatório**: o `deploy-prod` **pausa** até alguém aprovar, e só roda se a **assinatura e a
atestação** da imagem passarem na verificação. O CD só roda a partir da `main`.

> **Não confunda com a revisão do PR (§3.1).** São **dois portões**: um aprova o *merge* do
> código; o outro aprova a *promoção* da imagem para produção. Nos dois vale o mesmo princípio:
> **quem faz não aprova o próprio** (separação de funções).

---

## 4. Autograding (nota parcial de apoio)

O workflow [`.github/workflows/classroom.yml`](../.github/workflows/classroom.yml) roda a cada
push (aba **Actions** → **Autograding (Classroom)**) e pontua só o que o grupo implementa:

| Item | Pontos | Passa quando… |
|---|:-:|---|
| **F-10 / V-05** — segredos fora do código | 40 | a chave do token saiu do `application.properties` e do `JwtUtil.java` ([`comum/08`](../roteiros/comum/08-segredos-fora-do-codigo.md)) |
| **P0 do grupo** — cenário Gherkin `@security` | 60 | o cenário do seu P0 passa contra um MySQL de teste |

O cenário do P0 de cada grupo (o mesmo filtro do [`rodar-testes.sh`](../roteiros/comum/04-testes-e-pipeline.md#2-rodar-os-testes-no-seu-computador)):

| Grupo | P0 | Cenário |
|---|---|---|
| G1 | F-01 (SQLi no login) | `sqli.feature` — filtro `"security & F-01"` |
| G2 e G4 | F-02 (laudo de outro paciente) | `idor.feature` — filtro `"security & F-02"` |
| G3 | F-03 (paciente publicando laudo) | `rbac.feature` — filtro `"security & F-03"` |

No código legado os dois itens falham (0/100): é o ponto de partida. O autograding é **feedback,
não a nota** — a nota vem do dossiê e da defesa ([docs/07](07-rubrica-avaliacao.md)). "Enganar" o
teste sem corrigir de verdade é **anti-padrão** e aparece na revisão do PR e na defesa.

---

## 5. Checklist para o kickoff (Fase 0)

- [ ] Repositório do grupo formado (§1), todos com acesso.
- [ ] Cada integrante clonou e subiu o VidaPlus ([`comum/00`](../roteiros/comum/00-antes-de-comecar.md) e [`comum/01`](../roteiros/comum/01-subir-o-vidaplus.md)).
- [ ] Pipeline executou; placar vermelho registrado (print) ([`comum/06` §5.2](../roteiros/comum/06-pipeline-do-zero.md#52-abrir-uma-execução-e-ler-o-placar)).
- [ ] Combinado que a **liderança de cada fase** é definida quando a fase começa (revezando
  entre os integrantes) — ações por papel × fase em [docs/00](00-visao-geral.md).
- [ ] Inventário de ativos e classificação de dados iniciados (entregável da Fase 0).
