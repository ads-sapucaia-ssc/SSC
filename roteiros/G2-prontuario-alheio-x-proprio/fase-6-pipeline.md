# G2 · Fase 6 — Pipeline e segredos no código

> **Seção do dossiê:** §8 · **Lidera:** ⚙️ DevSecOps · **Aula:** 8 · **Semana:** 13
> Nesta fase **não se configura nada**: os gates ficam informativos e a proteção da `main` é do
> professor. O trabalho é **ler** o pipeline e **explicar**.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. A tabela de **gates**: o que verifica, estado antes e depois da PoC, e se deveria ser
   bloqueante num projeto real (e por quê).
2. A explicação do **SBOM**, da **entrega assinada** e da **proteção da `main`**.
3. A análise dos **segredos no código** (F-10) e o plano.

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

- **Pipeline:** as verificações que o GitHub roda a cada push (`.github/workflows/ssdlc-pipeline.yml`).
  Cada uma é um **job**; os de segurança são **gates**.
- **Informativo × bloqueante:** aqui todos são informativos — senão o repositório ficaria travado
  pelas falhas dos outros pacotes ([`docs/06` §2](../../docs/06-pipeline-cicd.md#2-informativo--bloqueante)).
- **SBOM:** a "lista de ingredientes" do software.
- **Entrega assinada:** na `main`, a imagem é construída, **assinada** e só vai para produção
  depois de conferir **quem assinou** e de uma aprovação humana — e isso **ainda não é SLSA L2**
  ([`docs/06` §5](../../docs/06-pipeline-cicd.md#5-entrega-segura-já-pré-fiada-assinatura-verificação-e-promoção)).
- **Segredo no código:** senha ou chave escrita num arquivo do repositório.

---

## Passo 1 — Leia os gates de uma execução

> 🔎 **Como o Grupo 5 fez:** leu o **Placar de gates SSDLC** de uma execução antes e de outra
> depois da PoC e montou a tabela, com opinião justificada por gate. Veja:
> [`docs/12` §8](../../docs/12-exemplo-dossie-preenchido.md#8-fase-6--pipeline-gerador-de-evidência).

**▶ Agora no G2:**

1. **A execução de "antes"** (o código antes da correção da Fase 4):
   1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
   2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
   3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com o nome da
      branch da correção (`fix/F-02-idor`). A execução **logo abaixo** dela é a de **antes**:
      clique no título dela.
   4. Role a página até a tabela **"Placar de gates SSDLC"**. Anote o resultado de cada gate
      (`success` = verde, `failure` = vermelho) e tire um print (**Shift+Print Screen**).
   5. Copie a URL da barra do navegador para o rascunho.
2. **A execução de "depois":** volte à lista (**Actions** → **SSDLC - VidaPlus**, filtro
   **Branch** = `main`), clique no título da execução **"Merge pull request #… fix/F-02-idor"** e
   faça a mesma anotação: placar, print e URL.
3. **O que cada gate verifica:** na execução de "depois", clique em cada job da lista da esquerda.
   O nome das etapas e os comandos mostram o que ele faz. A tabela de todos os jobs, em palavras,
   está em [`comum/06` §3.4](../comum/06-pipeline-do-zero.md#34-todos-os-jobs-do-vidaplus), e cada
   ferramenta em [`comum/07`](../comum/07-ferramentas-de-seguranca.md).
4. Monte a tabela. Para o G2, pense em especial:
   - **Testes de segurança:** o seu cenário ficou verde, mas o job continua vermelho. Por quê?
   - **SAST:** o número de achados mudou com a sua correção? (Releia a Fase 5: o SAST não via o
     IDOR.) O que isso diz sobre usar **só** scanners como gate?
   - **DAST:** algum alerta fala da pasta `/laudos/`?
   - **"Deveria ser bloqueante?"** por gate, com motivo. Um gate só com os testes do seu pacote
     poderia bloquear hoje? (Opcional: [`docs/13`](../../docs/13-gate-bloqueante-do-grupo.md).)

## Passo 2 — Ache o SBOM

> 🔎 **Como o Grupo 5 fez:** explicou o que é o SBOM e onde encontrá-lo (**Actions** → execução →
> **Artifacts** → `sbom`).

**▶ Agora no G2:** baixe e abra o SBOM:

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-idor"**) e role até a seção **Artifacts**, no fim da página.
2. Clique em **sbom**. O navegador baixa o arquivo `sbom.zip` para a pasta **Downloads**.
3. No terminal, descompacte e confira:

   ```bash
   mkdir -p ~/Downloads/artefato-sbom
   unzip -o ~/Downloads/sbom.zip -d ~/Downloads/artefato-sbom
   ls ~/Downloads/artefato-sbom
   ```

   Deve aparecer `sbom.cdx.json`.
4. Abra no VS Code: **File → Open File…** → `Downloads/artefato-sbom/sbom.cdx.json`.

Com o arquivo aberto, procure
(**Ctrl+F**) `spring-boot-starter-web` (a biblioteca que recebe os pedidos HTTP e onde o
`@PathVariable` funciona) e `mysql-connector-j`. Anote as versões. Explique **para que serve** o
SBOM com um exemplo: se sair uma falha grave numa dessas bibliotecas, como a clínica descobre se
está afetada?

## Passo 3 — Explique a entrega assinada e a proteção da `main`

> 🔎 **Como o Grupo 5 fez:** explicou `release-sign → deploy-staging → deploy-prod`, por que a
> verificação confere **quem** assinou (nunca `.*`), que isso **ainda não é SLSA L2** e que a
> proteção da `main` aplica "quem faz não aprova o próprio".

**▶ Agora no G2:**

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-idor"**). Na lista de jobs da esquerda, clique, um de cada vez, em `Release - build, push, sign & attest SBOM`,
   `Deploy staging + smoke` e `Deploy prod - verifica assinatura + aprovacao`. O último **pausa**
   esperando aprovação? Quem aprova?
2. Explique o que a assinatura garante e o que **não** garante.
3. Abra o PR da Fase 4 (**Pull requests** → **Closed**). Antes da aprovação, a caixa de merge
   mostrava **"Review required"**: isso é a proteção da `main`. Explique por que ela existe. **Não**
   tente fazer `push` direto na `main` para testar.

## Passo 4 — Ache e analise os segredos no código (F-10)

> 🔎 **Como o Grupo 5 fez:** tabela **onde | segredo | risco | plano**, terminando com: *"Como os
> segredos já estão no histórico do Git, removê-los dos arquivos não basta: os valores precisam ser
> trocados."* Classificou a senha do banco de teste do CI como **risco aceito**, com justificativa.

**▶ Agora no G2:**

1. **No pipeline:** abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-idor"**) → job **Gate - Secret Scanning
   (Gitleaks)** → clique na etapa **Gitleaks** e leia os achados (como ler cada campo:
   [`comum/07` §4](../comum/07-ferramentas-de-seguranca.md#4-gitleaks--procura-segredos-inclusive-no-passado)).
   **No seu computador**, rode e grave a saída:

   ```bash
   docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
     detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
     --log-opts="--all -- legacy-app" --no-banner --redact -v \
     | tee entregaveis/evidencias/fase6-gitleaks.txt
   ```

   O `--redact` é **obrigatório** aqui: sem ele, os valores dos segredos iriam para o arquivo de
   evidência e, com o commit, de novo para o Git. A última linha deve dizer `leaks found: 8`
   (se o grupo não tiver mexido nesses arquivos).
2. Para cada achado: arquivo, linha, **o que é** o segredo.
3. **Ligue com o G2:** a correção de vocês confia no **token** para saber quem é a pessoa. Se a
   chave que assina o token (`JwtUtil.java`) vazar, alguém fabrica um token "da Ana" — e a regra
   de vocês entrega os exames dela, **corretamente**, ao invasor. Escreva esse risco: ele mostra
   por que o segredo é um elo do **seu** controle.
4. Se o Gitleaks acusar um arquivo que **não existe mais**, explique o que isso prova.
5. Escreva o **plano**: tirar do código (variável de ambiente; `.env` fora do Git com um
   `.env.example`), **trocar o valor**, e o que fazer com os segredos de teste.

**Não altere o código nem o pipeline** nesta fase.

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

- [ ] A tabela de gates tem estado antes/depois e opinião justificada por gate.
- [ ] Expliquei por que o SAST não serve de gate para IDOR.
- [ ] Achei o SBOM e expliquei para que serve.
- [ ] Expliquei assinatura, verificação de quem assinou e por que não é SLSA L2.
- [ ] Cada segredo tem risco concreto e plano (incluindo **trocar** o valor).
