# G3 · Fase 3 — Priorização, matriz de permissões, comportamento sob falha e DoD

> **Seção do dossiê:** §5 · **Lidera:** 🛡️ Segurança · **Aula:** 5 · **Semana:** 6

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. A tabela de **priorização**: método, nota, risco e prioridade de cada requisito.
2. A explicação de **por que cada linha usa aquele método**.
3. **A matriz de permissões** (papel × função) — o projeto do controle por papel. Específico do G3.
4. O **comportamento sob falha** — **testado**.
5. A **Definition of Done** segura do grupo.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório.
   **Todos os comandos deste roteiro são digitados nesta pasta.**
2. **Entre na branch da fase.** Quem **lidera** a fase cria a branch e a envia ao GitHub:

   ```bash
   git checkout main
   git pull
   git checkout -b dossie/fase-3
   git push -u origin dossie/fase-3
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-3
   ```

   Antes de começar a editar, a cada aula: `git pull` (traz o que os colegas já enviaram).
3. **Ligue o VidaPlus com o banco original e carregue os tokens** dos usuários de teste:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

**Prioridade:** **P0** bloqueia a entrega; **P1** é essencial; **P2** é melhoria.

| Método | Serve para | Como funciona |
|---|---|---|
| **CVSS 3.1** | falha técnica com caminho de exploração | 8 perguntas na calculadora → 0 a 10 |
| **DREAD** | ameaça sem vetor claro (privacidade por design) | 0–10 em cinco critérios; média |
| **Conformidade** | descumprimento de lei, sem ataque | a prioridade vem da obrigação legal |
| **EPSS / KEV** | vulnerabilidade **com CVE** | chance de exploração; lista de falhas já exploradas |

**Matriz de permissões:** a tabela que diz, para cada função do sistema, **quais papéis podem**.
Tudo o que não está marcado é **negado** (*deny-by-default*). É o "projeto" do RBAC: a Fase 4 só
traduz essa tabela em código.

**Fail-secure:** quando algo dá errado, o sistema **nega** e não revela detalhes.

---

## Passo 1 — Calcule o CVSS das falhas técnicas

> 🔎 **Como o Grupo 5 fez:** na calculadora do FIRST (`https://www.first.org/cvss/calculator/3.1`),
> respondeu cada métrica com o que **viu**, e escreveu o vetor completo. Injeção no login: **9.8**;
> na busca: **8.2**, porque a da busca **só lê**. Lição: **o impacto vem do que o acesso permite**.

**▶ Agora no G3:** o RBAC ausente não é **uma** falha: são várias funções, com impactos
diferentes. Calcule para pelo menos três, e repare como a nota muda com o **impacto**:

| Função | Pense em C / I / A |
|---|---|
| publicar laudo (P0) | o que muda? quem é prejudicado? |
| desativar contas | o que um paciente consegue **derrubar**? (Fase 2a) |
| listar usuários / ver a agenda / ver a fila | o que se **lê**? |

Em todas: **PR** — precisa de conta? De que tipo? Escreva o **vetor completo** (`CVSS:3.1/...`).

## Passo 2 — Use DREAD ou conformidade onde o CVSS não serve

> 🔎 **Como o Grupo 5 fez:** busca que isola uma pessoa → **DREAD 7,6**; anonimização (obrigação
> legal) → **conformidade**, sem nota.

**▶ Agora no G3:** F-04 (sem consentimento), F-15 (coleta sem finalidade) e F-19 (titular sem
acesso/correção) são, quase sempre, **conformidade** — a prioridade vem da lei. Escrevam **qual
artigo** cada uma descumpre. Se alguma tiver um caminho técnico de exploração (ex.: a atribuição
em massa da F-19), use CVSS para ela.

## Passo 3 — Decida risco e prioridade, e justifique

> 🔎 **Como o Grupo 5 fez:** a coluna **Risco** é decisão do grupo. A busca sem login tinha CVSS
> **7.5**, mas risco **Crítico** e **P0**: *"o CVSS mede a falha técnica, não o tipo de dado."*

**▶ Agora no G3:** para cada requisito, risco e prioridade; se mudar a do catálogo, explique.
Escreva o bloco **"Por que cada linha usa um método"**. EPSS/KEV: falhas no código do VidaPlus não
têm CVE → "n/a", com a explicação.

## Passo 4 — Desenhe a matriz de permissões

Não há equivalente no exemplo do Grupo 5 (o pacote dele não tinha RBAC). É a peça de **projeto**
mais importante do G3.

**▶ Agora no G3:**

1. Liste **todas** as funções que vocês acharam na Fase 0 (método + endereço).
2. Para cada uma, marque **quem pode**, consultando o quadro de atores do
   [`docs/00`](../../docs/00-visao-geral.md) (o que cada papel faz e **não** faz):

   | Função | Endereço | ADMIN | MEDICO | RECEPCAO | BIOQUIMICO | PACIENTE |
   |---|---|:-:|:-:|:-:|:-:|:-:|
   | Listar usuários | `GET /api/usuarios` | | | | | |
   | Ativar/desativar usuário | `POST /api/usuarios/{id}/alternar-ativo` | | | | | |
   | Ver a agenda | `GET /api/atendimentos` | | | | | |
   | Agendar | `POST /api/atendimentos` | | | | | |
   | Ver a própria carteira de pacientes | `GET /api/atendimentos/medico/{id}` | | | | | |
   | Ver a fila de laudos | `GET /api/exames/pendentes` | | | | | |
   | Publicar laudo | `POST /api/exames/{id}/publicar` | | | | | |
   | Editar cadastro de paciente | `PUT /api/pacientes/{id}` | | | | | |

