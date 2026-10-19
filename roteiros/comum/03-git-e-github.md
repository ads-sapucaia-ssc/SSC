# 03 · Git e GitHub — gravar e entregar o trabalho do grupo

**Objetivo:** saber levar uma mudança (no dossiê ou no código) do seu computador até a `main` do
repositório do grupo, do jeito que o projeto exige: **branch → commit → push → Pull Request →
revisão de um colega → merge**.

**Use este guia** toda vez que um roteiro disser "grave no repositório" ou "abra um PR".

---

## 1. As palavras, em uma frase cada

| Termo | O que é |
|---|---|
| **Repositório** | a pasta do projeto, com todo o histórico. Existe uma cópia no GitHub e uma no seu computador |
| **Commit** | um "ponto salvo" no histórico, com uma mensagem dizendo o que mudou |
| **Branch** | uma "linha paralela" de trabalho. Você muda as coisas na sua branch sem mexer na `main` |
| **`main`** | a branch oficial. É ela que o professor avalia |
| **Push** | enviar os seus commits do computador para o GitHub |
| **Pull** | trazer do GitHub o que os colegas enviaram |
| **Pull Request (PR)** | o pedido, no GitHub, para juntar a sua branch à `main`. É onde o colega revisa e o pipeline roda |
| **Merge** | juntar a branch à `main`, depois do PR aprovado |
| **Conflito** | quando você e um colega mudaram **as mesmas linhas** do mesmo arquivo; o Git pede que alguém decida qual versão fica |

## 2. Por que tanta cerimônia?

A `main` do seu repositório está **protegida** (configuração feita pelo professor): ninguém
consegue enviar direto para ela. Todo trabalho entra por um **Pull Request aprovado por outro
integrante**. Isso não é burocracia: é um controle de segurança de verdade, chamado **separação de
funções** — quem faz não aprova o próprio trabalho. Você vai explicar isso no dossiê, na Fase 6.

---

## 3. O ciclo completo, passo a passo

Use o terminal do VS Code, na **raiz do repositório** (a pasta que tem `README.md`, `docs`,
`entregaveis`…). Se estiver dentro de `legacy-app`, volte com `cd ..`.

### Passo 1 — Comece sempre pela `main` atualizada

```bash
git checkout main
git pull
```

- `git checkout main` muda para a `main`. Resposta: `Switched to branch 'main'` ou
  `Already on 'main'`.
- `git pull` traz o que os colegas já juntaram. Resposta: a lista de arquivos atualizados, ou
  `Already up to date.`

> Se o `git pull` pedir usuário e senha: usuário do GitHub e, na senha, **o token**
> ([`00`](00-antes-de-comecar.md) §2.1).

### Passo 2 — Crie uma branch para o trabalho

```bash
git checkout -b dossie/fase-0
```

- `-b` cria uma branch nova e já muda para ela.
- `dossie/fase-0` é o **nome inteiro** da branch: a barra faz parte do nome. Não existe uma
  branch `dossie` separada.

**Nomes que o projeto usa:**

| Tipo de trabalho | Nome da branch | Exemplo |
|---|---|---|
| seção do dossiê | `dossie/fase-<n>` | `dossie/fase-2a` |
| correção de falha (Fase 4) | `fix/<funcionalidade>-<assunto>` | `fix/F-02-idor` |
| especificação de algo novo | `spec/<funcionalidade>-<assunto>` | `spec/F-04-consentimento` |

### Passo 3 — Faça as mudanças

Edite os arquivos no VS Code e **salve** (**Ctrl+S**). Um arquivo modificado e não salvo aparece
com uma bolinha branca na aba.

### Passo 4 — Veja o que mudou

```bash
git status
```

Lista os arquivos modificados (em vermelho) e os novos (`Untracked files`).

Para ver **o que exatamente** mudou dentro dos arquivos, use a aba **Source Control** do VS Code
(ícone de ramificação, à esquerda, ou **Ctrl+Shift+G**): clique num arquivo e veja, lado a lado, o
antes e o depois.

> **Confira antes de gravar** que nenhum arquivo traz senha, token ou dado pessoal real. Prints de
> tela também: não mostre o seu token do GitHub nem nada fora do VidaPlus.

### Passo 5 — Grave um commit

```bash
git add entregaveis/
git commit -m "docs(dossie): fase 0 - inventario de ativos"
```

- `git add <caminho>` escolhe o que entra no próximo commit. Aqui, tudo dentro de `entregaveis/`
  (o dossiê e as evidências). Se mudou outra coisa, acrescente o caminho:
  `git add entregaveis/ legacy-app/backend/src/test/`.
- `git commit -m "mensagem"` grava. A mensagem diz **o que** mudou, em poucas palavras.

Resposta esperada: `[dossie/fase-0 1a2b3c4] docs(dossie): fase 0 - ...` e um resumo como
`1 file changed, 25 insertions(+)`.

Pode (e deve) fazer **vários commits pequenos** ao longo do trabalho.

### Passo 6 — Envie a branch para o GitHub

Na **primeira vez** que enviar esta branch:

```bash
git push -u origin dossie/fase-0
```

Das próximas vezes, nesta mesma branch, basta `git push`.

Resposta esperada: linhas terminando com
`branch 'dossie/fase-0' set up to track 'origin/dossie/fase-0'`.

### Passo 7 — Abra o Pull Request

