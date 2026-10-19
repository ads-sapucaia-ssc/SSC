# G3 · Fase 1 — Requisitos de segurança e LGPD

> **Seção do dossiê:** §2 (e a coluna "Base legal" da §1) · **Lidera:** 🛡️ Segurança e 📋 DPO ·
> **Aula:** 2 · **Semanas:** 2–3

Cada passo mostra **🔎 como o Grupo 5 fez** (o grupo do [`docs/12`](../../docs/12-exemplo-dossie-preenchido.md))
e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. A coluna **Base legal** da §1 preenchida.
2. Na §2, **Forma 1:** requisitos *shall* — pelo menos **um por funcionalidade** (F-03, F-04,
   F-15, F-19).
3. O **nível ASVS** alvo, com justificativa.
4. Na §2, **Forma 2:** os `.feature` — o do P0 já existe; os outros com `@analise`.

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
   git checkout -b dossie/fase-1
   git push -u origin dossie/fase-1
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-1
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

**Requisito de segurança:** uma frase que diz **o que o sistema deve garantir**, de forma
**verificável**. Duas formas: a **declaração *shall*** (no dossiê, **sem tecnologia**) e o
**cenário Gherkin** (`.feature`, **com tecnologia**). Só existe com as duas.

> **Frase-síntese:** requisito bem-feito = **ativo + ameaça + atributo + critério verificável +
> rastreabilidade**.

**Base legal (LGPD) — o assunto central do G3.** A clínica precisa de uma **hipótese legal** para
tratar cada dado: art. 7º (dado comum) e art. 11 (dado sensível). Três pontos que o G3 precisa
dominar:

1. **Consentimento é só uma das bases** (art. 7º, I; art. 11, I) — e não é a do atendimento
   médico. Para tratar a saúde do paciente, a base é a **tutela da saúde** (art. 11, II, "f"), que
   **não depende** de consentimento. Pedir consentimento para algo que não depende dele engana o
   paciente (ele acha que pode recusar, e não pode).
2. **Consentimento serve para finalidades opcionais** — por exemplo, usar dados em pesquisa,
   mandar comunicações que o paciente não pediu, compartilhar com um parceiro. O paciente pode
   dizer **não** e continuar sendo atendido.
3. **Consentimento precisa ser** livre, informado, inequívoco, para **finalidade determinada**
   (art. 5º, XII), **revogável** a qualquer momento (art. 8º, §5º), e **a clínica precisa provar**
   que o obteve (art. 8º, §2º).

E dois princípios: **finalidade** e **necessidade/minimização** (art. 6º, I e III) — a base da
F-15. O **art. 18** lista os direitos do titular, entre eles **acesso** e **correção** (F-19).
Material: [`docs/02`](../../docs/02-lgpd.md).

**ASVS** (OWASP, **versão 4.0.3**). Capítulos que servem ao G3:

| Capítulo | Assunto | Funcionalidades |
|---|---|---|
| **V4** Access Control | o servidor decide (V4.1.1); o usuário não pode alterar o próprio papel (V4.1.2); menor privilégio (V4.1.3); negar por padrão (V4.1.5) | F-03 (P0) |
| **V5** Validation | proteção contra **atribuição em massa** — aceitar campos que a tela nem tem (V5.1.2) | F-19 |
| **V8** Data Protection | dados sensíveis; linguagem clara sobre coleta e consentimento (V8.3.3) | F-04, F-15 |

Níveis: **L1**, **L2** (recomendado para dados sensíveis), **L3**.

---

## Passo 1 — Preencha a base legal da §1

> 🔎 **Como o Grupo 5 fez:** nota clínica → **art. 11, II, "f"**; credenciais → **art. 7º, V**;
> religião sem uso → **"— (sem finalidade)"**; segredos → **"— (não é dado pessoal)"**.

**▶ Agora no G3:** para cada ativo da §1, **para que** a clínica o trata? Casos do G3:

- **agenda** (quem se trata com qual médico): é dado de saúde?
- **e-mail e papel dos funcionários**: base de quem? (Os funcionários também são titulares.)
- **religião, estado civil, renda**: a lista coluna | quem usa | finalidade (Fase 0, Passo 4)
  responde. Sem finalidade → escreva isso.
- **funções restritas** (publicar, desativar): não são dados pessoais, mas são ativos — "—".

---

## Passo 2 — Escreva as ameaças "em palavras"

> 🔎 **Como o Grupo 5 fez:** *"copiar o cadastro de todos os pacientes pela busca, sem login"*.

**▶ Agora no G3:** formato *"alguém [quem] consegue [o quê], porque [o que você observou]"*.
Perguntas:

