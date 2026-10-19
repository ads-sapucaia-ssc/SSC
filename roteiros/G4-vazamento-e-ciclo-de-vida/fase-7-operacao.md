# G4 · Fase 7 — Operação e resposta a incidente

> **Seção do dossiê:** §9 · **Lidera:** 🛡️ Segurança (runbook) e 📋 DPO (notificação) ·
> **Aula:** 9 · **Semana:** 14
> No G4 esta fase é **o coração do tema**: o incidente típico de vocês é exatamente o vazamento.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. **Operação:** para cada canal e para a rotina de retenção, o que é registrado, como vira alerta
   e quem é avisado.
2. A **interface com a trilha de auditoria** (F-05, que é do G1).
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
- **Nunca** gravar no registro: senha, token, conteúdo do laudo.
- **Runbook:** detecção → contenção → erradicação → recuperação → notificação → lições. **Conter
  antes de erradicar**; **nunca apagar o log**.
- **LGPD art. 48:** incidente com risco relevante → ANPD e titulares em até **3 dias úteis**
  (Resolução CD/ANPD nº 15/2024).
- **Ligação com a F-06:** um dado que **já deveria ter sido eliminado** e vaza é um incidente
  **agravado** — a clínica nem deveria tê-lo.

---

## Passo 1 — Descubra o que o sistema registra hoje, canal por canal

