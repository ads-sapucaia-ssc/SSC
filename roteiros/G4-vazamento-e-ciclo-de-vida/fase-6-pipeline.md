# G4 · Fase 6 — Pipeline e segredos no código

> **Seção do dossiê:** §8 · **Lidera:** ⚙️ DevSecOps · **Aula:** 8 · **Semana:** 13
> Nesta fase vocês **não configuram o pipeline**: os gates ficam informativos e a proteção da
> `main` é do professor. O trabalho é **ler** o pipeline e **explicar** — e há **uma correção
> guiada no código**: tirar os segredos do código (F-10), no Passo 5.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. A tabela de **gates**: o que verifica, estado antes e depois da PoC, e se deveria ser
   bloqueante num projeto real (e por quê).
2. A explicação do **SBOM**, da **entrega assinada** e da **proteção da `main`** — no G4, ligadas
   à F-20 (integridade do site).
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

- **Pipeline:** as verificações que o GitHub roda a cada push (`.github/workflows/ssdlc-pipeline.yml`).
  Cada uma é um **job**; os de segurança são **gates**.
- **Informativo × bloqueante:** aqui todos são informativos ([`docs/06` §2](../../docs/06-pipeline-cicd.md#2-informativo--bloqueante)).
- **SBOM:** a "lista de ingredientes" do software.
- **Entrega assinada:** imagem construída, **assinada** e só promovida a produção depois de
  conferir **quem assinou** e de uma aprovação humana — e **ainda não é SLSA L2**
  ([`docs/06` §5](../../docs/06-pipeline-cicd.md#5-entrega-segura-já-pré-fiada-assinatura-verificação-e-promoção)).
- **Segredo no código:** senha ou chave escrita num arquivo do repositório.

**A ligação do G4 com esta fase:** o **site** (F-20) é o que o nginx serve — e o nginx roda a
**imagem** que o pipeline constrói e assina. A pergunta "quem consegue trocar a página inicial da
clínica?" tem resposta aqui: só quem consegue fazer uma mudança **chegar à `main`** e passar pela
assinatura e pela aprovação.

---

## Passo 1 — Leia os gates de uma execução

> 🔎 **Como o Grupo 5 fez:** leu o **Placar de gates SSDLC** antes e depois da PoC e montou a
> tabela com opinião justificada por gate. Veja:
> [`docs/12` §8](../../docs/12-exemplo-dossie-preenchido.md#8-fase-6--pipeline-gerador-de-evidência).

**▶ Agora no G4:**

1. **A execução de "antes"** (o código antes da correção da Fase 4):
   1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
   2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
   3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com o nome da
      branch da correção (`fix/F-02-canais-do-laudo`). A execução **logo abaixo** dela é a de **antes**:
      clique no título dela.
   4. Role a página até a tabela **"Placar de gates SSDLC"**. Anote o resultado de cada gate
      (`success` = verde, `failure` = vermelho) e tire um print (**Shift+Print Screen**).
   5. Copie a URL da barra do navegador para o rascunho.
2. **A execução de "depois":** volte à lista (**Actions** → **SSDLC - VidaPlus**, filtro
   **Branch** = `main`), clique no título da execução **"Merge pull request #… fix/F-02-canais-do-laudo"** e
   faça a mesma anotação: placar, print e URL.
3. **O que cada gate verifica:** na execução de "depois", clique em cada job da lista da esquerda.
   O nome das etapas e os comandos mostram o que ele faz. A tabela de todos os jobs, em palavras,
   está em [`comum/06` §3.4](../comum/06-pipeline-do-zero.md#34-todos-os-jobs-do-vidaplus), e cada
   ferramenta em [`comum/07`](../comum/07-ferramentas-de-seguranca.md).
4. Monte a tabela. Para o G4, em especial: **DAST** — os alertas de erro e de cabeçalhos mudaram
   com a correção (Fase 5)? Um gate de DAST bloqueante, com a regra 90022 como falha, teria
   impedido o *stack trace* de voltar? "Deveria ser bloqueante?" por gate, com motivo.

## Passo 2 — Ache o SBOM

> 🔎 **Como o Grupo 5 fez:** explicou o que é o SBOM e onde encontrá-lo (**Actions** → execução →
> **Artifacts** → `sbom`).

**▶ Agora no G4:** baixe e abra o SBOM:

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-canais-do-laudo"**) e role até a seção **Artifacts**, no fim da página.
2. Clique em **sbom**. O navegador baixa o arquivo `sbom.zip` para a pasta **Downloads**.
3. No terminal, descompacte e confira:

   ```bash
   mkdir -p ~/Downloads/artefato-sbom
   unzip -o ~/Downloads/sbom.zip -d ~/Downloads/artefato-sbom
   ls ~/Downloads/artefato-sbom
   ```

   Deve aparecer `sbom.cdx.json`.
4. Abra no VS Code: **File → Open File…** → `Downloads/artefato-sbom/sbom.cdx.json`.

Com o arquivo aberto, repare: ele lista as
bibliotecas do **backend**. E o **nginx** que serve os laudos e o site — aparece? O nginx não está no SBOM do
backend. Para descobrir a versão dele, de dois jeitos:

- pelo **arquivo que monta a imagem do site**: abra `legacy-app/frontend/Dockerfile` (**Ctrl+P** →
  `frontend/Dockerfile`) e leia a linha `FROM nginx:...`;
- pela **resposta do próprio servidor**:

  ```bash
  curl -s -I localhost:4200/ | grep -i "^server"
  ```

  Esperado: uma linha como `Server: nginx/1.27.5`. (Repare: o servidor **anuncia** a versão a qualquer visitante — o ZAP chama isso de "Server
  leaks version", regra 10036.)

E para ver se essa imagem tem CVEs: na execução de "depois", clique no job **Gate - Container
Scanning (Trivy image)** → etapa **Trivy - imagem do frontend**. O Trivy imprime uma tabela com as
falhas conhecidas encontradas nos programas da imagem (coluna `Library` = o programa; `Severity` =
gravidade).
Explique para que serve o SBOM com esse exemplo: se sair uma falha grave no nginx, como a clínica
descobre se está afetada?

## Passo 3 — Explique a entrega assinada como proteção do site (F-20)

> 🔎 **Como o Grupo 5 fez:** explicou `release-sign → deploy-staging → deploy-prod`, por que a
> verificação confere **quem** assinou (nunca `.*`), que isso **ainda não é SLSA L2**, e que a
> proteção da `main` aplica "quem faz não aprova o próprio".

**▶ Agora no G4:**

1. Abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-canais-do-laudo"**). Na lista de jobs da esquerda, clique, um de cada vez, em `Release - build, push, sign & attest SBOM`,
   `Deploy staging + smoke` e `Deploy prod - verifica assinatura + aprovacao`. O último **pausa**
   esperando aprovação?
2. **Monte o caminho de uma mudança no site**, do computador de um integrante até a produção, e
   marque **cada controle** que ela atravessa: PR → revisão de outro integrante (proteção da
   `main`) → pipeline → imagem assinada → verificação de **quem** assinou → aprovação de produção.
   Esse caminho **é** o controle anti-desfiguração (*anti-defacement*) da F-20. O que **ainda
   falta**? (Ex.: alguém com acesso ao servidor trocar o arquivo **depois** do deploy; o navegador
   sem CSP executar um script injetado.)
3. Abra o PR da Fase 4 (**Pull requests** → **Closed**): antes da aprovação, a caixa de merge
   mostrava **"Review required"**. **Não** tente fazer `push` direto na `main` para testar.

## Passo 4 — Ache e analise os segredos no código (F-10)

> 🔎 **Como o Grupo 5 fez:** tabela **onde | segredo | risco | plano**, terminando com: *"Como os
> segredos já estão no histórico do Git, removê-los dos arquivos não basta: os valores precisam ser
> trocados."* A senha do banco de teste do CI virou **risco aceito**, com justificativa.

**▶ Agora no G4:**

1. **No pipeline:** abra a execução de "depois" (**Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → clique na execução **"Merge pull request #… fix/F-02-canais-do-laudo"**) → job **Gate - Secret Scanning
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
2. Para cada achado: arquivo, linha, **o que é** o segredo.
3. **Para o G4, o repositório é um canal de vazamento:** a senha do banco no `docker-compose.yml`
   dá acesso a **todos** os laudos, sem passar por nenhum controle da API. E a porta 3306 está
   aberta na máquina. Escrevam esse risco como um **canal**.
4. **Ciclo de vida do segredo:** o Gitleaks varre **o histórico**. Se ele acusar um arquivo que
   **não existe mais**, o que isso prova? É a mesma lição da F-06: **apagar do lugar visível não é
   eliminar**.
5. **O plano:** tirar do código (variável de ambiente; `.env` fora do Git com um `.env.example`),
   **trocar o valor**, e o que fazer com os segredos de teste.

Este passo é só a **análise**: não mexa no código ainda. A correção vem no Passo 5.

## Passo 5 — Corrija os segredos no código (F-10) — aula guiada

> 🔎 **Como o Grupo 5 fez:** seguiu o roteiro comum dos segredos, guardou as provas do antes e do
> depois e escreveu na §8 o que foi feito, o requisito REQ-SEGREDO-01 nas duas formas e o que
> **ainda** fica em aberto. Veja: [`docs/12` §8](../../docs/12-exemplo-dossie-preenchido.md#8-fase-6--pipeline-gerador-de-evidência).

**▶ Agora no G4:**

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
   correção já na `main`. É o **mesmo** para os quatro grupos; o que muda no G4 está no item 3.
3. **O que muda no G4:** o repositório era um **canal de vazamento** — a senha do banco dava
   acesso a todos os laudos sem passar pela API. No **Passo 8.4 do roteiro comum**, rodem os dois
   comandos desta forma, que grava as saídas como evidência do canal fechado:

   ```bash
   docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT 1;" \
     2>&1 | tee entregaveis/evidencias/fase6-f10-g4-banco-senha-antiga.txt
   docker compose -f legacy-app/docker-compose.yml exec db sh -c 'mysql -uvidaplus -p"$MYSQL_PASSWORD" vidaplus -e "SELECT COUNT(*) AS usuarios FROM usuario;"' \
     2>&1 | tee entregaveis/evidencias/fase6-f10-g4-banco-forma-nova.txt
   ```

   Esperado: no primeiro, `ERROR 1045 (28000): Access denied`; no segundo, `usuarios` e `6`. É também
   uma lição de **ciclo de vida**: o segredo antigo continua no histórico (não dá para eliminá-lo),
   mas **perdeu a validade** — o mesmo raciocínio da F-06 (o que não se pode apagar, se torna
   inútil).
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
     processo de **rotação**); e, no G4, a porta 3306 do banco continua publicada na máquina (`ports: "3306:3306"` no
   `docker-compose.yml`): com a senha nova ela está protegida, mas o canal existe — fica no plano.

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| `unzip: cannot find or open ~/Downloads/sbom.zip` | o arquivo do SBOM ainda não foi baixado, ou o navegador o salvou com outro nome | baixe de novo (**Actions** → execução → **Artifacts** → **sbom**) e confira o nome na pasta **Downloads**; se for outro (ex.: `sbom (1).zip`), use esse nome entre aspas no `unzip` |
| No Passo 8.4 da aula guiada, o primeiro comando **entrou** no banco | as senhas ainda não foram trocadas (o `.env` não foi usado, ou o banco não foi zerado) | volte ao Passo 8.1 do [`comum/08`](../comum/08-segredos-fora-do-codigo.md) e rode o `zerar-banco.sh` |
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

- [ ] A tabela de gates tem estado antes/depois e opinião justificada — inclusive sobre o DAST.
- [ ] Sei onde está o SBOM e onde aparece a versão do nginx.
- [ ] Desenhei o caminho de uma mudança no site e os controles que ela atravessa.
- [ ] Cada segredo tem risco concreto e plano — e o repositório aparece como canal.
- [ ] Fiz a correção guiada da F-10 e registrei o canal "senha do banco no repositório" fechado
  (senha antiga recusada).