- **F-03:** o que um paciente consegue fazer que só o laboratório, a recepção ou o admin
  deveriam? (A matriz da Fase 0.)
- **F-04:** o que acontece hoje se a clínica quiser usar os dados para uma finalidade opcional —
  o paciente é perguntado? Consegue dizer não? Consegue mudar de ideia?
- **F-15:** que dados a clínica guarda sem precisar? O que acontece se eles vazarem?
- **F-19:** o paciente consegue ver e corrigir os próprios dados? Quem consegue editar o cadastro
  de quem? O que o servidor aceita receber?

---

## Passo 3 — Transforme cada ameaça num requisito *shall*

> 🔎 **Como o Grupo 5 fez** (com a busca):
>
> - 1º rascunho: *"A busca deve usar PreparedStatement."* → **rejeitado** (é solução, cita
>   tecnologia).
> - 2º: *"A busca deve ser segura contra injeção."* → **rejeitado** ("segura" não se testa).
> - Final: *"A busca de pacientes **deve** devolver somente cadastros que correspondam ao termo
>   informado, qualquer que seja o conteúdo do termo."* → **aceito**.

**▶ Agora no G3:** receita e **teste de qualidade** (diz **o que**, não **como**? sem tecnologia?
testável? combate ameaça concreta?):

```
O sistema deve <garantia>, <em que situação/escopo>, <sem que / de forma que ...>.
```

**O requisito do P0 já está pronto:**

| ID | *shall* | Prior. | Origem | Ameaça |
|---|---|:-:|---|---|
| REQ-AC-RBAC-01 | O sistema **deve** permitir publicar laudo apenas a quem tem o papel autorizado; qualquer outro papel **deve** ser negado (*deny-by-default*). | P0 | art. 46 · ASVS V4.1.5 | um paciente (ou outro papel não autorizado) publicar um laudo |

