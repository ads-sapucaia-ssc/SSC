# G3 · Fase 8 — Matriz de rastreabilidade, métricas e preparação da defesa

> **Seção do dossiê:** §10 e §11 · **Lidera:** 🛡️ Segurança (matriz) e 📋 DPO (métricas e risco
> residual) · **Aula:** 10 · **Semanas:** 15–16

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

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

   Deve aparecer `README.md  docs  entregaveis  legacy-app  roteiros  security`. Se não
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
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

```
Ativo → Ameaça (STRIDE/LINDDUN) → Requisito → Controle → Teste → Evidência → Status
```

**Status:** **Verificado** (implementado + teste passando + evidência), **Planejado**,
**Transferida**.

> **Não confunda as duas matrizes do G3:** a **matriz de permissões** (Fase 3) diz quem pode o quê;
> a **matriz de rastreabilidade** (esta fase) liga cada requisito à sua evidência. A primeira é
> **uma das evidências** da segunda.

---

## Passo 1 — Monte a matriz de rastreabilidade

> 🔎 **Como o Grupo 5 fez:** uma linha por requisito e, no fim, uma por item **transferido**. Cada
> coluna foi **copiada** de outra seção: a matriz **liga**, não inventa. Veja:
> [`docs/12` §10.1](../../docs/12-exemplo-dossie-preenchido.md#101-matriz-de-rastreabilidade-o-fio-completo).

**▶ Agora no G3:**

| Coluna | Copie de |
|---|---|
| ID, Requisito | §2, Forma 1 |
| Atributo | §1 |
| Origem (LGPD/ASVS) | §2 |
| Ameaça (STRIDE/LINDDUN) | letras e elementos que citam o requisito em §3 e §4.1 |
| Controle | §6.1 ou §6.2 |
| Teste | o `.feature` (`@security` ou `@analise`) |
| Evidência | link da execução; a matriz de acesso antes/depois |
| Status | Verificado / Planejado / Transferida |

Para o G3: `REQ-AC-RBAC-01` e os demais requisitos de RBAC corrigidos no PR (**Verificados**);
F-04, F-15 e F-19 (**Planejados** — a F-04 com a especificação da Fase 4); o REQ-SEGREDO-01 da
chave do token (F-10, **Verificado** na Fase 6); e os transferidos: a vida do token (F-11, G1), a trilha (F-05, G1), a regra de dono da
carteira do médico (G2), as funções sem login (F-13, V-19).

**Teste de consistência:** 3 ameaças da §3/§4 ao acaso estão na matriz? 3 linhas da matriz têm a
ameaça na §3/§4?

## Passo 2 — Conte as métricas por ameaça

> 🔎 **Como o Grupo 5 fez:** a primeira versão ("71% em linhas com requisito verificado") estava
> **inflada**, porque contava por linha. Recontou **ameaça por ameaça** em quatro situações —
> **verificada, parcial, planejada, transferida** — e chegou a **28%**, explicando que número baixo
> é o esperado numa prova de conceito. Leia a 💡 em [`docs/12` §10.2](../../docs/12-exemplo-dossie-preenchido.md#102-métricas-do-processo).

**▶ Agora no G3:**

1. Cada letra marcada em cada elemento da §3 é uma ameaça. Conte.
2. Classifique cada uma:
   - **Verificada:** todos os caminhos fechados e testados;
   - **Parcial:** um caminho fechado, outro não. Exemplo típico do G3: o **E** de "paciente faz
     ação de admin" — chamar a função direto foi fechado; fabricar o token, não;
   - **Planejada**; **Transferida**.
3. Faça o mesmo com a LINDDUN (§4.1). Atenção: no G3, a LINDDUN tem **muitas** ameaças (F-04,
   F-15, F-19) e quase todas ficam **planejadas** — e isso precisa aparecer na métrica.
4. Calcule: % de elementos analisados; % de ameaças com resposta; % verificadas por inteiro;
   % verificadas ao menos em parte; % de requisitos com teste; % de requisitos verificados; e, do
   G3, **% de células da matriz de permissões cobertas por teste**.

## Passo 3 — Escreva o risco residual

> 🔎 **Como o Grupo 5 fez:** uma frase por tema, dizendo **"não"** quando não era aceitável:
> *"num projeto real, esta versão não iria para produção sem ele"*.

**▶ Agora no G3:** para o RBAC, o token fabricável, o token que não acompanha a mudança de papel,
o cadastro com dados sem finalidade, o paciente sem consentimento nem autosserviço — o risco que
sobrou é aceitável? Atenção ao token: o RBAC está correto, mas o **sistema** está protegido?

## Passo 4 — Prepare a defesa

A defesa dura 10–15 minutos; **qualquer integrante** pode ser perguntado sobre **qualquer parte**
([`docs/07`](../../docs/07-rubrica-avaliacao.md#defesa--como-pontuar-banca-o-professor)).

1. **O fio do P0**, na §11:

   ```
   Ativo: função "publicar laudo" (POST /api/exames/{id}/publicar)
     → Ameaça: STRIDE E (e T) no processo "Publicar laudo"
     → Requisito: REQ-AC-RBAC-01
     → Controle: Papeis.exigir(auth, "BIOQUIMICO") — regra central, deny-by-default (PR #...)
     → Teste: rbac.feature (@security) + matriz de acesso (script)
     → Evidência: execução nº ... (🔴) e nº ... (🟢); fase4-matriz-antes/depois.txt
   ```

2. **Dividam os elos**; cada um estuda os dos colegas — inclusive a **base legal** e o
   **consentimento**, que o DPO conduz.
3. **Ensaiem a demonstração ao vivo:**
   Antes, no terminal (na raiz do repositório), religue o VidaPlus com o código desta branch (que
   já tem a correção) e o banco original:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   ```

   Depois, um item de cada vez:

   - **a matriz de acesso** (200 só onde a matriz de permissões tem ✓; 403 no resto; 401 sem
     login):

     ```bash
     bash roteiros/G3-papeis-e-consentimento/matriz-de-acesso.sh
     ```

   - **pela tela:** entre como Ana em `http://localhost:4200` e digite, na barra de endereços,
     `http://localhost:4200/usuarios` → a tela abre, mas fica em "Carregando…" (a API negou);
   - **o teste do P0 verde** (zere antes, porque a matriz mudou dados):

     ```bash
     bash roteiros/comum/zerar-banco.sh
     bash roteiros/comum/rodar-testes.sh "security & F-03"
     ```

   - **o elo que vocês fecharam na Fase 6** — o token fabricado. A chave não está mais no código;
     o script acha a **antiga** no histórico do Git, e o servidor recusa:

     ```bash
     FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
     curl -s -o /dev/null -w 'token fabricado -> lista de usuarios: %{http_code}\n' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO"
     ```

     Esperado: a mensagem sobre a chave antiga e um código que **não** é 200. E, honestamente,
     **o que continua aberto**: o token antigo depois de mudar o papel (F-11) — e o que está no
     plano para ele.
4. **Perguntas que o G3 certamente vai ouvir:**
   - por que esconder o menu não é controle de acesso?
   - qual a diferença entre 401 e 403? Quando o `Papeis.exigir` devolve cada um?
   - o que é *deny-by-default*, e onde ele aparece no código de vocês?
   - por que o atendimento médico não depende de consentimento? Para que finalidade a F-04 pede?
   - o que acontece se a chave do token vazar?
   - o que é atribuição em massa, e onde ela aparece no VidaPlus?

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

- [ ] Toda linha da matriz de rastreabilidade tem todas as colunas (ou "—" justificado).
- [ ] Matriz e tabelas STRIDE/LINDDUN batem nos dois sentidos.
- [ ] Métricas contadas por ameaça, inclusive as LINDDUN planejadas.
- [ ] O risco residual diz "não" onde não é aceitável.
- [ ] Cada integrante conduz o fio do P0 **e** explica a base legal do consentimento, sem consultar.
