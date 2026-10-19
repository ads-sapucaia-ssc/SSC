# G1 · Fase 3 — Priorização, comportamento sob falha e Definition of Done

> **Seção do dossiê:** §5 · **Lidera:** 🛡️ Segurança · **Aula:** 5 · **Semana:** 6

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. A tabela de **priorização** de cada requisito, com método, nota, risco e prioridade (P0/P1/P2).
2. A explicação de **por que cada linha usa aquele método**.
3. O **comportamento sob falha** (*fail-secure*) dos controles do pacote — **testado**.
4. A **Definition of Done** segura do grupo.

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
   git checkout -b dossie/fase-3
   git push -u origin dossie/fase-3
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-3
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

**Por que priorizar:** vocês têm mais problemas do que tempo. A prioridade diz **o que corrigir
primeiro**: **P0** bloqueia a entrega; **P1** é essencial; **P2** é melhoria.

**Os métodos de pontuação** — cada um serve para um tipo de problema:

| Método | Serve para | Como funciona |
|---|---|---|
| **CVSS 3.1** | falha técnica com um caminho de exploração (injeção, acesso indevido) | você responde 8 perguntas na calculadora e ela dá uma nota de 0 a 10 |
| **DREAD** | ameaça sem "vetor de ataque" claro (ex.: privacidade por design) | nota de 0 a 10 para Dano, Reprodutibilidade, Exploração, Afetados, Descoberta; tira a média |
| **Conformidade** | descumprimento de lei, sem ataque (ex.: não ter trilha de auditoria) | a prioridade vem da obrigação legal, sem nota |
| **EPSS / KEV** | vulnerabilidade **com número de CVE** (em biblioteca) | EPSS = chance de ser explorada em 30 dias; KEV = lista de falhas **já exploradas** no mundo (se estiver no KEV, sobe um nível) |

**Fail-secure:** quando algo dá errado (banco fora do ar, token inválido, erro inesperado), o
sistema deve **negar**, e não liberar. E a mensagem de erro não pode revelar detalhes internos.

**Definition of Done (DoD):** a lista de condições para dizer que uma funcionalidade está
"pronta" com segurança.

---

## Passo 1 — Calcule o CVSS das falhas técnicas

> 🔎 **Como o Grupo 5 fez:** para a injeção no login, abriu a calculadora do FIRST
> (`https://www.first.org/cvss/calculator/3.1`) e respondeu cada métrica **com o que tinha visto
> no VidaPlus**:
>
> | Métrica | Escolha | Por quê (o que ele viu) |
> |---|---|---|
> | Attack Vector (AV) | Network | atacou pela rede, com curl |
> | Attack Complexity (AC) | Low | o mesmo payload funciona sempre |
> | Privileges Required (PR) | None | não precisou de conta |
> | User Interaction (UI) | None | ninguém precisou clicar em nada |
> | Scope (S) | Unchanged | o efeito fica no próprio VidaPlus |
> | Confidentiality (C) | High | entrando como ADMIN, lê tudo |
> | Integrity (I) | High | como ADMIN, altera usuários |
> | Availability (A) | High | como ADMIN, pode desativar todas as contas |
>
> Resultado: `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H` = **9.8**. Para a injeção na **busca**,
> o impacto era menor (só lê; o banco recusa um segundo comando) — **8.2**. Lição: **o impacto vem
> do que o acesso permite**, não do nome da falha.

**▶ Agora no G1:**

1. O P0 de vocês é **a mesma falha** do primeiro cálculo acima. Refaça o cálculo **por conta
   própria**, justificando cada métrica com o que **vocês** viram (Fase 2a). Concordam com o 9.8?
2. Calcule também para as falhas técnicas dos outros requisitos que se encaixam em CVSS. Exemplos
   do G1 para avaliar: o token que não expira e continua valendo depois do Sair; o código de
   recuperação previsível; a força bruta sem limite; a chave do token no código. Para cada um,
   pense em especial em:
   - **PR:** precisa de conta? (Para reutilizar um token, o atacante precisa **ter** o token.)
   - **AC:** é fácil sempre, ou depende de condições (estar na mesma rede, ter o código-fonte)?
   - **C/I/A:** o que o atacante consegue **fazer** depois?
3. Escreva na tabela o **vetor completo** (começando por `CVSS:3.1/`) e a nota.

## Passo 2 — Use DREAD ou conformidade onde o CVSS não serve

> 🔎 **Como o Grupo 5 fez:** para a busca que isola uma pessoa (privacidade por design), usou
> DREAD 6/9/8/8/7 → **7,6**. Para a anonimização (obrigação legal, sem ataque), usou
> **conformidade**, sem nota.

**▶ Agora no G1:**

- **F-05 (trilha de auditoria):** não ter registro não é um "ataque" — é descumprir uma obrigação
  (LGPD art. 37: registro das operações). Que método?
- **Enumeração de contas pelo reset** (descobrir quem é paciente): é técnica ou de privacidade?
  Escolha e **justifique**.

## Passo 3 — Decida o risco e a prioridade, e justifique