Repare que o P0 fala só de **publicar laudo**, mas a Fase 0 mostrou **a mesma falha em todas as
funções restritas**. Decidam: um requisito por função, ou um requisito geral ("toda função
restrita a um papel...") **mais** o do P0? As duas formas são aceitáveis — justifiquem.

Escrevam os outros. IDs sugeridos:

| Func. | Prefixo | Pense em |
|---|---|---|
| F-03 (demais funções) | `REQ-AC-RBAC-0x` | gestão de usuários, agenda, carteira do médico, fila de laudos, edição de cadastro; e **o próprio papel** — quem pode mudá-lo? |
| F-04 | `REQ-CONS-...` | registrar o consentimento **por finalidade e versão do termo**; só tratar para aquela finalidade com consentimento válido; **revogar** tão fácil quanto consentir; **provar** depois |
| F-15 | `REQ-MIN-...` | coletar e devolver só o necessário para cada finalidade |
| F-19 | `REQ-PERFIL-...` | o titular vê e corrige os **próprios** dados; o servidor aceita **só** os campos permitidos e **valida** cada um |

---

## Passo 4 — Escolha o nível ASVS e justifique

> 🔎 **Como o Grupo 5 fez:** *"L2 — dado de saúde e religião (sensíveis); L1 não basta."*

**▶ Agora no G3:** uma ou duas frases. Uma pergunta para a justificativa: se o controle por papel
falha, **que dados e que funções** ficam abertos? (Todos os do sistema.) Digam que usam a **versão
4.0.3**.

---

## Passo 5 — Rode o teste do P0 e veja-o falhar

> 🔎 **Como o Grupo 5 fez:** leu o cenário em voz alta, rodou, viu `expected: <401> but was: <200>`
> e guardou a saída do "antes".

**▶ Agora no G3:**

1. Abra `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/rbac.feature` e leia.
2. No terminal (na raiz do repositório), religue o VidaPlus com o banco original e rode o teste
   do P0. O `rodar-testes.sh` roda os testes dentro de um container, sem precisar instalar Java (o
   que ele faz, passo a passo: [`comum/04` §2](../comum/04-testes-e-pipeline.md#2-rodar-os-testes-no-seu-computador)):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   bash roteiros/comum/rodar-testes.sh "security & F-03" 2>&1 | tee entregaveis/evidencias/fase1-rbac-vermelho.txt
   ```

3. Ache `expected: <403> but was: <200>` e explique o que prova.

> ⚠️ O teste **publica o laudo 104** de verdade no banco (no código original, a publicação dá
> certo). Depois de rodar, **zere o banco** (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`) antes de explorar o sistema de novo.

---

## Passo 6 — Os `.feature` dos outros requisitos

> 🔎 **Como o Grupo 5 fez:** criou `busca-minima-analise.feature`, com **`@analise`** e as frases
> prontas:
>
> ```gherkin
> # language: pt
> @F-13 @REQ-PRIV-BUSCA-01 @P1 @lgpd-art6 @linddun-identificabilidade @analise
> Funcionalidade: F-13 · Busca mínima (REQ-PRIV-BUSCA-01)
>   Cenário: termo curto demais é recusado
>     Dado que estou autenticado como "recepcao@vidaplus.local" com senha "Recep123"
>     Quando faço GET em "/api/pacientes/busca?q=Si"
>     Então o status HTTP é 400
> ```
>
> E, para o requisito que **ia implementar** (a injeção na busca), escreveu os cenários com
> `@security`, porque eles iam passar depois da correção.

**▶ Agora no G3** — vocês têm uma vantagem: os requisitos de RBAC cabem **inteiros** nas frases
prontas ([`comum/04` §1.2](../comum/04-testes-e-pipeline.md#12-frases-que-o-computador-já-sabe-executar)).

**Para as funções que vocês vão corrigir na Fase 4**, acrescentem cenários **no próprio
`rbac.feature`** (eles herdam a tag `@security` do cabeçalho e passam a rodar no gate). Exemplos:

```gherkin
  Cenário: paciente não lista os usuários do sistema
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    Quando faço GET em "/api/usuarios"
    Então o status HTTP é 403

  Cenário: o laboratório continua publicando laudo
    Dado que estou autenticado como "bio@vidaplus.local" com senha "Bio123"
    Quando faço POST em "/api/exames/104/publicar"
    Então o status HTTP é 200
```

Repare no segundo: é o **uso legítimo** — a correção não pode quebrá-lo. Escrevam cenários de
**negação** e de **permissão** para cada função.

> ⚠️ Esses cenários novos ficam **vermelhos até a Fase 4** (o de permissão já passa hoje). É
> esperado: eles descrevem o comportamento que a correção vai entregar.

**Para F-04, F-15 e F-19** (só analisados), criem arquivos `<assunto>-analise.feature` com
**`@analise`**:

```gherkin
# language: pt
@F-04 @REQ-CONS-01 @P0 @lgpd-art8 @asvs-V8 @linddun-intervencao @analise
Funcionalidade: F-04 · <nome curto> (REQ-CONS-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-CONS-01.
  # Só especificado: a criação do F-04 não é implementada nesta turma.

  Cenário: tratamento para a finalidade é negado depois da revogação
    Dado que o paciente consentiu com a finalidade "<finalidade>" na versão "1.0" do termo
    E que o paciente revogou esse consentimento
    Quando a clínica tenta usar os dados dele para "<finalidade>"
    Então o uso é negado
    E fica registrado quando o consentimento foi revogado
```

Para F-19, uma frase pronta serve para a **atribuição em massa**:
`Quando faço PUT ...` não existe — escreva uma nova:
`Quando envio a alteração do meu cadastro com o campo "papel" igual a "ADMIN"` /
`Então o campo "papel" é recusado`.

## Passo 7 — Monte a Forma 2 no dossiê

> 🔎 **Como o Grupo 5 fez:** tabela "requisito → arquivo → estado" ([`docs/12` §2, Forma 2](../../docs/12-exemplo-dossie-preenchido.md#forma-2--cenários-gherkin-feature-vinculados-à-tecnologia)).

**▶ Agora no G3:** `rbac.feature` com `@security` (com os cenários novos); os outros com
`@analise`.

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-1`:

```bash
git status
git add entregaveis/ legacy-app/backend/src/test/resources/
git commit -m "docs(dossie): fase 1 - requisitos"
git push -u origin dossie/fase-1
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
   **compare** escolha `dossie/fase-1` → **Create pull request**.
2. Título: "Dossiê — Fase 1: requisitos". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Toda linha da §1 tem base legal (ou "não é dado pessoal", ou "sem finalidade").
- [ ] Sei explicar por que o atendimento não depende de consentimento, e para que finalidade a
      F-04 vai pedir consentimento.
- [ ] Há requisito *shall* para F-03 (todas as funções restritas), F-04, F-15 e F-19.
- [ ] Os cenários do `rbac.feature` cobrem negação **e** permissão.
- [ ] Rodei o teste do P0, guardei a saída vermelha e zerei o banco.

## Erros comuns

- **Pedir consentimento para o atendimento.** A base é a tutela da saúde.
- **Requisito de RBAC só com "negar".** Sem o cenário de permissão, uma correção que nega **tudo**
  passaria no teste — e quebraria o sistema.
- **Esquecer `@analise`** nos arquivos só especificados.
