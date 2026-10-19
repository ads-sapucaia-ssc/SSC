# G1 · Fase 7 — Operação, trilha de auditoria e resposta a incidente

> **Seção do dossiê:** §9 · **Lidera:** 🛡️ Segurança (runbook) e 📋 DPO (notificação) ·
> **Aula:** 9 · **Semana:** 14
> No G1 esta fase pesa mais: **a trilha de auditoria é de vocês**.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. **Operação:** para cada funcionalidade do pacote, o que é registrado, como vira alerta e quem é
   avisado.
2. **Garantias da trilha de auditoria** (F-05): append-only, sem dado pessoal, integridade
   verificável.
3. **Runbook** (§9.1) do incidente mais grave do pacote.
4. **Notificação à ANPD e aos titulares** (§9.2, LGPD art. 48).

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

- **Log** é o diário técnico do sistema; **trilha de auditoria** é um registro com valor de
  **prova**: quem fez o quê, quando — e ninguém consegue alterar depois.
- **Regra de ouro do registro:** **nunca** gravar senha, token, código de recuperação ou dado de
  saúde. Identifique as pessoas pelo **número interno** (pseudônimo).
- **Runbook:** o roteiro do que fazer num incidente, passo a passo, decidido **antes** de ele
  acontecer. As seis etapas: detecção, contenção, erradicação, recuperação, notificação, lições.
- **Conter antes de erradicar**, e **nunca apagar o log**: ele é a prova.
- **LGPD art. 48:** incidente com risco relevante ao titular → comunicar a ANPD e os titulares em
  até **3 dias úteis** (Resolução CD/ANPD nº 15/2024).

---

## Passo 1 — Descubra o que o sistema registra hoje

> 🔎 **Como o Grupo 5 fez:** acompanhou o log **durante um ataque que deu certo** (o login com
> `' -- `). Resultado: **nenhuma linha**. Só as tentativas malformadas deixavam `error in your SQL
> syntax`. Concluiu: *"o log mostra quem tentou e errou, não quem conseguiu"* — e criou um
> requisito de registro. Leia a 💡 "O ataque que dá certo é o que não deixa rastro" no
> [`docs/12` §9](../../docs/12-exemplo-dossie-preenchido.md#9-fase-7--operação--resposta-a-incidente).

**▶ Agora no G1:** você vai precisar de **dois terminais** ao mesmo tempo: um mostrando o log, e
outro onde você faz os pedidos.

**Terminal 1 — o log ao vivo.** No terminal que já está aberto (na raiz do repositório), rode:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend -f --tail 0
```

O `-f` ("seguir") deixa o terminal **parado**, mostrando cada linha nova do log do backend assim
que ela aparece; o `--tail 0` esconde as linhas antigas. Deixe-o assim.

**Terminal 2 — os pedidos.** No painel do terminal do VS Code, clique no **+** (à direita) para
abrir um segundo terminal. Ele também abre na raiz do repositório. Rode **um bloco de cada vez** e,
depois de cada um, clique no **Terminal 1** (na lista de terminais, à direita do painel) para ver
se apareceu alguma linha. Anote o resultado de cada bloco.

1. Um login **certo**:

   ```bash
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}'; echo
   ```

2. Um login com **senha errada**:

   ```bash
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"errada"}'; echo
   ```

3. **10 logins errados seguidos** (o `for` repete o pedido 10 vezes, com as senhas `errada1` a
   `errada10`, e mostra o código de cada um):

   ```bash
   for i in 1 2 3 4 5 6 7 8 9 10; do
     curl -s -o /dev/null -w '%{http_code} ' -X POST localhost:8080/api/auth/login \
       -H "Content-Type: application/json" -d "{\"email\":\"ana.paciente@email.com\",\"senha\":\"errada$i\"}"
   done; echo
   ```

4. Um pedido de **recuperação de senha**:

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com"}'; echo
   ```

