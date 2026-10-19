# G2 · Fase 3 — Priorização, comportamento sob falha e Definition of Done

> **Seção do dossiê:** §5 · **Lidera:** 🛡️ Segurança · **Aula:** 5 · **Semana:** 6

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. A tabela de **priorização**: método, nota, risco e prioridade (P0/P1/P2) de cada requisito.
2. A explicação de **por que cada linha usa aquele método**.
3. O **comportamento sob falha** (*fail-secure*) dos controles — **testado**.
4. A **Definition of Done** segura do grupo.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
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
| **CVSS 3.1** | falha técnica com caminho de exploração | 8 perguntas na calculadora → nota de 0 a 10 |
| **DREAD** | ameaça sem vetor de ataque claro (privacidade por design) | 0–10 para Dano, Reprodutibilidade, Exploração, Afetados, Descoberta; média |
| **Conformidade** | descumprimento de lei, sem ataque | a prioridade vem da obrigação legal |
| **EPSS / KEV** | vulnerabilidade **com CVE** (biblioteca) | chance de exploração; lista de falhas já exploradas (sobe um nível) |

**Fail-secure:** quando algo dá errado, o sistema **nega** — e não revela detalhes internos.
**Para o G2, é a essência do P0:** *deny-by-default* — se o servidor não consegue confirmar que a
pessoa pode ver o exame, ele nega.

**Definition of Done (DoD):** as condições para dizer que uma funcionalidade está pronta com
segurança.

---

## Passo 1 — Calcule o CVSS das falhas técnicas

> 🔎 **Como o Grupo 5 fez:** na calculadora do FIRST (`https://www.first.org/cvss/calculator/3.1`),
> respondeu cada métrica com o que **viu** no VidaPlus (rede, sem complexidade, sem conta…) e
> escreveu o vetor completo. Para a injeção no login, **9.8**; para a da busca, **8.2** — porque a
> da busca **só lê**. Lição: **o impacto vem do que o acesso permite**.

**▶ Agora no G2:** calcule para cada falha técnica do pacote. Pense em especial:

| Métrica | Pergunta para o G2 |
|---|---|
| **PR** (privilégio exigido) | para ver o exame do Bruno pela API, o atacante precisa de **alguma** conta? E para baixar o PDF da pasta pública? |
| **UI** (interação do usuário) | alguém precisa clicar em algo para o ataque funcionar? |
| **C / I / A** | o que o atacante consegue **ler**, **alterar**, **derrubar**? Publicar um laudo indevidamente afeta qual? |

Faça pelo menos: o IDOR da API (P0), o PDF sem login (F-08), a publicação por qualquer papel
(F-07). Escreva o **vetor completo** (`CVSS:3.1/...`) e a nota.

## Passo 2 — Use DREAD ou conformidade onde o CVSS não serve

> 🔎 **Como o Grupo 5 fez:** busca que isola uma pessoa → DREAD **7,6**; anonimização (obrigação
> legal) → **conformidade**, sem nota.

**▶ Agora no G2:**

- **F-09 (portabilidade):** não ter a exportação não é um ataque: é não cumprir o art. 18. Método?
- **Oráculo de existência / números sequenciais:** técnico ou de privacidade por design? Escolha e
  **justifique**.

## Passo 3 — Decida risco e prioridade, e justifique

> 🔎 **Como o Grupo 5 fez:** a busca sem login tinha **CVSS 7.5**, mas o grupo marcou risco
> **Crítico** e **P0**: *"o CVSS mede a falha técnica, não o tipo de dado; aqui o dado é sensível
> e de todos os titulares."*

**▶ Agora no G2:** o CVSS do IDOR **não** é altíssimo (calcule e veja). Mas o dado é **resultado
de exame**. Qual é o **risco** para a clínica e para o paciente? Justifique a coluna Risco
separadamente da nota — exatamente como o Grupo 5 fez.

Escreva o bloco **"Por que cada linha usa um método"**. Sobre EPSS/KEV: falhas no código do
VidaPlus não têm CVE → "n/a", com a explicação.

## Passo 4 — Defina e **teste** o comportamento sob falha

> 🔎 **Como o Grupo 5 fez:** escreveu "erro de banco → mensagem genérica" e **testou** (`q='`). A
> resposta trazia o SQL e o *stack trace*: a afirmação era **falsa**. Registrou e corrigiu no
> próprio PR. Leia a 💡 em [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G2:** escreva o que **deve** acontecer em cada falha e provoque para ver o que
acontece **hoje** (no terminal, na raiz do repositório; os blocos carregam os tokens com `source roteiros/comum/tokens.sh`):

1. **Exame que não existe:**
   `curl -s -i localhost:8080/api/exames/999 -H "Authorization: Bearer $TOKEN_ANA" | head -5`
   e o mesmo com `/api/exames/999/pdf`. As respostas são iguais? Alguma revela detalhes
   (`exception`, `trace`)? Lembre do oráculo da Fase 2b: a resposta para "não existe" e "não é seu"
   deveria ser **a mesma**.
2. **Papel desconhecido / regra não prevista:** se aparecer um papel novo amanhã (ex.:
   "ENFERMEIRO"), a regra de vocês deixa ver ou nega? (Deve negar: é o *deny-by-default*.)
3. **Banco fora do ar durante a verificação:**

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   docker compose -f legacy-app/docker-compose.yml stop db
   curl -s -i localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANA" | head -12
   docker compose -f legacy-app/docker-compose.yml start db
   ```

   **Demora uns 30 segundos** (o backend espera o banco antes de desistir). A resposta entrega o
   exame? Revela detalhes internos? Espere uns 20 segundos depois do `start`.
4. **Exportação (planejada):** se a montagem do arquivo falhar no meio, o titular recebe um arquivo
   **pela metade**? Melhor receber parcial ou um erro claro?

Guarde as saídas em `entregaveis/evidencias/fase3-...txt`.

## Passo 5 — Escreva a Definition of Done

> 🔎 **Como o Grupo 5 fez:** 8 critérios: as duas formas do requisito; PR revisado com checklist;
> teste do **ataque** e do **uso legítimo**; **ataque repetido à mão**; erro genérico **conferido
> provocando o erro**; nenhum segredo no código; linha "Verificado" na matriz. Veja:
> [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G2:** a DoD de vocês, com o que é **específico do G2**. Pistas:

- a verificação de dono está em **todos** os caminhos até o laudo (exame, PDF, listagem), não só
  no que o teste cobre;
- testada para **cada papel** (paciente, médico, laboratório, recepção, admin);
- o uso legítimo de cada papel continua funcionando (a Ana vê os dela; a médica, os dos pacientes
  dela; o laboratório publica);
- a resposta negada **não revela** nada do exame (nem tipo, nem paciente) e é **igual** para "não
  existe" e "não é seu".

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

- [ ] Cada requisito tem método, nota (ou "—"), risco e prioridade.
- [ ] Cada métrica do CVSS foi justificada com algo observado.
- [ ] O risco do IDOR foi justificado **além** da nota.
- [ ] Testei o comportamento sob falha e registrei o que vi.
- [ ] A DoD cobre todos os caminhos e todos os papéis.
