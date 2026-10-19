# G2 · Fase 7 — Operação e resposta a incidente

> **Seção do dossiê:** §9 · **Lidera:** 🛡️ Segurança (runbook) e 📋 DPO (notificação) ·
> **Aula:** 9 · **Semana:** 14

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. **Operação:** para cada funcionalidade do pacote, o que é registrado, como vira alerta e quem é
   avisado.
2. A **interface com a trilha de auditoria** (F-05, que é do G1): o que vocês precisam que ela
   registre.
3. **Runbook** (§9.1) do incidente mais grave do pacote.
4. **Notificação** à ANPD e aos titulares (§9.2, art. 48).

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
   git checkout -b dossie/fase-7
   git push -u origin dossie/fase-7
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-7
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

- **Log** é o diário técnico; **trilha de auditoria** é registro com valor de **prova**.
- **Nunca** gravar no registro: senha, token, conteúdo do exame. Identifique pessoas pelo
  **número interno**.
- **Runbook:** detecção → contenção → erradicação → recuperação → notificação → lições. **Conter
  antes de erradicar**; **nunca apagar o log**.
- **LGPD art. 48:** incidente com risco relevante → comunicar ANPD e titulares em até **3 dias
  úteis** (Resolução CD/ANPD nº 15/2024).

---

## Passo 1 — Descubra o que o sistema registra hoje

