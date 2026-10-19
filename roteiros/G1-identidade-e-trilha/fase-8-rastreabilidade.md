# G1 · Fase 8 — Matriz de rastreabilidade, métricas e preparação da defesa

> **Seção do dossiê:** §10 e §11 · **Lidera:** 🛡️ Segurança (matriz) e 📋 DPO (métricas e risco
> residual) · **Aula:** 10 · **Semanas:** 15–16

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. **§10.1 — Matriz de rastreabilidade:** cada requisito ligado à ameaça, ao controle, ao teste, à
   evidência e ao status.
2. **§10.2 — Métricas** contadas **por ameaça**, e o risco residual.
3. **§11 — Defesa:** quem conduz cada elo do fio, e o ensaio ao vivo.

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
   git checkout -b dossie/fase-8
   git push -u origin dossie/fase-8
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-8
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

**O "fio completo"** é a ideia central da disciplina:

```
Ativo → Ameaça (STRIDE/LINDDUN) → Requisito → Controle → Teste → Evidência → Status
```

A matriz é esse fio, numa tabela. Se um elo falta, o fio quebra — e a defesa vai perguntar
exatamente pelo elo que falta.

**Status possíveis:** **Verificado** (implementado + teste passando + evidência); **Planejado**
(no plano de remediação); **Transferida** (é de outro pacote).

---

## Passo 1 — Monte a matriz

