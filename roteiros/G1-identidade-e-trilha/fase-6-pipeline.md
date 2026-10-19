# G1 · Fase 6 — Pipeline e segredos no código

> **Seção do dossiê:** §8 · **Lidera:** ⚙️ DevSecOps · **Aula:** 8 · **Semana:** 13
> Nesta fase vocês **não configuram o pipeline**: os gates ficam informativos e a proteção da
> `main` é do professor. O trabalho é **ler** o pipeline e **explicar** — e há **uma correção
> guiada no código**: tirar os segredos do código (F-10), no Passo 5.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. A tabela de **gates**: o que cada um verifica, o estado antes e depois da PoC, e se deveria ser
   bloqueante num projeto real (e por quê).
2. A explicação do **SBOM**, da **entrega assinada** e da **proteção da `main`**.
3. Os **segredos no código** (F-10): a análise, a **correção guiada** e as provas do antes e
   do depois.

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
   git checkout -b dossie/fase-6
   git push -u origin dossie/fase-6
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-6
   ```

   Antes de começar a editar, a cada aula: `git pull` (traz o que os colegas já enviaram).

---

## Conceitos em 5 minutos

- **Pipeline:** a lista de verificações que o GitHub roda a cada push
  (`.github/workflows/ssdlc-pipeline.yml`). Cada verificação é um **job**; os de segurança são
  **gates**.
- **Informativo × bloqueante:** um gate **informativo** mostra o problema mas não impede o merge;
  um **bloqueante** impede. Aqui todos são informativos — senão o repositório de cada grupo
  ficaria travado pelas falhas dos outros pacotes. Mais em [`docs/06` §2](../../docs/06-pipeline-cicd.md#2-informativo--bloqueante).
- **SBOM:** a "lista de ingredientes" do software (bibliotecas e versões).
- **Entrega assinada:** na `main`, o pipeline constrói a imagem, **assina** e só a publica em
  produção depois de **conferir quem assinou** e de uma aprovação humana. Leia
  [`docs/06` §5](../../docs/06-pipeline-cicd.md#5-entrega-segura-já-pré-fiada-assinatura-verificação-e-promoção)
  — inclusive o quadro "ainda não é SLSA L2".
- **Segredo no código:** senha ou chave escrita num arquivo do repositório. Quem lê o repositório
  ganha o acesso.

---

## Passo 1 — Leia os gates de uma execução

> 🔎 **Como o Grupo 5 fez:** abriu uma execução **antes** da PoC e uma **depois**, leu o "Placar
> de gates SSDLC" das duas e montou a tabela, com uma opinião justificada sobre cada gate. Veja:
> [`docs/12` §8](../../docs/12-exemplo-dossie-preenchido.md#8-fase-6--pipeline-gerador-de-evidência).

**▶ Agora no G1:**

1. **A execução de "antes"** (o código antes da correção da Fase 4):
   1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
   2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
   3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com o nome da
      branch da correção (`fix/F-01-sqli-login`). A execução **logo abaixo** dela é a de **antes**:
      clique no título dela.
   4. Role a página até a tabela **"Placar de gates SSDLC"**. Anote o resultado de cada gate
      (`success` = verde, `failure` = vermelho) e tire um print (**Shift+Print Screen**).
   5. Copie a URL da barra do navegador para o rascunho.
2. **A execução de "depois":** volte à lista (**Actions** → **SSDLC - VidaPlus**, filtro
   **Branch** = `main`), clique no título da execução **"Merge pull request #… fix/F-01-sqli-login"** e
   faça a mesma anotação: placar, print e URL.
3. **O que cada gate verifica:** na execução de "depois", clique em cada job da lista da esquerda.
   O nome das etapas e os comandos mostram o que ele faz. A tabela de todos os jobs, em palavras,
   está em [`comum/06` §3.4](../comum/06-pipeline-do-zero.md#34-todos-os-jobs-do-vidaplus), e cada
   ferramenta em [`comum/07`](../comum/07-ferramentas-de-seguranca.md).
4. Monte a tabela. Para o G1, pense em especial:
   - **Testes de segurança:** o seu cenário ficou verde, mas o job continua vermelho. Por quê?
   - **SAST (Semgrep):** o número de achados mudou com a sua correção? Que regra do **seu pacote**
     continua acusando (pense no MD5 e no token sem expiração)?
   - **"Deveria ser bloqueante?"**: responda **por gate**, com um motivo. Ex.: um gate só com os
     testes do seu pacote poderia bloquear hoje? (Material opcional: [`docs/13`](../../docs/13-gate-bloqueante-do-grupo.md).)

## Passo 2 — Ache o SBOM e uma biblioteca do seu pacote

> 🔎 **Como o Grupo 5 fez:** explicou o que é o SBOM e onde encontrá-lo (**Actions** → execução →
> **Artifacts** → `sbom`).

**▶ Agora no G1:** baixe e abra o SBOM:

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-01-sqli-login"**) e role até a seção **Artifacts**, no fim da página.
2. Clique em **sbom**. O navegador baixa o arquivo `sbom.zip` para a pasta **Downloads**.
3. No terminal, descompacte e confira:

   ```bash
   mkdir -p ~/Downloads/artefato-sbom
   unzip -o ~/Downloads/sbom.zip -d ~/Downloads/artefato-sbom
   ls ~/Downloads/artefato-sbom
   ```

   Deve aparecer `sbom.cdx.json`.
4. Abra no VS Code: **File → Open File…** → `Downloads/artefato-sbom/sbom.cdx.json`.

Com o arquivo aberto,
procure (**Ctrl+F**) as bibliotecas que o seu pacote usa:

- `jjwt` — a biblioteca que cria e confere o token (F-11);
- `spring-security-crypto` — a que tem o `PasswordEncoder` e o Argon2 (F-01).

Anote a **versão** de cada uma. Escreva no dossiê **para que serve** o SBOM, com este exemplo: se
amanhã sair uma vulnerabilidade grave no `jjwt`, como o SBOM ajuda a clínica a saber se está
afetada?

## Passo 3 — Explique a entrega assinada e a proteção da `main`

> 🔎 **Como o Grupo 5 fez:** explicou o fluxo `release-sign → deploy-staging → deploy-prod`, por que
> a verificação confere **quem** assinou (e não aceita `.*`), que isso **ainda não é SLSA L2**
> (falta a proveniência) e que a proteção da `main` aplica "quem faz não aprova o próprio".

**▶ Agora no G1:**

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-01-sqli-login"**). Na lista de jobs da esquerda, clique, um de cada vez, em `Release - build, push, sign & attest SBOM`,
   `Deploy staging + smoke` e `Deploy prod - verifica assinatura + aprovacao`. O último **pausa**
   esperando aprovação? Quem pode aprovar?
2. Explique com as palavras de vocês: o que a assinatura garante, e o que ela **não** garante.
3. Abra o PR da Fase 4 (aba **Pull requests** → **Closed**). Na linha do tempo, ache a aprovação
   do revisor. Antes dela, a caixa de merge mostrava **"Review required"** e o botão ficava cinza:
   isso é a **proteção da `main`** funcionando. Explique por que ela existe ("quem faz não aprova
   o próprio"). **Não** tente fazer `push` direto na `main` para "testar": se a proteção não
   estiver ativa, você suja o histórico do grupo.

## Passo 4 — Ache e analise os segredos no código (F-10)

> 🔎 **Como o Grupo 5 fez:** listou cada segredo numa tabela **onde | segredo | risco | plano** e
> terminou com a frase: *"Como os segredos já estão no histórico do Git, removê-los dos arquivos
> não basta: os valores precisam ser trocados."* Classificou a senha do banco de teste do CI como
> **risco aceito**, com justificativa.

**▶ Agora no G1:**

1. **No pipeline:** abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-01-sqli-login"**) → job **Gate - Secret Scanning
   (Gitleaks)** → clique na etapa **Gitleaks** e leia os achados (como ler cada campo:
   [`comum/07` §4](../comum/07-ferramentas-de-seguranca.md#4-gitleaks--procura-segredos-inclusive-no-passado)).
   **No seu computador**, rode e grave a saída:

   ```bash
   mkdir -p entregaveis/evidencias
   docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
     detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
     --log-opts="--all -- legacy-app" --no-banner --redact -v \
     | tee entregaveis/evidencias/fase6-gitleaks.txt
   ```

   O `--redact` é **obrigatório** aqui: sem ele, os valores dos segredos iriam para o arquivo de
   evidência e, com o commit, de novo para o Git. A última linha deve dizer `leaks found: 8`
   (se o grupo não tiver mexido nesses arquivos).
2. Para cada achado, anote arquivo e linha, e diga **o que é** aquele segredo.
3. **Para o G1, a chave que assina os tokens (`JwtUtil.java`) é o segredo mais grave do sistema:**
   ela é a "chave mestra" da F-11. Escreva o risco **concreto**: quem tem o código pode fabricar um
   token de qualquer pessoa, com qualquer papel — e, como os tokens não expiram, trocar a chave é
   a única forma de invalidar todos os tokens já emitidos.
4. Repare se o Gitleaks acusa algum arquivo que **não existe mais** no repositório. Se acusar,
   explique o que isso prova sobre o histórico do Git.
5. Escreva o **plano** de cada segredo: tirar do código (variável de ambiente; arquivo `.env`
   fora do Git, com um `.env.example` de modelo), **trocar o valor**, e o que fazer com segredos de
   teste (risco aceito?).

Este passo é só a **análise**: não mexa no código ainda. A correção vem no Passo 5.

## Passo 5 — Corrija os segredos no código (F-10) — aula guiada

> 🔎 **Como o Grupo 5 fez:** seguiu o roteiro comum dos segredos, guardou as provas do antes e do
> depois e escreveu na §8 o que foi feito, o requisito REQ-SEGREDO-01 nas duas formas e o que
> **ainda** fica em aberto. Veja: [`docs/12` §8](../../docs/12-exemplo-dossie-preenchido.md#8-fase-6--pipeline-gerador-de-evidência).

**▶ Agora no G1:**

1. **Grave o que já escreveram do dossiê** (vocês estão na branch `dossie/fase-6`), porque o
   roteiro comum vai trocar de branch:

   ```bash
   git add entregaveis/
   git commit -m "docs(dossie): fase 6 - rascunho"
   git push
   ```

   Se aparecer `nothing to commit`, não havia nada novo: siga em frente.
2. **Siga o roteiro comum inteiro:** [`comum/08` — Segredos fora do código](../comum/08-segredos-fora-do-codigo.md),
   do Passo 1 ao 13. Ele cria a branch `fix/F-10-segredos` a partir da `main` e termina com a
   correção já na `main`. É o **mesmo** para os quatro grupos; o que muda no G1 está no item 3.
3. **O que muda no G1:** a chave do token é a "chave mestra" da F-11. No Passo 9.1 do roteiro
   comum, o token **antigo** do admin é recusado depois da troca — guardem essa evidência com
   destaque: como o token do VidaPlus **não expira**, trocar a chave foi a **única** forma de
   revogar todos os tokens já emitidos. Escrevam isso na §8 e liguem com o plano da F-11
   (expiração curta): a troca da chave vira o "botão de emergência" do plano de resposta a
   incidente da Fase 7.
4. **Volte para a branch do dossiê** e traga a correção que acabou de entrar na `main`:

   ```bash
   git checkout dossie/fase-6
   git pull
   git merge main --no-edit
   ```

   (`--no-edit` aceita a mensagem padrão do merge, sem abrir um editor no terminal.)
5. **Escreva na §8 do dossiê** (substitua a "análise e plano" do Passo 4 pelo que foi **feito**):
   - a tabela **onde | segredo | risco | o que foi feito** (tirado do código? valor trocado? risco
     aceito, com justificativa, para os de teste);
   - o **REQ-SEGREDO-01** nas duas formas (o texto "deve" e o nome do arquivo `.feature` —
     [`comum/08` Passo 10](../comum/08-segredos-fora-do-codigo.md#passo-10--escreva-o-requisito-as-duas-formas));
   - as provas, com o nome de cada arquivo de evidência (`fase6-f10-...`): o Gitleaks 🔴 8 → 🟢
     `no leaks found`, o V-05, o token antigo e o fabricado recusados, o "sem `.env` não sobe";
   - **por que** os 8 achados do histórico viraram exceção no `.gitleaksignore` (e por que isso só
     é honesto **depois** de trocar os valores);
   - na **tabela de gates** (Passo 1), a linha do Gitleaks ganha o "depois" da F-10;
   - **o que continua em aberto** (seja honesto): o `.env` fica em texto no computador de cada
     um (num projeto real, um **cofre de segredos**); a troca foi manual (num projeto real, há um
     processo de **rotação**); e, no G1, o token continua sem expiração (F-11, no plano) — a troca da chave revoga
   **todos** os tokens de uma vez, inclusive os de quem não fez nada de errado.

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| `unzip: cannot find or open ~/Downloads/sbom.zip` | o arquivo do SBOM ainda não foi baixado, ou o navegador o salvou com outro nome | baixe de novo (**Actions** → execução → **Artifacts** → **sbom**) e confira o nome na pasta **Downloads**; se for outro (ex.: `sbom (1).zip`), use esse nome entre aspas no `unzip` |
| O Gitleaks deu `no leaks found` já no Passo 4 | a correção da F-10 já está na `main` (vocês estão repetindo a fase) | é o resultado do "depois"; use as evidências que já existem |
| `git merge main --no-edit` diz `CONFLICT` | o mesmo trecho do dossiê foi editado nas duas branches | veja [`comum/03` §4.2](../comum/03-git-e-github.md#42-se-aparecer-um-conflito) |
| Algo da aula guiada (Passo 5) deu errado | — | use a tabela **Deu errado?** do próprio [`comum/08`](../comum/08-segredos-fora-do-codigo.md#deu-errado) |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-6`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 6 - pipeline"
git push -u origin dossie/fase-6
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
   **compare** escolha `dossie/fase-6` → **Create pull request**.
2. Título: "Dossiê — Fase 6: pipeline". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] A tabela de gates tem estado antes/depois e uma opinião justificada por gate.
- [ ] Sei onde está o SBOM e achei nele as bibliotecas do meu pacote.
- [ ] Expliquei assinatura, verificação de quem assinou e por que não é SLSA L2.
- [ ] Cada segredo tem risco concreto e plano (incluindo **trocar** o valor).
- [ ] Fiz a correção guiada da F-10 e registrei na §8 por que trocar a chave revogou todos os
  tokens (ligação com a F-11).