> 🔎 **Como o Grupo 5 fez:** a coluna **Risco** é decisão do grupo, não cópia da nota. A busca sem
> login tinha CVSS 7.5, mas o grupo marcou risco **Crítico** e **P0**, explicando: *"o CVSS mede a
> falha técnica, não o tipo de dado; aqui o dado é sensível e de todos os titulares."* E promoveu
> a busca de P1 para P0 porque a correção era a mesma técnica do login.

**▶ Agora no G1:** para cada requisito, decida o risco (Crítico/Alto/Médio/Baixo) e a prioridade.
Se mudar a prioridade do catálogo (para cima ou para baixo), **explique**. Escreva o bloco "Por
que cada linha usa um método" — um parágrafo curto por método usado.

Sobre **EPSS/KEV**: as falhas do G1 estão no código do próprio VidaPlus e **não têm CVE**; escreva
"n/a" e explique, como o exemplo fez. Se o SCA do pipeline acusar CVE numa biblioteca de
autenticação (ex.: a biblioteca de JWT), ela entra na tabela com CVSS + EPSS + KEV.

## Passo 4 — Defina e **teste** o comportamento sob falha

> 🔎 **Como o Grupo 5 fez:** escreveu "erro de banco → mensagem genérica" e **foi testar**:
>
> ```bash
> curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q='"
> ```
>
> A resposta trouxe o SQL e o rastro da exceção (*stack trace*): `You have an error in your SQL
> syntax ... near '%'`. A afirmação era **falsa** no legado. O grupo registrou isso e incluiu a
> correção no próprio PR (três linhas de configuração). Leia a 💡 "Fail-secure se testa provocando
> a falha" no [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G1:** para cada controle do pacote, escreva **o que deve acontecer quando algo dá
errado** — e provoque a falha para ver o que acontece **hoje**:

1. **Login com erro de banco:** derrube o banco e tente entrar.

   ```bash
   docker compose -f legacy-app/docker-compose.yml stop db
   curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}' | head -20
   docker compose -f legacy-app/docker-compose.yml start db
   ```

   **A resposta demora uns 30 segundos** (é o tempo que o backend espera pelo banco antes de
   desistir) — não é travamento. Ela revela detalhes internos? Procure `exception` e `trace`.
   (Depois do `start`, espere uns 20 segundos antes de usar o sistema de novo.)
2. **Token inválido:** um token adulterado deve dar **401**. O que dá hoje?

   ```bash
   curl -s -i localhost:8080/api/exames/101 -H "Authorization: Bearer token-falso" | head -5
   ```

   Esperado: cinco linhas de cabeçalho; a primeira, `HTTP/1.1` e o código de status.

3. **Sem token** — o mesmo pedido, sem o cabeçalho `Authorization`:

   ```bash
   curl -s -i localhost:8080/api/exames/101 | head -5
   ```

   Esperado: de novo cinco linhas, a primeira com o código. Qual o código? Deveria ser qual?
4. **Trava contra força bruta (planejada):** se a contagem de tentativas falhar, o sistema deve
   liberar ou bloquear? E por quanto tempo?
5. **Trilha de auditoria (planejada):** se gravar o registro falhar, a ação (ex.: o login) deve
   acontecer mesmo assim? Pense nos dois lados: segurança (sem registro, sem prova) e
   disponibilidade (ninguém entra se o log cair). Escolha e justifique.

Guarde as saídas em `entregaveis/evidencias/fase3-...txt`.

## Passo 5 — Escreva a Definition of Done

> 🔎 **Como o Grupo 5 fez:** 8 critérios, entre eles: as duas formas do requisito; PR revisado
> por outro integrante com o checklist; teste do **ataque** e do **uso legítimo**; **ataque
> repetido à mão** depois da correção; erro genérico **conferido provocando o erro**; nenhum
> segredo no código; linha "Verificado" na matriz. Veja: [`docs/12` §5](../../docs/12-exemplo-dossie-preenchido.md#5-fase-3--especificação-priorização--definition-of-done).

**▶ Agora no G1:** escreva a DoD de vocês. Use o exemplo como base, mas acrescente o que é
**específico do G1**. Pistas:

- o login legítimo continua funcionando (há um teste para isso: `login_valido_funciona`);
- nenhuma senha, token ou código de recuperação aparece em log, resposta de erro ou evidência;
- todo evento de autenticação relevante gera registro de auditoria (quando a F-05 existir).

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Depois do `stop db`, o `curl` ficou uns 30 segundos sem responder | o backend espera o banco antes de desistir | espere: é o comportamento que você está observando |
| Depois do `start db`, o login continua dando erro | o banco ainda está subindo | espere uns 20 segundos; se persistir, `bash roteiros/comum/zerar-banco.sh` |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-3`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 3 - priorizacao"
git push -u origin dossie/fase-3
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
   **compare** escolha `dossie/fase-3` → **Create pull request**.
2. Título: "Dossiê — Fase 3: priorização". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Cada requisito tem método, nota (ou "—"), risco e prioridade.
- [ ] Cada métrica do CVSS foi justificada com algo observado.
- [ ] Há um parágrafo explicando a escolha de cada método.
- [ ] Testei o comportamento sob falha (banco fora, token falso, sem token) e registrei o que vi.
- [ ] A DoD tem critérios verificáveis e específicos do G1.