3. **Justifique cada ✓** numa frase (ex.: "a recepção agenda porque é a função dela"). Cada
   **vazio** é uma negação.
4. **Casos difíceis — decidam e registrem:**
   - o ADMIN pode publicar laudo? (Ele administra o sistema, não faz conteúdo clínico.)
   - a carteira do médico: além do papel MEDICO, o médico só pode ver **a própria** carteira
     (o `{id}` dele)? Isso é papel ou é dono? (Dono é o assunto do G2; registrem como interface.)
   - o paciente edita o **próprio** cadastro (F-19)? Hoje não há essa função; se houver, ela é
     **outra** função ("editar o meu cadastro"), com regras próprias.
5. A matriz entra no dossiê, na §5. Ela é a referência da Fase 4 **e** da Fase 5 (cada célula vira
   um teste).

## Passo 5 — Defina e **teste** o comportamento sob falha

> 🔎 **Como o Grupo 5 fez:** escreveu "erro de banco → mensagem genérica" e **testou**: a resposta
> trazia o SQL e o *stack trace*. A afirmação era **falsa**; corrigiu no próprio PR. Leia a 💡 em
> [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G3:** o que **deve** acontecer, e o que acontece **hoje** (no terminal, na raiz do repositório; os blocos carregam os tokens com `source roteiros/comum/tokens.sh`):

1. **Sem token:** `curl -s -i -X POST localhost:8080/api/exames/104/publicar | head -5` — deveria
   ser **401** ("não sei quem você é"). O que vem?
2. **Token inválido:** o mesmo com `-H "Authorization: Bearer token-falso"`.
3. **Papel desconhecido:** se amanhã surgir um papel novo (ex.: "ENFERMEIRO") e ninguém atualizar a
   matriz, a regra de vocês **nega** ou **libera**? (Tem que negar.)
4. **Consentimento (planejado):** se o sistema não conseguir consultar o registro de consentimento
   (banco fora do ar), ele trata o dado para a finalidade opcional ou não?

Guarde as saídas em `entregaveis/evidencias/fase3-...txt`.

## Passo 6 — Escreva a Definition of Done

> 🔎 **Como o Grupo 5 fez:** 8 critérios: duas formas do requisito; PR revisado com checklist;
> teste do **ataque** e do **uso legítimo**; **ataque repetido à mão**; erro genérico **conferido
> provocando o erro**; nenhum segredo no código; "Verificado" na matriz. Veja:
> [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G3:** a DoD de vocês, com o que é **específico do G3**. Pistas:

- cada célula da matriz de permissões tem um cenário (negação **e** permissão);
- a regra de papel fica num **lugar só**, chamada por todas as funções;
- sem token → 401; papel errado → 403; papel desconhecido → 403;
- todo dado coletado tem finalidade registrada (F-15).

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-3`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 3 - priorizacao"
git push -u origin dossie/fase-3
```

- `git status` lista o que mudou: confira que só aparecem os arquivos que você quer gravar;
- `git add` escolhe o que entra; `git commit` grava; `git push` envia ao GitHub;
- se o `push` pedir usuário e senha: o seu usuário do GitHub e, na senha, o **token** do GitHub
  ([`comum/00` §2.1](../comum/00-antes-de-comecar.md#21-crie-um-token-de-acesso-a-senha-do-git));
- se o `push` for recusado (`rejected`), um colega enviou algo antes: rode `git pull` e repita o
  `git push`.

Depois, no navegador:

1. Abra o repositório do grupo no GitHub e clique em **Compare & pull request** (faixa amarela).
   Se a faixa não aparecer: **Pull requests** → **New pull request** → em **base** deixe `main`, em
   **compare** escolha `dossie/fase-3` → **Create pull request**.
2. Título: "Dossiê — Fase 3: priorização". Na descrição: o que foi feito e quem participou. Clique em
   **Create pull request**.
3. **Outro integrante** (não quem abriu) abre o PR → aba **Files changed** → lê → **Review
   changes** → **Approve** → **Submit review**.
4. Com a aprovação: **Merge pull request** → **Confirm merge** → **Delete branch**.
5. No terminal de cada integrante:

   ```bash
   git checkout main
   git pull
   ```

Conflito ou erro? Veja [`comum/03`](../comum/03-git-e-github.md) §4.2 e §5.

---

## Confira antes de seguir

- [ ] Calculei o CVSS de pelo menos três funções e expliquei por que as notas diferem.
- [ ] F-04, F-15 e F-19 têm o artigo de lei que justifica a prioridade.
- [ ] A matriz de permissões está completa, com cada ✓ justificado e os casos difíceis decididos.
- [ ] Testei sem token, com token inválido, e decidi o papel desconhecido.
