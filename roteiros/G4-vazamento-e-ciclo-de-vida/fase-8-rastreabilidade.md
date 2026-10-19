# G4 · Fase 8 — Matriz de rastreabilidade, métricas e preparação da defesa

> **Seção do dossiê:** §10 e §11 · **Lidera:** 🛡️ Segurança (matriz) e 📋 DPO (métricas e risco
> residual) · **Aula:** 10 · **Semanas:** 15–16

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

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
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório.
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

**Status:** **Verificado** (implementado + teste passando + evidência), **Planejado**,
**Transferida**.

---

## Passo 1 — Monte a matriz

> 🔎 **Como o Grupo 5 fez:** uma linha por requisito e, no fim, uma por item **transferido**. Cada
> coluna foi **copiada** de outra seção: a matriz **liga**, não inventa. Veja:
> [`docs/12` §10.1](../../docs/12-exemplo-dossie-preenchido.md#101-matriz-de-rastreabilidade-o-fio-completo).

**▶ Agora no G4:**

| Coluna | Copie de |
|---|---|
| ID, Requisito | §2, Forma 1 |
| Atributo | §1 |
| Origem (LGPD/ASVS) | §2 |
| Ameaça (STRIDE/LINDDUN) | letras e elementos que citam o requisito em §3 e §4.1 |
| Controle | §6.1 ou §6.2 |
| Teste | o `.feature` (`@security` ou `@analise`) |
| Evidência | link da execução; a tabela de canais antes/depois |
| Status | Verificado / Planejado / Transferida |

Para o G4: `REQ-AC-IDOR-01` (**Verificado**); o requisito de erro da F-16 (**Verificado** na parte
que entrou no PR; **Planejado** no resto — diga isso na célula); os requisitos do canal arquivo,
F-18, F-20 e F-06 (**Planejados**); e os transferidos: o armazenamento dos PDFs (F-08, G2), a
criação de atendimentos por qualquer papel (F-03, G3), a nota clínica sem login (V-19), a trilha
(F-05, G1).

**Teste de consistência:** 3 ameaças da §3/§4 ao acaso estão na matriz? 3 linhas da matriz têm a
ameaça na §3/§4?

## Passo 2 — Conte as métricas por ameaça

> 🔎 **Como o Grupo 5 fez:** a primeira versão ("71% em linhas com requisito verificado") estava
> **inflada**, porque contava por linha. Recontou **ameaça por ameaça** em quatro situações —
> **verificada, parcial, planejada, transferida** — e chegou a **28%**, explicando que número baixo
> é o esperado numa prova de conceito. Leia a 💡 em [`docs/12` §10.2](../../docs/12-exemplo-dossie-preenchido.md#102-métricas-do-processo).

**▶ Agora no G4:**

1. Cada letra marcada em cada elemento da §3 é uma ameaça. Conte.
2. Classifique cada uma:
   - **Verificada:** todos os caminhos fechados e testados;
   - **Parcial:** um canal fechado, outro não. **No G4, este é o caso mais comum** — o **I** do
     depósito "laudos": API fechada, arquivo aberto;
   - **Planejada**; **Transferida**.
3. Faça o mesmo com a LINDDUN (§4.1).
4. Calcule: % de elementos analisados; % de ameaças com resposta; % verificadas por inteiro;
   % verificadas ao menos em parte; % de requisitos com teste; % de requisitos verificados; e, do
   G4, **% de canais até o laudo fechados** (canais fechados / canais identificados).

## Passo 3 — Escreva o risco residual

> 🔎 **Como o Grupo 5 fez:** uma frase por tema, dizendo **"não"** quando não era aceitável:
> *"num projeto real, esta versão não iria para produção sem ele"*.

**▶ Agora no G4:** canal por canal, e para o ciclo de vida: o risco que sobrou é aceitável? A
pergunta-chave da defesa: **o laudo está protegido**, se a API está fechada e o arquivo não?

## Passo 4 — Prepare a defesa

A defesa dura 10–15 minutos; **qualquer integrante** pode ser perguntado sobre **qualquer parte**
([`docs/07`](../../docs/07-rubrica-avaliacao.md#defesa--como-pontuar-banca-o-professor)).

1. **O fio do P0**, na §11:

   ```
   Ativo: laudo / resultado de exame — dado de saúde, art. 11
     → Ameaça: STRIDE I/E no processo "Obter exame"; I no fluxo "mensagem de erro";
               LINDDUN D (oráculo de existência)
     → Requisito: REQ-AC-IDOR-01 (+ REQ-ERR-...)
     → Controle: regra de dono, deny-by-default, resposta uniforme, erro sem detalhes, 401 (PR #...)
     → Teste: idor.feature (@security) + tabela de canais
     → Evidência: execução nº ... (🔴) e nº ... (🟢); fase4-canais-antes/depois.txt
   ```

2. **Dividam os elos** — inclusive a **tabela de retenção** e a resposta ao pedido de eliminação,
   que o DPO conduz.
3. **Ensaiem a demonstração ao vivo:**
   Antes, no terminal (na raiz do repositório), religue o VidaPlus com o código desta branch (que
   já tem a correção) e o banco original:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   ```

   Depois, um item de cada vez:

   - **a tabela de canais** (403 igual para "não existe" e "não é seu", nada vazado, 401 sem login):

     ```bash
     bash roteiros/G4-vazamento-e-ciclo-de-vida/canais-do-laudo.sh
     ```

   - **pela tela:** entre como Ana em `http://localhost:4200`, abra o exame 101 e troque, na barra de
     endereços, `/exames/101` por `/exames/102` → "Exame não encontrado ou acesso negado.";
   - **o teste do P0 verde:**

     ```bash
     bash roteiros/comum/rodar-testes.sh "security & F-02"
     ```

   - e, honestamente, **o canal que continua aberto** — a última parte da saída da tabela de canais
     mostra o `laudo-104.pdf` ainda baixando sem login — e o plano para ele.
4. **Perguntas que o G4 certamente vai ouvir:**
   - por que "não existe" e "não é seu" dão a mesma resposta?
   - por que uma mensagem de erro é um canal de vazamento?
   - o laudo está protegido? (Resposta honesta: pela API, sim; pelo arquivo, não.)
   - o paciente pede para apagar tudo. O que a clínica apaga, o que guarda, por quê e até quando?
   - qual a diferença entre eliminar e anonimizar? Quando usar cada um?
   - o que a notificação pode dizer, e o que nunca pode?

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
- [ ] Métricas contadas por ameaça, com os casos parciais por canal.
- [ ] O risco residual diz "não" onde não é aceitável.
- [ ] Cada integrante conduz o fio do P0 **e** responde ao pedido de eliminação, sem consultar.