> 🔎 **Como o Grupo 5 fez:** uma linha por requisito do pacote e, no fim, uma linha por item
> **transferido** a outro pacote. Em cada coluna, copiou o que já estava escrito em outra seção do
> dossiê — a matriz **não inventa nada novo**, ela **liga** o que já existe. Veja:
> [`docs/12` §10.1](../../docs/12-exemplo-dossie-preenchido.md#101-matriz-de-rastreabilidade-o-fio-completo).

**▶ Agora no G1:** de onde vem cada coluna:

| Coluna | Copie de |
|---|---|
| ID, Requisito | §2, Forma 1 |
| Atributo | §1 (atributo do ativo) |
| Origem (LGPD/ASVS) | §2, coluna "Base legal / ASVS" |
| Ameaça (STRIDE/LINDDUN) | as **letras** e os **elementos** das linhas que citam o requisito em §3 e §4.1 (ex.: "STRIDE S/E (1.0)") |
| Controle | §6.1 (implementado) ou §6.2 (planejado) |
| Teste | o `.feature` (com `@security` ou `@analise`) |
| Evidência | link da execução ou arquivo em `evidencias/` — só para os verificados |
| Status | Verificado / Planejado / Transferida |

Para o G1, a matriz deve ter: o P0 (`REQ-AUTH-SQLI-01`, **Verificado**); os requisitos de F-01
(credencial), F-11, F-12, F-17 e F-05 (**Planejados**); e os transferidos que apareceram no STRIDE
(ex.: desativar conta sem ser ADMIN → F-03; TLS → V-13).

**Teste de consistência:** escolha 3 ameaças da §3 ao acaso. Cada uma aparece na matriz? Escolha
3 linhas da matriz. Cada uma tem a ameaça na §3/§4?

## Passo 2 — Conte as métricas por ameaça

> 🔎 **Como o Grupo 5 fez:** a primeira versão dizia "71% das ameaças em linhas com requisito
> verificado". Estava **inflado**: contava por linha, e uma linha podia ter uma ameaça resolvida e
> outra não. Recontou **ameaça por ameaça**, em quatro situações — **verificada**, **parcial**,
> **planejada**, **transferida** — e o número real foi **28%**. Explicou que número baixo é o
> esperado numa prova de conceito. Leia a 💡 em [`docs/12` §10.2](../../docs/12-exemplo-dossie-preenchido.md#102-métricas-do-processo).

**▶ Agora no G1:**

1. Na tabela STRIDE (§3), **cada letra marcada em cada elemento é uma ameaça**. Conte o total.
2. Classifique **cada uma** numa das quatro situações:
   - **Verificada:** todos os caminhos dela foram corrigidos e testados (ex.: o S do processo
     Autenticar **pela injeção**);
   - **Parcial:** um caminho foi verificado, outro não (ex.: o S de uma entidade — a injeção foi
     fechada, mas a força bruta e a senha roubada não);
   - **Planejada:** do G1, no plano;
   - **Transferida:** de outro pacote.
3. Faça o mesmo com a LINDDUN (§4.1).
4. Calcule as métricas (copie a tabela de métricas do exemplo e troque os números):
   % de elementos do DFD analisados; % de ameaças com resposta; % verificadas por inteiro;
   % verificadas ao menos em parte; % de requisitos com teste; % de requisitos verificados.

## Passo 3 — Escreva o risco residual

> 🔎 **Como o Grupo 5 fez:** uma frase por tema, dizendo se o risco que sobrou é aceitável — e
> dizendo **"não"** quando não era: *"num projeto real, esta versão não iria para produção sem
> ele"*.

**▶ Agora no G1:** para a injeção no login, a credencial em MD5, a força bruta, o token eterno, a
recuperação de senha e a falta de trilha de auditoria — o risco que sobrou é aceitável? Por quê?
O que precisaria acontecer para ser?

## Passo 4 — Prepare a defesa

A defesa dura 10–15 minutos, a banca é o professor, e **qualquer integrante** pode ser perguntado
sobre **qualquer parte** ([`docs/07`](../../docs/07-rubrica-avaliacao.md#defesa--como-pontuar-banca-o-professor)).

1. **Escolham o fio do P0** e escrevam-no na §11:

   ```
   Ativo: credencial (usuario.email / senha_hash)
     → Ameaça: STRIDE S/E no 1.0 Autenticar (injeção no e-mail)
     → Requisito: REQ-AUTH-SQLI-01
     → Controle: consulta parametrizada + senha conferida no Java + 401 (PR #...)
     → Teste: sqli.feature (@security)
     → Evidência: execução nº ... (🔴) e nº ... (🟢); fase4-sqli-antes/depois.txt
   ```

2. **Dividam os elos:** cada integrante conduz pelo menos um, e estuda os dos colegas.
3. **Ensaiem a demonstração ao vivo** (no laboratório, com o VidaPlus ligado):
   Antes, no terminal (na raiz do repositório), religue o VidaPlus com o código desta branch (que
   já tem a correção) e o banco original:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   ```

   Depois, um item de cada vez:

   - **o ataque falha** (esperado: `HTTP/1.1 401`, sem `access_token`):

     ```bash
     curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
       -d "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}" | head -1
     ```

   - **o login legítimo continua funcionando** (esperado: `200`):

     ```bash
     curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/api/auth/login \
       -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}'
     ```

   - **o teste do P0 verde** (esperado, na última linha: `RESULTADO: todos os testes ... PASSARAM (verde)`):

     ```bash
     bash roteiros/comum/rodar-testes.sh "security & F-01"
     ```

   - **os segredos fora do código** (Fase 6) — um token de ADMIN fabricado com a chave antiga,
     tirada do histórico do Git, é recusado (esperado: a mensagem sobre a chave antiga e um código
     que **não** é `200`):

     ```bash
     FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
     curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO"
     ```

   - **a evidência no pipeline:** abra no navegador as duas URLs de execução que vocês guardaram
     na Fase 5 (antes e depois) e mostre o cenário no log;
   - **a linha do requisito** na matriz da §10 do `entregaveis/DOSSIE.md` (abra a pré-visualização
     com **Ctrl+Shift+V**).
4. **Preparem respostas** para as perguntas do checklist da §11 do dossiê. Algumas que o G1
   certamente vai ouvir:
   - por que filtrar aspas não é uma correção?
   - o login agora é seguro? (Resposta honesta: contra **injeção**, sim; o que mais falta?)
   - por que a trilha de auditoria também é um risco à privacidade?
   - se a chave do token vazar hoje, o que vocês fazem, e o que acontece com quem está logado?

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| O ataque deu `200` na demonstração | a branch da defesa não tem a correção, ou o VidaPlus não foi religado | `git checkout main`, `git pull` e `bash roteiros/comum/zerar-banco.sh` |
| `zerar-banco.sh` diz `Falta o arquivo legacy-app/.env` | este computador ainda não tem o seu `.env` (Fase 6) | crie-o com o comando do [`comum/08` Passo 4b](../comum/08-segredos-fora-do-codigo.md#passo-4--crie-o-modelo-envexample-e-o-seu-env) |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-8`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 8 - rastreabilidade"
git push -u origin dossie/fase-8
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
   **compare** escolha `dossie/fase-8` → **Create pull request**.
2. Título: "Dossiê — Fase 8: rastreabilidade". Na descrição: o que foi feito e quem participou. Clique em
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

## Confira antes de entregar

- [ ] Toda linha da matriz tem todas as colunas preenchidas (ou "—" justificado).
- [ ] A matriz e as tabelas STRIDE/LINDDUN batem nos dois sentidos.
- [ ] As métricas foram contadas por ameaça, com as quatro situações.
- [ ] O risco residual diz "não" onde não é aceitável.
- [ ] Cada integrante sabe conduzir o fio do P0 inteiro, sem consultar.