> 🔎 **Como o Grupo 5 fez:** acompanhou o log **durante um ataque que deu certo**: **nenhuma
> linha**. Concluiu: *"o log mostra quem tentou e errou, não quem conseguiu"*. Leia a 💡 no
> [`docs/12` §9](../../docs/12-exemplo-dossie-preenchido.md#9-fase-7--operação--resposta-a-incidente).

**▶ Agora no G4** — os canais têm logs **diferentes**. 

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

1. A Ana pede o exame 102 (do Bruno) pela API:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA"
   ```

2. Alguém **sem login** baixa o laudo 104 direto do servidor de arquivos:

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' localhost:4200/laudos/laudo-104.pdf
   ```

3. Alguém pede um laudo que **não existe** (105):

   ```bash
   curl -s -o /dev/null -w '%{http_code} %{content_type}\n' localhost:4200/laudos/laudo-105.pdf
   ```

4. A Ana pede, pela API, o PDF de um exame que **não existe** (999):

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/999/pdf -H "Authorization: Bearer $TOKEN_ANA"
   ```

Para cada um, compare os Terminais 1 e 2. O nginx registra o download: **com que** informações? Ele
sabe **quem** baixou? A API registrou o acesso negado? Se uma lista de laudos aparecesse amanhã num
fórum, por qual log vocês descobririam **quais** arquivos saíram e **de onde**?

Ao terminar, em cada um dos Terminais 1 e 2, clique nele e aperte **Ctrl+C** (o VidaPlus continua
ligado). Os logs dos containers **somem** quando eles são recriados (o `zerar-banco.sh` faz isso);
para guardá-los como evidência, no Terminal 3:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend --tail 200 > entregaveis/evidencias/fase7-log-api.txt
docker compose -f legacy-app/docker-compose.yml logs frontend --tail 200 > entregaveis/evidencias/fase7-log-nginx.txt
```

## Passo 2 — Descreva a operação de cada canal

> 🔎 **Como o Grupo 5 fez:** para cada funcionalidade, **o que é registrado** (e o que **nunca** é),
> **como vira alerta** (regras com números) e **quem é avisado**, mais a **situação**.

**▶ Agora no G4:** para **API, arquivo, erros, notificação, site e rotina de retenção**. Perguntas:

- **API:** registrar as negações? Que padrão de 403 indica alguém percorrendo números?
- **Arquivo:** o nginx já registra. Que regra pega um download em sequência (101, 102, 103…) de um
  mesmo IP? Quando os PDFs saírem da pasta pública (plano), como o download passa a registrar
  **quem**?
- **Erros:** o detalhe que saiu da resposta agora vai **só para o log**. Um pico de erros 500
  indica o quê?
- **Notificação (planejada):** registrar o envio **sem** o conteúdo? Monitorar falhas de entrega?
- **Site:** como perceber que a página inicial mudou sem uma mudança aprovada? (Comparar com o que
  o pipeline publicou?)
- **Rotina de retenção:** quantos registros eliminou, se falhou, se eliminou **muito mais** que o
  normal (regra de prazo errada!) — e quem é avisado (o DPO).

## Passo 3 — A interface com a trilha de auditoria (F-05, do G1)

> 🔎 **Como o Grupo 5 fez:** escreveu **o que precisava da trilha** de outro pacote: o evento
> `PACIENTE_ANONIMIZADO`, com o paciente identificado só pelo número interno — porque isso permite
> **provar** à ANPD que a regra de retenção é cumprida.

**▶ Agora no G4:** que eventos vocês precisam (ex.: `LAUDO_BAIXADO`, `ACESSO_NEGADO`,
`NOTIFICACAO_ENVIADA`, `DADOS_ELIMINADOS`, `PEDIDO_ELIMINACAO_RESPONDIDO`)? Com que campos? Como o
registro de uma **eliminação** prova que ela aconteceu **sem guardar o dado eliminado**?

## Passo 4 — Escreva o runbook do incidente mais grave

> 🔎 **Como o Grupo 5 fez:** *"vazamento de cadastros pela busca"*, seis passos com ação, quem e
> evidência; honesto na detecção; *"copiar os logs antes de qualquer reinício"*. Veja:
> [`docs/12` §9.1](../../docs/12-exemplo-dossie-preenchido.md#91-runbook-de-incidente--vazamento-de-cadastros-pela-busca-incidente-do-nosso-pacote).

**▶ Agora no G4:** o incidente natural é **"vazamento de laudos"** — o modelo da Aula 9. Escolham
o canal **mais provável** (Fase 2a: o mais barato da árvore) e escrevam para ele.

| Passo | Perguntas para o G4 |
|---|---|
| 1 Detecção | por qual log (Passo 1)? Ou por aviso de fora (um paciente encontrou o laudo dele num site)? Como preservar? |
| 2 Contenção | tirar `/laudos/` do ar no nginx? O paciente legítimo fica sem o laudo — por quanto tempo? Invalidar os links já enviados (notificação)? |
| 3 Erradicação | o PR do P0 + o plano do arquivo |
| 4 Recuperação | a tabela de canais (Fase 4) toda negando; testes verdes |
| 5 Notificação | Passo 5 |
| 6 Lições | que requisito nasce? Que dado vazou que **já deveria ter sido eliminado** (F-06)? |

## Passo 5 — Notificação à ANPD e aos titulares (art. 48)

> 🔎 **Como o Grupo 5 fez:** risco relevante (sim — dado sensível), prazo (3 dias úteis), os 6
> itens do art. 48 §1º, **pior caso** quando não havia registro, e o **registro interno**
> obrigatório. Veja: [`docs/12` §9.2](../../docs/12-exemplo-dossie-preenchido.md#92-notificação-lgpd-art-48).

**▶ Agora no G4:**

1. **Há risco relevante?** Resultado de exame é dado de saúde. E um laudo **não publicado** — o
   paciente pode saber de um resultado grave por terceiros, antes do médico.
2. **Prazo, quem envia, os 6 itens.**
3. **Quantos titulares?** O log do nginx mostra **quais arquivos** saíram (Passo 1): use-o para
   delimitar. Se não houver log suficiente, o pior caso.
4. **Agravante:** algum dos laudos vazados era de alguém cujo prazo de guarda já tinha acabado?
   Como a clínica explica, na comunicação, por que ainda tinha o dado?
5. **Recomendação aos titulares.**

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

- [ ] Comparei os logs da API e do nginx em cada canal.
- [ ] Cada canal e a rotina de retenção têm registro, alerta com números e responsável.
- [ ] Escrevi a interface com a F-05, inclusive como provar uma eliminação.
- [ ] O runbook é do canal mais provável, com ação, quem e evidência nos seis passos.
- [ ] A notificação usa o log do nginx para delimitar os titulares (ou assume o pior caso).