1. Abra o repositório do grupo no GitHub.
2. Aparece uma faixa amarela com o botão **Compare & pull request**. Clique nele.
   - **A faixa não apareceu?** Aba **Pull requests** → **New pull request**. Em **base**, deixe
     `main`; em **compare**, escolha a sua branch. Clique em **Create pull request**.
3. **Título:** o que o PR entrega (ex.: "Dossiê — Fase 0: inventário de ativos").
4. **Descrição:** diga o que foi feito, quem participou e o que o revisor deve conferir. Para PR
   de correção, diga também qual teste deve passar.
5. Clique em **Create pull request**.

### Passo 8 — Revisão por outro integrante

**Quem revisa** (não pode ser quem abriu o PR):

1. Abre o PR no GitHub e clica na aba **Files changed**.
2. Lê as mudanças. Para comentar uma linha, passa o mouse sobre ela e clica no **+** azul.
3. Para PR de código, usa o **checklist de revisão** do
   [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc).
4. Clica em **Review changes** (canto superior direito) → escolhe:
   - **Approve** — está bom;
   - **Request changes** — precisa mudar algo (escreva o quê);
5. → **Submit review**.

Se o revisor pediu mudanças, o autor corrige no computador, faz commit e `git push` na **mesma
branch**: o PR se atualiza sozinho.

### Passo 9 — Merge

Com a aprovação: na página do PR, **Merge pull request** → **Confirm merge**. Depois, o GitHub
oferece **Delete branch**: pode clicar.

### Passo 10 — Atualize o seu computador

```bash
git checkout main
git pull
git branch -d dossie/fase-0
```

O último comando apaga a branch local, que já foi juntada. Esperado: `Switched to branch 'main'`,
as linhas do `pull` e `Deleted branch dossie/fase-0`.

---

## 4. Trabalhando em quatro pessoas no mesmo `DOSSIE.md`

O dossiê é **um arquivo só** para o grupo todo. Para não brigar com conflitos:

1. **Uma branch por fase**, criada por quem lidera a fase (ex.: `dossie/fase-2a`). Os colegas
   trabalham **nessa mesma branch**:

   ```bash
   git fetch
   git checkout dossie/fase-2a
   git pull
   ```

   (`git fetch` descobre as branches novas do GitHub.)
2. **Antes de começar a editar, sempre `git pull`.** Depois de editar, **commit e push logo**.
   Quanto mais tempo você fica com mudanças só no seu computador, maior a chance de conflito.
3. **Dividam por pedaço:** cada um escreve **linhas diferentes** (por exemplo, cada integrante
   cuida das linhas de uma funcionalidade na tabela). Duas pessoas na mesma linha = conflito.

### 4.1 Alternativa sem terminal: editar pelo site

Para mudanças só de **texto**, dá para editar direto no GitHub:

1. Abra `entregaveis/DOSSIE.md` no site e clique no **lápis** (✏️, *Edit this file*).
2. Edite e clique em **Commit changes...**.
3. Escolha **Create a new branch for this commit and start a pull request**, dê um nome à branch
   e confirme. O GitHub já abre o PR.

Para prints e arquivos de evidência, use o terminal (ou **Add file → Upload files** no site).

### 4.2 Se aparecer um conflito

Ao rodar `git pull`, você pode ver:

```
CONFLICT (content): Merge conflict in entregaveis/DOSSIE.md
```

Calma: nada foi perdido.

1. Abra o arquivo no VS Code. Os trechos em conflito aparecem marcados assim:

   ```
   <<<<<<< HEAD
   a sua versão
   =======
   a versão do colega
   >>>>>>> ...
   ```

2. Acima de cada trecho, o VS Code mostra botões: **Accept Current Change** (fica a sua),
   **Accept Incoming Change** (fica a do colega), **Accept Both Changes** (ficam as duas). Na
   maioria das vezes no dossiê, a resposta certa é **Accept Both** e depois ajustar o texto à mão.
3. Confira que não sobrou nenhuma linha `<<<<<<<`, `=======` ou `>>>>>>>`.
4. Salve e termine:

   ```bash
   git add entregaveis/DOSSIE.md
   git commit -m "resolve conflito no dossie"
   git push
   ```

Se ficar confuso, **não apague nada**: chame o professor ou um colega.

---

## 5. Problemas comuns

| O que aparece | O que significa | O que fazer |
|---|---|---|
| `fatal: not a git repository` | o terminal não está dentro do repositório | `cd ~/ssc/vidaplus` (a pasta criada no clone, [`00` §4](00-antes-de-comecar.md#4-clone-o-repositório-do-grupo)) |
| `Your local changes ... would be overwritten by checkout` | você tem mudanças não gravadas e tentou trocar de branch | grave-as (passos 4–5) ou, se forem lixo, peça ajuda antes de descartar |
| `! [rejected] ... (fetch first)` no push | a branch no GitHub tem commits que você não tem | `git pull` e depois `git push` |
| `Authentication failed` | token errado, vencido ou sem a permissão `repo` | gere outro token ([`00`](00-antes-de-comecar.md) §2.1) |
| No PR, o botão de merge está cinza: **Review required** | falta a aprovação de outro integrante | peça a revisão (passo 8) |
| No PR, aparece **This branch has conflicts** | a `main` mudou nas mesmas linhas | no computador: `git checkout <sua-branch>`, `git pull origin main`, resolva (§4.2), `git push` |
| Commitei um arquivo errado (ex.: com senha) | — | **não tente apagar o histórico sozinho**. Avise o professor. Se for um token seu, apague-o no GitHub imediatamente |