5. Um pedido com um **token inválido** (o texto `token-falso` no lugar de um token de verdade):

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101 \
     -H "Authorization: Bearer token-falso"
   ```

O que cada pedido responde no **Terminal 2** (para você saber que ele rodou): o 1 devolve um
JSON com `access_token`; o 2, um texto longo com `"status":401`; o 3, dez vezes `401`; o 4, um
JSON com `"reset_token"`; o 5, `500`.

Para cada um, anote: apareceu alguma linha no Terminal 1? Ela diria **quem**, **quando** e **o
que**? Se alguém invadisse a conta da Ana ontem, vocês conseguiriam descobrir hoje?

Esperado no Terminal 1: nos pedidos 1 a 4, **nenhuma** linha nova; no 5, um `ERROR` longo com
`java.lang.RuntimeException: nao autenticado`. Leia esse resultado com as perguntas acima.

Ao terminar, clique no **Terminal 1** e aperte **Ctrl+C** para parar de acompanhar o log (o
VidaPlus continua ligado). Para guardar o log como evidência:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend --tail 100 > entregaveis/evidencias/fase7-log-backend.txt
```

Esperado: nada aparece na tela (a saída foi para o arquivo). Abra-o no VS Code (**Ctrl+P** →
`fase7-log-backend.txt`) e procure (**Ctrl+F**) `ERROR`.

## Passo 2 — Descreva a operação de cada funcionalidade

