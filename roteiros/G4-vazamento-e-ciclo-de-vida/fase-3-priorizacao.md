# G4 · Fase 3 — Priorização, comportamento sob falha e Definition of Done

> **Seção do dossiê:** §5 · **Lidera:** 🛡️ Segurança · **Aula:** 5 · **Semana:** 6

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. A tabela de **priorização**: método, nota, risco e prioridade de cada requisito.
2. A explicação de **por que cada linha usa aquele método**.
3. O **comportamento sob falha** — **testado**. No G4, ele **é** uma funcionalidade (F-16).
4. A **Definition of Done** segura do grupo.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório (se você seguiu o [`comum/00`](../comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo), é a `vidaplus`, dentro da pasta `ssc` da sua pasta pessoal).
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
   mkdir -p entregaveis/evidencias
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. O terceiro cria a pasta das evidências, se ela ainda não existir. Os tokens valem **só neste terminal**: se abrir
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

**Fail-secure:** quando algo dá errado, o sistema **nega** — e **não conta** nada do lado de
dentro. Para o G4, as duas metades importam: negar (não entregar o laudo) e calar (não revelar
detalhes nem se o laudo existe).

---

## Passo 1 — Calcule o CVSS das falhas técnicas

> 🔎 **Como o Grupo 5 fez:** na calculadora do FIRST (`https://www.first.org/cvss/calculator/3.1`),
> respondeu cada métrica com o que **viu**, e escreveu o vetor completo. Injeção no login: **9.8**;
> na busca: **8.2**, porque a da busca **só lê**. Lição: **o impacto vem do que o acesso permite**.

**▶ Agora no G4:** um cálculo **por canal** — e repare como a nota muda:

| Canal | Pense em |
|---|---|
| API (o P0) | **PR**: precisa de conta? De qualquer conta? |
| Arquivo direto | **PR**: precisa de conta? |
| Mensagem de erro | **C**: o que se lê é dado pessoal, ou informação sobre o sistema? Isso é C:H ou C:L? |

Escrevam o **vetor completo** (`CVSS:3.1/...`) e a nota de cada um.

## Passo 2 — Use DREAD ou conformidade onde o CVSS não serve

> 🔎 **Como o Grupo 5 fez:** busca que isola uma pessoa → **DREAD 7,6**; anonimização (obrigação
> legal) → **conformidade**, sem nota.

**▶ Agora no G4:**

- **F-06 (retenção):** guardar além do prazo e não ter regra de eliminação descumprem quais
  artigos? Método?
- **F-18 (texto da notificação):** não há "ataque" — a mensagem faz o que foi feita para fazer.
  DREAD ou conformidade? Justifiquem.
- **F-20 (site adulterado):** técnico (CVSS) ou de processo? Depende de **como** o atacante muda o
  site — escolham o cenário e justifiquem.

## Passo 3 — Decida risco e prioridade, e justifique

> 🔎 **Como o Grupo 5 fez:** a coluna **Risco** é decisão do grupo. A busca sem login tinha CVSS
> **7.5**, mas risco **Crítico** e **P0**: *"o CVSS mede a falha técnica, não o tipo de dado."*

**▶ Agora no G4:** a nota do arquivo direto e a da API são diferentes — mas o **dado** é o mesmo
(o laudo). O risco deveria ser diferente? Justifiquem a coluna Risco separadamente da nota.
Escrevam o bloco **"Por que cada linha usa um método"**. EPSS/KEV: falhas no código do VidaPlus
não têm CVE → "n/a", com a explicação.

## Passo 4 — Defina e **teste** o comportamento sob falha (a F-16)

> 🔎 **Como o Grupo 5 fez:** escreveu "erro de banco → mensagem genérica" e **testou** (`q='`). A
> resposta trazia o SQL e o *stack trace*: a afirmação era **falsa**. Corrigiu com três linhas do
> `application.properties` e **testou derrubando o banco**. Leia a 💡 em
> [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G4** — o comportamento sob falha é a sua F-16, então a análise vai mais fundo. Monte
uma tabela **situação de falha | resposta de hoje | resposta que deveria ser**, provocando cada uma
(no terminal, na raiz do repositório; os blocos carregam os tokens com `source roteiros/comum/tokens.sh`):

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s -i localhost:8080/api/exames/999 -H "Authorization: Bearer $TOKEN_ANA" | head -12     # não existe
curl -s -i localhost:8080/api/exames/999/pdf -H "Authorization: Bearer $TOKEN_ANA" | head -12 # não existe (PDF)
curl -s -i localhost:8080/api/exames/abc -H "Authorization: Bearer $TOKEN_ANA" | head -12     # número inválido
curl -s -i localhost:8080/api/exames/101 | head -12                                           # sem login
curl -s -i localhost:8080/api/exames/101 -H "Authorization: Bearer token-falso" | head -12    # token falso
```

E o banco fora do ar:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
docker compose -f legacy-app/docker-compose.yml stop db
curl -s -i localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANA" | head -12
docker compose -f legacy-app/docker-compose.yml start db
```

(**Demora uns 30 segundos**: é o tempo que o backend espera pelo banco. Depois do `start`, espere
uns 20 segundos.)

Para cada linha da tabela, decidam: que **código** (400, 401, 403, 404, 500, 503)? Que **texto**?
E a regra do oráculo: "não existe" e "não é seu" devem dar **a mesma** resposta? Guarde as saídas
em `entregaveis/evidencias/fase3-falhas.txt`.

**E a rotina de retenção (planejada):** se ela falhar no meio, o paciente fica meio eliminado? (A
resposta do Grupo 5: tudo numa **transação** — ou tudo, ou nada.)

## Passo 5 — Escreva a Definition of Done

> 🔎 **Como o Grupo 5 fez:** 8 critérios: duas formas do requisito; PR revisado com checklist;
> teste do **ataque** e do **uso legítimo**; **ataque repetido à mão**; erro genérico **conferido
> provocando o erro**; nenhum segredo no código; "Verificado" na matriz. Veja:
> [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G4:** a DoD de vocês, com o que é **específico do G4**. Pistas:

- **todos os canais** até o dado foram testados, não só o do teste automático;
- nenhuma resposta (de sucesso **ou** de erro) contém detalhe interno ou indica se um recurso
  existe;
- todo dado novo nasce com **prazo de retenção** definido.

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

- [ ] Calculei o CVSS de cada canal e expliquei por que as notas diferem.
- [ ] F-06, F-18 e F-20 têm método justificado.
- [ ] Tenho a tabela situação de falha | hoje | deveria ser, com as saídas reais.
- [ ] A DoD cobre todos os canais e o prazo de retenção.
