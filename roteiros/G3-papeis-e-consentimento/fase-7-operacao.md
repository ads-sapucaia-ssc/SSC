# G3 · Fase 7 — Operação e resposta a incidente

> **Seção do dossiê:** §9 · **Lidera:** 🛡️ Segurança (runbook) e 📋 DPO (notificação) ·
> **Aula:** 9 · **Semana:** 14

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. **Operação:** para cada funcionalidade do pacote, o que é registrado, como vira alerta e quem é
   avisado.
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
- **Nunca** gravar senha, token ou dado de saúde no registro; pessoas pelo **número interno**.
- **Runbook:** detecção → contenção → erradicação → recuperação → notificação → lições. **Conter
  antes de erradicar**; **nunca apagar o log**.
- **LGPD art. 48:** incidente com risco relevante → ANPD e titulares em até **3 dias úteis**.
- **Para o G3:** o **consentimento** também precisa de registro — é a **prova** de que a clínica
  pode tratar o dado para aquela finalidade (art. 8º, §2º).

---

## Passo 1 — Descubra o que o sistema registra hoje

> 🔎 **Como o Grupo 5 fez:** acompanhou o log **durante um ataque que deu certo**: **nenhuma
> linha**. Concluiu: *"o log mostra quem tentou e errou, não quem conseguiu"*. Leia a 💡 no
> [`docs/12` §9](../../docs/12-exemplo-dossie-preenchido.md#9-fase-7--operação--resposta-a-incidente).

**▶ Agora no G3:** 

Você vai usar **dois terminais** ao mesmo tempo. No painel do terminal do VS Code, o **+** (à
direita) abre um terminal novo; a lista à direita do painel mostra todos, e você troca de um para
outro clicando no nome. Os dois abrem na raiz do repositório.

**Terminal 1 — o log da API (backend).** Rode e deixe parado (o `-f` mostra cada linha nova do
log assim que ela aparece; o `--tail 0` esconde as antigas):

```bash
docker compose -f legacy-app/docker-compose.yml logs backend -f --tail 0
```

**Terminal 2 — os pedidos.** Abra outro terminal (**+**) e prepare os tokens:

```bash
source roteiros/comum/tokens.sh
```

Rode **um bloco de cada vez** e, depois de cada um, olhe o Terminal 1.

1. O **admin** desativa a conta da recepção (usuário 3) — uma ação **permitida**:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ADMIN"; echo
   ```

   Esperado: `{"id":3,"alterado":true}`. Nos próximos dois blocos, o `source` vai mostrar
   `TOKEN_REC FALHOU`: é o efeito deste item (a conta da recepção está desativada).

2. A **Ana** (paciente) tenta reativar a mesma conta — uma ação que, depois da correção da Fase 4,
   deve ser **negada** (403):

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ANA"
   ```

   Esperado: `403`.

3. O **admin** reativa a conta (o mesmo pedido do item 1 inverte o estado):

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ADMIN"; echo
   ```

   Esperado: de novo `{"id":3,"alterado":true}`.

Esperado no Terminal 1: **nenhuma** linha nova, em nenhum dos três — nem na ação permitida, nem
na negada. O que apareceu no Terminal 1 depois de cada um? Se a recepção chegasse amanhã e não conseguisse
entrar, alguém saberia **quem** desativou a conta dela, e **quando**? A tentativa **negada** da Ana
deixou algum rastro?

Ao terminar, clique no Terminal 1 e aperte **Ctrl+C**. Para guardar o log como evidência e voltar
o banco ao estado original, no Terminal 2:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend --tail 200 > entregaveis/evidencias/fase7-log-api.txt
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
```

## Passo 2 — Descreva a operação de cada funcionalidade

> 🔎 **Como o Grupo 5 fez:** para cada funcionalidade, **o que é registrado** (e o que **nunca**
> é), **como vira alerta** (regras com números) e **quem é avisado**, mais a **situação**.

**▶ Agora no G3:** para **controle por papel, consentimento, cadastro (F-15) e perfil (F-19)**.
Perguntas:

- **RBAC:** registrar as negações (403)? Que padrão indica alguém tentando funções de outro papel
  (muitos 403 seguidos)? Quais ações **permitidas** são sensíveis o bastante para sempre registrar
  (desativar conta, publicar laudo, mudar papel)?
- **Consentimento:** cada concessão e cada revogação (com a versão do termo) — é a prova. O que
  monitorar? (Ex.: o sistema tratou um dado para a finalidade opcional **depois** de uma
  revogação.)
- **Cadastro e perfil:** registrar cada alteração de cadastro — **o que** mudou, sem gravar o
  valor novo de um dado sensível?
- **Quem é avisado** (o DPO, nos casos de consentimento), por qual canal; a **situação**.

## Passo 3 — A interface com a trilha de auditoria (F-05, do G1)

> 🔎 **Como o Grupo 5 fez:** escreveu **o que precisava da trilha** de outro pacote: o evento
> `PACIENTE_ANONIMIZADO`, com o paciente identificado só pelo número interno — e **por quê**.

**▶ Agora no G3:** que eventos do seu pacote a trilha precisa guardar (ex.: `USUARIO_DESATIVADO`,
`ACESSO_NEGADO`, `CONSENTIMENTO_CONCEDIDO`, `CONSENTIMENTO_REVOGADO`, `CADASTRO_ALTERADO`)? Com
que campos? Para que servem depois de um incidente? O registro de consentimento fica na **trilha**
ou na **tabela de consentimento** (ou nos dois)? Por quê?

## Passo 4 — Escreva o runbook do incidente mais grave

> 🔎 **Como o Grupo 5 fez:** *"vazamento de cadastros pela busca"*, seis passos com ação, quem e
> evidência; honesto na detecção; *"copiar os logs antes de qualquer reinício"*. Veja:
> [`docs/12` §9.1](../../docs/12-exemplo-dossie-preenchido.md#91-runbook-de-incidente--vazamento-de-cadastros-pela-busca-incidente-do-nosso-pacote).

**▶ Agora no G3:** candidatos — escolham o mais grave para o **seu** pacote:

- **"um paciente desativou as contas da equipe"** (a clínica para de funcionar);
- **"um token de ADMIN fabricado"** (Fase 5);
- **"dados usados para a finalidade opcional sem consentimento"** (incidente de privacidade, sem
  ataque nenhum).

| Passo | Perguntas para o G3 |
|---|---|
| 1 Detecção | por qual registro vocês perceberiam? Hoje existe? Como preservar? |
| 2 Contenção | reativar as contas? Trocar a chave do token — como vocês fizeram na Fase 6, com a `JWT_SECRET` do `legacy-app/.env` — derruba **todos** os tokens, inclusive os fabricados: e os legítimos? Suspender a finalidade opcional? |
| 3 Erradicação | qual PR fecha a causa? |
| 4 Recuperação | a matriz de acesso (script) igual à de permissões; testes verdes |
| 5 Notificação | Passo 5 |
| 6 Lições | que requisito nasce? |

## Passo 5 — Notificação à ANPD e aos titulares (art. 48)

> 🔎 **Como o Grupo 5 fez:** risco relevante (sim), prazo (3 dias úteis), os 6 itens do art. 48
> §1º, **pior caso** quando não havia registro, e o **registro interno** obrigatório. Veja:
> [`docs/12` §9.2](../../docs/12-exemplo-dossie-preenchido.md#92-notificação-lgpd-art-48).

**▶ Agora no G3:** para o incidente do runbook:

1. **Há risco relevante?** Num incidente de **disponibilidade** (contas desativadas), houve dado
   pessoal exposto? Se não houve, precisa notificar? (Leiam o art. 48 e a definição de incidente:
   ele inclui perda de **disponibilidade** do dado.) Justifiquem — e registrem internamente de
   qualquer forma.
2. **Prazo, quem envia, os 6 itens.**
3. **Quantos titulares?** Sem registro de quem fez o quê, como saber?

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| `TOKEN_REC FALHOU` nos itens 2 e 3 | a conta da recepção foi desativada no item 1 (é o experimento) | siga em frente; o item 3 a reativa e o bloco final zera o banco |
| No item 2, a Ana recebeu `200` | a correção da Fase 4 não está nesta branch | `git log --oneline -3` deve mostrar o merge do `fix/F-03-rbac`; depois, `zerar-banco.sh` |

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

- [ ] Testei o que o log registra quando alguém usa uma função de outro papel.
- [ ] Cada funcionalidade tem registro, alerta com números e responsável — inclusive o
      consentimento.
- [ ] Escrevi a interface com a F-05.
- [ ] O runbook tem ação concreta, quem e evidência nos seis passos.
- [ ] A notificação decide, com justificativa, se o incidente precisa ser comunicado.
