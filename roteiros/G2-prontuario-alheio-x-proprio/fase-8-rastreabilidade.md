# G2 · Fase 8 — Matriz de rastreabilidade, métricas e preparação da defesa

> **Seção do dossiê:** §10 e §11 · **Lidera:** 🛡️ Segurança (matriz) e 📋 DPO (métricas e risco
> residual) · **Aula:** 10 · **Semanas:** 15–16

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. **§10.1 — Matriz de rastreabilidade.**
2. **§10.2 — Métricas** contadas **por ameaça**, e o risco residual.
3. **§11 — Defesa:** quem conduz cada elo, e o ensaio ao vivo.

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

```
Ativo → Ameaça (STRIDE/LINDDUN) → Requisito → Controle → Teste → Evidência → Status
```

A matriz é esse fio numa tabela. **Status:** **Verificado** (implementado + teste passando +
evidência), **Planejado** (no plano), **Transferida** (outro pacote).

---

## Passo 1 — Monte a matriz

> 🔎 **Como o Grupo 5 fez:** uma linha por requisito e, no fim, uma por item **transferido**. Cada
> coluna foi **copiada** de outra seção: a matriz não inventa, ela **liga**. Veja:
> [`docs/12` §10.1](../../docs/12-exemplo-dossie-preenchido.md#101-matriz-de-rastreabilidade-o-fio-completo).

**▶ Agora no G2:**

| Coluna | Copie de |
|---|---|
| ID, Requisito | §2, Forma 1 |
| Atributo | §1 |
| Origem (LGPD/ASVS) | §2 |
| Ameaça (STRIDE/LINDDUN) | letras e elementos das linhas que citam o requisito em §3 e §4.1 |
| Controle | §6.1 ou §6.2 |
| Teste | o `.feature` (`@security` ou `@analise`) |
| Evidência | link da execução ou arquivo em `evidencias/` |
| Status | Verificado / Planejado / Transferida |

Para o G2: `REQ-AC-IDOR-01` (**Verificado**); os requisitos de F-02 para médico e outros papéis,
F-07, F-08 e F-09 (**Planejados** — ou Verificados, se entraram no PR); e os transferidos: a
criação de atendimento por qualquer papel (F-03, G3 — a interface da Fase 5), a trilha (F-05, G1),
o tratamento de erros (F-16, G4).

**Teste de consistência:** 3 ameaças da §3 ao acaso estão na matriz? 3 linhas da matriz têm a
ameaça na §3/§4?

## Passo 2 — Conte as métricas por ameaça

> 🔎 **Como o Grupo 5 fez:** a primeira versão ("71% em linhas com requisito verificado") estava
> **inflada**, porque contava por linha. Recontou **ameaça por ameaça** em quatro situações —
> **verificada, parcial, planejada, transferida** — e chegou a **28%**, explicando que número baixo
> é o esperado numa prova de conceito. Leia a 💡 em [`docs/12` §10.2](../../docs/12-exemplo-dossie-preenchido.md#102-métricas-do-processo).

**▶ Agora no G2:**

1. Cada letra marcada em cada elemento da §3 é uma ameaça. Conte.
2. Classifique cada uma:
   - **Verificada:** todos os caminhos fechados e testados;
   - **Parcial:** um caminho fechado, outro não. Exemplo típico do G2: o **I** do depósito
     `exame` — a API foi fechada, mas o arquivo na pasta pública não;
   - **Planejada**; **Transferida**.
3. Faça o mesmo com a LINDDUN (§4.1).
4. Calcule: % de elementos analisados; % de ameaças com resposta; % verificadas por inteiro;
   % verificadas ao menos em parte; % de requisitos com teste; % de requisitos verificados.

## Passo 3 — Escreva o risco residual

> 🔎 **Como o Grupo 5 fez:** uma frase por tema, dizendo **"não"** quando não era aceitável:
> *"num projeto real, esta versão não iria para produção sem ele"*.

**▶ Agora no G2:** para o IDOR na API, o PDF na pasta pública, a publicação aberta, os números
sequenciais e a falta de exportação — o risco que sobrou é aceitável? Atenção ao PDF: se a API
está protegida mas o arquivo não, o **laudo** está protegido?

## Passo 4 — Prepare a defesa

A defesa dura 10–15 minutos; **qualquer integrante** pode ser perguntado sobre **qualquer parte**
([`docs/07`](../../docs/07-rubrica-avaliacao.md#defesa--como-pontuar-banca-o-professor)).

1. **O fio do P0**, na §11:

   ```
   Ativo: resultado de exame (exame.resultado) — dado de saúde, art. 11
     → Ameaça: STRIDE I/E no processo "Obter exame" (IDOR)
     → Requisito: REQ-AC-IDOR-01
     → Controle: regra de dono no servidor, deny-by-default, nos 3 caminhos da API (PR #...)
     → Teste: idor.feature (@security) + tabela de acessos por papel
     → Evidência: execução nº ... (🔴) e nº ... (🟢); fase4-acessos-antes/depois.txt
   ```

2. **Dividam os elos**; cada um estuda os dos colegas.
3. **Ensaiem a demonstração ao vivo:**
   Antes, no terminal (na raiz do repositório), religue o VidaPlus com o código desta branch (que
   já tem a correção) e o banco original:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   ```

   Depois, um item de cada vez:

   - **pela tela:** entre como Ana em `http://localhost:4200`, abra o exame 101 e troque, na barra de
     endereços, `/exames/101` por `/exames/102` → "Exame não encontrado ou acesso negado.";
   - **a mesma coisa pelo terminal** — o primeiro comando mostra o código; o segundo conta as
     linhas da resposta que contêm `Glicemia` ou `paciente_id` (esperado: `403` e `0`):

     ```bash
     source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
     curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA"
     curl -s localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA" | grep -c -E "Glicemia|paciente_id"
     ```

   - **a tabela de acessos por papel** (o uso legítimo dá 200; o resto, 403):

     ```bash
     bash roteiros/G2-prontuario-alheio-x-proprio/acessos-por-papel.sh
     ```

   - **o teste do P0 verde** (esperado, na última linha: `RESULTADO: todos os testes ... PASSARAM (verde)`):

     ```bash
     bash roteiros/comum/rodar-testes.sh "security & F-02"
     ```

   - **os segredos fora do código** (Fase 6) — o token "da Ana" fabricado com a chave antiga, do
     histórico do Git, não abre mais o exame dela (esperado: a mensagem sobre a chave antiga e um
     código que **não** é `200`):

     ```bash
     FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 PACIENTE)
     curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101 -H "Authorization: Bearer $FORJADO"
     ```

   - e, honestamente, **o que continua aberto** — o PDF ainda baixa sem login (esperado:
     `200 application/pdf`):

     ```bash
     curl -s -o /dev/null -w '%{http_code} %{content_type}\n' localhost:4200/laudos/laudo-102.pdf
     ```

     — e o que está no plano para ele.
4. **Perguntas que o G2 certamente vai ouvir:**
   - qual a diferença entre 401 e 403, e por que vocês usaram 403?
   - por que "não existe" e "não é seu" dão a mesma resposta?
   - o laudo está protegido? (Resposta honesta: pela API, sim; o arquivo, não — e por quê.)
   - a regra do médico depende de quê? E se alguém mexer nessa tabela? (O achado da Fase 5.)
   - como a exportação (F-09) evita o mesmo erro do IDOR?

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Na demonstração, a Ana vê o exame 102 (`200`) | a branch da defesa não tem a correção, ou o VidaPlus não foi religado | `git checkout main`, `git pull` e `bash roteiros/comum/zerar-banco.sh` |
| A tabela de acessos mostra o Bio sem acesso ao 104 | os testes rodaram antes e publicaram o laudo 104 | zere o banco antes da tabela: `bash roteiros/comum/zerar-banco.sh` |
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

- [ ] Toda linha da matriz tem todas as colunas (ou "—" justificado).
- [ ] Matriz e tabelas STRIDE/LINDDUN batem nos dois sentidos.
- [ ] Métricas contadas por ameaça, com as quatro situações.
- [ ] O risco residual diz "não" onde não é aceitável.
- [ ] Cada integrante conduz o fio do P0 inteiro, sem consultar.