> 🔎 **Como o Grupo 5 fez:** acompanhou o log **durante um ataque que deu certo**: **nenhuma
> linha**. Concluiu: *"o log mostra quem tentou e errou, não quem conseguiu"*. Leia a 💡 no
> [`docs/12` §9](../../docs/12-exemplo-dossie-preenchido.md#9-fase-7--operação--resposta-a-incidente).

**▶ Agora no G2** — o laudo sai por **dois caminhos**, e cada um tem o seu log. 

Você vai usar **três terminais** ao mesmo tempo. No painel do terminal do VS Code, o **+** (à
direita) abre um terminal novo; a lista à direita do painel mostra todos, e você troca de um para
outro clicando no nome. Todos abrem na raiz do repositório.

**Terminal 1 — o log da API (backend).** Rode e deixe parado:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend -f --tail 0
```

**Terminal 2 — o log do servidor de arquivos e do site (nginx).** Abra outro terminal (**+**),
rode e deixe parado:

```bash
docker compose -f legacy-app/docker-compose.yml logs frontend -f --tail 0
```

(O `-f` deixa o terminal mostrando cada linha nova do log assim que ela aparece; o `--tail 0`
esconde as antigas.)

**Terminal 3 — os pedidos.** Abra um terceiro terminal (**+**) e prepare os tokens:

```bash
source roteiros/comum/tokens.sh
```

Agora rode **um bloco de cada vez** e, depois de cada um, olhe os Terminais 1 e 2.

1. A Ana abre o **próprio** exame pela API:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANA"
   ```

2. A Ana tenta o exame **do Bruno** pela API (depois da correção, deve dar 403):

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA"
   ```

3. Alguém **sem login** baixa o laudo 104 direto do servidor de arquivos:

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' localhost:4200/laudos/laudo-104.pdf
   ```

No Terminal 3, as respostas esperadas são `200`, `403` e `200`. Nos logs, o esperado é: no
**Terminal 1 (API)**, **nenhuma** linha nova nos pedidos 1 e 2; no **Terminal 2 (nginx)**, uma
linha no pedido 3, parecida com
`172.18.0.1 - - [19/Oct/2026:15:50:40 +0000] "GET /laudos/laudo-104.pdf HTTP/1.1" 200 1040 "-" "curl/8.18.0"`.

Compare os Terminais 1 e 2. O nginx registrou o download: **com que** informações (endereço IP,
data, arquivo, programa usado)? Ele sabe **quem** baixou? (Por que não?) E a API, registrou o
acesso da Ana — permitido ou negado? Se um laudo vazasse ontem, o que vocês conseguiriam descobrir
hoje, e por qual caminho?

Ao terminar, em cada um dos Terminais 1 e 2, clique nele e aperte **Ctrl+C** (o VidaPlus continua
ligado). Os logs dos containers **somem** quando eles são recriados (o `zerar-banco.sh` faz isso);
para guardá-los como evidência, no Terminal 3:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend --tail 200 > entregaveis/evidencias/fase7-log-api.txt
docker compose -f legacy-app/docker-compose.yml logs frontend --tail 200 > entregaveis/evidencias/fase7-log-nginx.txt
```

## Passo 2 — Descreva a operação de cada funcionalidade

> 🔎 **Como o Grupo 5 fez:** para cada funcionalidade, **o que é registrado** (e o que **nunca** é),
> **como vira alerta** (regras com números) e **quem é avisado**, mais a **situação** ("planejado").

**▶ Agora no G2:** para **acesso ao exame, download do PDF, publicação e exportação**. Perguntas:

- **Acesso ao exame:** registrar acessos **negados** (403) também? Que padrão de 403 indica alguém
  "percorrendo" números? Que padrão indica um médico acessando pacientes demais?
- **Cuidado (LINDDUN, Fase 2b):** registrar "paciente X abriu o exame de sorologia às 22h" é
  necessário? Quem pode ler esse registro? O que vai nele — o **número** do exame ou o **tipo**?
- **Download do PDF:** depois que o arquivo sair da pasta pública (plano), como o download passa a
  ser registrado com **quem** baixou?
- **Publicação:** o `publicado_por` já guarda **quem** publicou. Falta **o quê** (hash, data,
  versão)?
- **Exportação:** registrar cada pedido (é o `SolicitacaoTitular`). Alerta para muitos pedidos?
- **Quem é avisado**, por qual canal, e a **situação** (hoje: nada → "planejado").

## Passo 3 — A interface com a trilha de auditoria (F-05, do G1)

> 🔎 **Como o Grupo 5 fez:** como a trilha era de outro pacote, escreveu **o que precisava dela**:
> *"cada anonimização gera o evento `PACIENTE_ANONIMIZADO`, com o paciente identificado só pelo
> número interno"* — e por quê (provar à ANPD que a regra é cumprida).

**▶ Agora no G2:** que **eventos** do seu pacote a trilha precisa registrar (ex.:
`EXAME_ACESSADO`, `EXAME_ACESSO_NEGADO`, `LAUDO_PUBLICADO`, `DADOS_EXPORTADOS`)? Com que campos?
Com que **cuidado** de privacidade? Para que servem (que pergunta eles respondem depois de um
incidente)?

## Passo 4 — Escreva o runbook do incidente mais grave

> 🔎 **Como o Grupo 5 fez:** *"vazamento de cadastros pela busca"*, seis passos com ação, quem e
> evidência. Na detecção, honesto: *"o ataque que dá certo hoje não deixa rastro"*. Na evidência:
> *"copiar os logs antes de qualquer reinício"*. Veja: [`docs/12` §9.1](../../docs/12-exemplo-dossie-preenchido.md#91-runbook-de-incidente--vazamento-de-cadastros-pela-busca-incidente-do-nosso-pacote).

**▶ Agora no G2:** candidato natural: **"vazamento de laudos"** — por IDOR na API ou pela pasta
pública. O modelo da Aula 9 é exatamente este caso; adapte ao que **vocês** viram.

| Passo | Perguntas para o G2 |
|---|---|
| 1 Detecção | por qual log vocês perceberiam (Passo 1)? O nginx mostra downloads em sequência (101, 102, 103…) de um mesmo IP? Como preservar? |
| 2 Contenção | tirar a pasta `/laudos/` do ar? Isso impede o paciente legítimo de baixar o laudo — vale a pena? Por quanto tempo? |
| 3 Erradicação | a correção do P0 (PR) e o plano do F-08 |
| 4 Recuperação | testes verdes + repetir a tabela de acessos por papel (Fase 4) |
| 5 Notificação | Passo 5 |
| 6 Lições | que requisito nasce? (Pense no achado da tabela `atendimento`, Fase 5.) |

## Passo 5 — Notificação à ANPD e aos titulares (art. 48)

> 🔎 **Como o Grupo 5 fez:** risco relevante (sim — dado sensível), prazo (3 dias úteis), os 6
> itens do art. 48 §1º, **pior caso** quando não havia registro, e o **registro interno**
> obrigatório. Veja: [`docs/12` §9.2](../../docs/12-exemplo-dossie-preenchido.md#92-notificação-lgpd-art-48).

**▶ Agora no G2:**

1. **Há risco relevante?** Resultado de exame é dado de saúde (art. 11). E um laudo **não
   publicado** que vazou — o paciente pode ficar sabendo por terceiros antes do médico. Justifique.
2. **Prazo e quem envia.**
3. **Os 6 itens.**
4. **Quantos titulares?** O nginx mostra **quais arquivos** foram baixados (Passo 1): isso ajuda a
   delimitar? E pela API, sem registro?
5. **Recomendação aos titulares.**

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Nenhuma linha aparece no Terminal 2 (nginx) depois do pedido 3 | o Terminal 2 não está acompanhando o log do `frontend` | clique no Terminal 2: a última linha deve ser o comando `logs frontend -f`, sem o prompt `$` depois; se tiver, rode-o de novo |
| Os arquivos `fase7-log-*.txt` ficaram sem as linhas dos pedidos | o VidaPlus foi religado depois dos pedidos (o log começa do zero) | repita os pedidos do Passo 1 e grave os logs em seguida, sem zerar o banco no meio |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-7`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 7 - operacao"
git push -u origin dossie/fase-7
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
   **compare** escolha `dossie/fase-7` → **Create pull request**.
2. Título: "Dossiê — Fase 7: operação". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Comparei o log da API com o do nginx durante um acesso indevido.
- [ ] Cada funcionalidade tem registro (sem conteúdo clínico), alerta com números e responsável.
- [ ] Escrevi a interface com a F-05: eventos, campos e cuidados de privacidade.
- [ ] O runbook tem ação concreta, quem e evidência nos seis passos.
- [ ] A notificação responde aos 6 itens e diz como os titulares afetados foram identificados.