> 🔎 **Como o Grupo 5 fez:** para cada funcionalidade, três blocos — **o que é registrado** (e o
> que **nunca** é), **como vira alerta** (regras concretas, com números: *"mais de 30 buscas em 5
> minutos"*) e **quem é avisado** — mais a **situação** ("planejado"). Veja: [`docs/12` §9](../../docs/12-exemplo-dossie-preenchido.md#9-fase-7--operação--resposta-a-incidente).

**▶ Agora no G1:** para **login, sessão, recuperação de senha e cabeçalhos**, escreva os três
blocos. Perguntas que ajudam:

- **Login:** que eventos registrar (sucesso, falha, conta bloqueada)? Que identificador usar
  numa falha com e-mail **que não existe** (o e-mail digitado é dado pessoal — gravar ou não?)?
  Que regra de alerta pega uma força bruta? E um ataque "espalhado" (uma senha comum testada em
  muitas contas)?
- **Sessão:** registrar o logout? Registrar token recusado? Que padrão indicaria um token
  roubado (o mesmo token usado de dois lugares)?
- **Recuperação de senha:** um pico de pedidos indica o quê?
- **Quem é avisado** e por qual canal; e a **situação** (hoje nada disso existe → "planejado").

## Passo 3 — Descreva as garantias da trilha de auditoria (F-05)

O `DOSSIE.md` pede, para quem tem a F-05: a trilha é **append-only**? **sem dado pessoal** em
texto claro? tem **integridade verificável**?

> 🔎 **Como o Grupo 5 fez:** como a trilha era de outro pacote, descreveu só a **interface** (o
> evento que precisava, identificado só pelo número interno). Vocês vão além: a trilha é de vocês.

**▶ Agora no G1:** retome a especificação da Fase 4 (Parte C) e responda, uma garantia por vez,
**como** ela é obtida e **como se verifica**:

| Garantia | Como é obtida | Como verificar (procedimento) |
|---|---|---|
| append-only | ex.: o usuário do banco da aplicação não tem permissão de `UPDATE`/`DELETE` na tabela | ex.: tentar um `DELETE` com esse usuário e ver o erro |
| sem dado pessoal | ... | ... |
| integridade (encadeamento de hash) | ... | ex.: percorrer a tabela recalculando cada hash; a primeira linha que não bate mostra onde houve adulteração |

Diga também **quem pode ler** a trilha e **por quanto tempo** ela é guardada (Fase 2b).

## Passo 4 — Escreva o runbook do incidente mais grave

> 🔎 **Como o Grupo 5 fez:** escolheu *"vazamento de cadastros pela busca"* e preencheu os seis
> passos com **ação concreta, quem faz e o que registrar**. Na detecção, foi honesto: *"o ataque
> que dá certo hoje não deixa rastro"*. Na contenção, incluiu *"trocar o segredo do JWT (como o
> token não expira, é o único jeito de derrubar sessões roubadas)"*. Na evidência do passo 1:
> *"copiar os logs para um arquivo antes de qualquer reinício: eles somem quando o container é
> recriado"*. Veja a tabela em [`docs/12` §9.1](../../docs/12-exemplo-dossie-preenchido.md#91-runbook-de-incidente--vazamento-de-cadastros-pela-busca-incidente-do-nosso-pacote).

**▶ Agora no G1:** escolha o incidente mais grave do **seu** pacote. Candidato natural:
**"tomada da conta de um ADMIN"** (por injeção, por senha descoberta ou por token vazado). Para
cada passo:

| Passo | Perguntas para o G1 |
|---|---|
| 1 Detecção | como vocês perceberiam? Hoje, com o log que vocês viram no Passo 1, perceberiam? E depois da F-05? Como preservar a prova (`docker compose -f legacy-app/docker-compose.yml logs backend > ...txt`)? |
| 2 Contenção | como derrubar **já** a sessão do invasor, se o token não expira? (Pista: vocês já fizeram isso na Fase 6 — trocar a `JWT_SECRET` do `legacy-app/.env` e religar o backend; o que acontece com **todos** os tokens?) Desativar a conta? Trocar senhas? |
| 3 Erradicação | qual a causa-raiz e qual PR a corrige? |
| 4 Recuperação | como saber que voltou a ser seguro? (Testes verdes + repetir o ataque à mão.) |
| 5 Notificação | ver Passo 5 |
| 6 Lições | que requisito nasce? Que gate deveria virar bloqueante? |

## Passo 5 — Notificação à ANPD e aos titulares (art. 48)

> 🔎 **Como o Grupo 5 fez:** respondeu se havia **risco relevante** (sim: religião é dado sensível),
> o **prazo** (3 dias úteis), os **6 itens** do art. 48, §1º — e, como não havia registro de
> acesso, comunicou **o pior caso** (todos os pacientes). Acrescentou o **registro interno** do
> incidente, obrigatório mesmo sem notificação. Veja: [`docs/12` §9.2](../../docs/12-exemplo-dossie-preenchido.md#92-notificação-lgpd-art-48).

**▶ Agora no G1:** para o incidente do runbook:

1. **Há risco relevante?** Com a conta de ADMIN, o invasor vê o quê? (Lembre: dado de saúde é
   sensível.) Justifique.
2. **Prazo e quem envia.**
3. **Os 6 itens** do art. 48, §1º.
4. **Quantos titulares?** Sem trilha de auditoria, vocês sabem **o que** o invasor viu? Então qual
   número comunicar?
5. **Recomendação aos titulares** (ex.: trocar a senha se a usavam em outros serviços — por quê?).

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Nenhuma linha aparece no Terminal 1 nem no pedido 5 | o Terminal 1 não está acompanhando (o comando `logs -f` foi interrompido) | clique no Terminal 1: a última linha deve ser o comando `logs ... -f`, sem o prompt `$` depois; se tiver o `$`, rode-o de novo |
| O arquivo `fase7-log-backend.txt` ficou vazio | o VidaPlus foi religado depois dos pedidos (o log começa do zero a cada religada) | repita os pedidos do Passo 1 e grave o log em seguida, **sem** zerar o banco no meio |

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

- [ ] Testei o que o log registra hoje durante um ataque bem-sucedido.
- [ ] Cada funcionalidade tem registro (sem dado pessoal), regra de alerta com números e
      responsável.
- [ ] As três garantias da trilha têm "como é obtida" e "como verificar".
- [ ] O runbook tem ação concreta, quem e evidência nos seis passos.
- [ ] A notificação responde aos 6 itens do art. 48 e assume o pior caso quando não há registro.
