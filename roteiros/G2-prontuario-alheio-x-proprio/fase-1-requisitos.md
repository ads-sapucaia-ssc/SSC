# G2 · Fase 1 — Requisitos de segurança e LGPD

> **Seção do dossiê:** §2 (e a coluna "Base legal" da §1) · **Lidera:** 🛡️ Segurança e 📋 DPO ·
> **Aula:** 2 · **Semanas:** 2–3

Cada passo mostra **🔎 como o Grupo 5 fez** (o grupo do [`docs/12`](../../docs/12-exemplo-dossie-preenchido.md))
e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. A coluna **Base legal** da §1 preenchida.
2. Na §2, **Forma 1:** requisitos *shall* — pelo menos **um por funcionalidade** (F-02, F-09,
   F-07, F-08).
3. O **nível ASVS** alvo, com justificativa.
4. Na §2, **Forma 2:** os `.feature` — o do P0 já existe; os outros com `@analise`.

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
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

**Requisito de segurança:** uma frase que diz **o que o sistema deve garantir** para que uma
ameaça não aconteça, de forma **verificável**.

> **Frase-síntese:** requisito bem-feito = **ativo + ameaça + atributo + critério verificável +
> rastreabilidade**.

Cada requisito tem **duas formas**: a **declaração *shall*** (no dossiê, **sem tecnologia**) e o
**cenário Gherkin** (`.feature`, **com tecnologia**: endereço, status, texto). Só existe com as
duas.

**Base legal (LGPD):** *por que a clínica pode tratar este dado?* — art. 7º (dado comum) ou
art. 11 (sensível). Resultado de exame é **dado de saúde** → art. 11. O **art. 46** não é base
legal: é o dever de proteger. E o **art. 18** lista os **direitos do titular** — entre eles, a
**portabilidade** (inciso V), que é a F-09. Material: [`docs/02`](../../docs/02-lgpd.md).

**ASVS** (OWASP, **versão 4.0.3**). Capítulos que servem ao G2:

| Capítulo | Assunto | Funcionalidades |
|---|---|---|
| **V4** Access Control | o servidor decide quem acessa o quê; negar por padrão; proteção contra IDOR (V4.2.1) | F-02 (P0), F-07 |
| **V8** Data Protection | dados sensíveis; V8.3.2: o usuário consegue **exportar** os próprios dados | F-09, F-02 |
| **V12** Files and Resources | arquivos guardados e entregues com segurança | F-08 |
| **V6** Stored Cryptography | hash para provar integridade | F-07 |
| **V7** Error Handling and Logging | registrar quem publicou | F-07 |

Níveis: **L1**, **L2** (recomendado para dados sensíveis), **L3**.

---

## Passo 1 — Preencha a base legal da §1

> 🔎 **Como o Grupo 5 fez:** nota clínica → **art. 11, II, "f"** (tutela da saúde); credenciais →
> **art. 7º, V**; religião sem uso → **"— (sem finalidade)"**; segredos → **"— (não é dado
> pessoal)"**.

**▶ Agora no G2:** para cada ativo da sua §1, pergunte **para que** a clínica o trata.
Atenção aos casos do G2: resultado e arquivo do laudo (dado de saúde); vínculo médico–paciente
(revela que a pessoa se trata com aquele médico — é dado de saúde?); quem publicou o laudo (dado de
quem? do funcionário).

---

## Passo 2 — Escreva as ameaças "em palavras"

> 🔎 **Como o Grupo 5 fez:** *"copiar o cadastro de todos os pacientes pela busca, sem login"*.

**▶ Agora no G2:** para cada funcionalidade, no formato
*"alguém [quem] consegue [o quê], porque [o que você observou]"*. Perguntas:

- **F-02:** a Ana viu o exame do Bruno — por quais caminhos (endereços) ela consegue? E um médico,
  vê exames de quem ele não atende? E a recepção, que não deveria ver conteúdo clínico?
- **F-08:** quem consegue baixar um laudo **sem login**? E um laudo **não publicado**?
- **F-07:** quem consegue publicar um laudo? Se um laudo publicado for alterado, alguém percebe?
- **F-09:** a Ana consegue hoje receber os próprios dados? O que acontece com o direito dela?

---

## Passo 3 — Transforme cada ameaça num requisito *shall*

> 🔎 **Como o Grupo 5 fez** (com a busca):
>
> - 1º rascunho: *"A busca deve usar PreparedStatement."* → **rejeitado** (é solução, cita
>   tecnologia).
> - 2º: *"A busca deve ser segura contra injeção."* → **rejeitado** ("segura" não se testa).
> - Final: *"A busca de pacientes **deve** devolver somente cadastros que correspondam ao termo
>   informado, qualquer que seja o conteúdo do termo."* → **aceito**.

**▶ Agora no G2:** receita

```
O sistema deve <garantia>, <em que situação/escopo>, <sem que / de forma que ...>.
```

**Teste de qualidade** (responda "sim" às quatro): diz **o que**, não **como**? **Não cita**
tecnologia (HTTP, 403, URL, JWT, nginx)? Dá para **imaginar um teste**? Combate **uma ameaça
concreta**?

**O requisito do P0 já está pronto:**

| ID | *shall* | Prior. | Origem | Ameaça |
|---|---|:-:|---|---|
| REQ-AC-IDOR-01 | O sistema **deve** validar a titularidade do recurso no servidor e negar o acesso ao laudo — sem revelar metadados — quando o solicitante não for o dono. | P0 | arts. 11, 46 · ASVS V4.2.1 | um paciente ver o laudo de outro trocando o número na URL |

Repare em **"sem revelar metadados"**: negar o acesso mas responder "este exame é uma glicemia do
paciente 2" ainda vaza dado. O teste confere isso (`a resposta não contém "Glicemia"`).

Repare também que o P0 fala de **laudo**, mas vocês viram **três** endereços que entregam o
laudo (exame, PDF, listagem). O requisito, por ser agnóstico, cobre os três.

Escreva os outros. IDs sugeridos:

| Func. | Prefixo | Pense em |
|---|---|---|
| F-02 (médico e outros papéis) | `REQ-AC-...` | o médico só vê quem atende; papéis sem função clínica não veem conteúdo clínico |
| F-09 | `REQ-PORT-...` | o titular recebe **todos** os **seus** dados, em formato estruturado, só depois de se identificar — e **nada** de outra pessoa |
| F-07 | `REQ-PUB-...` | **quem** pode publicar; e como provar que o laudo publicado **não foi alterado** |
| F-08 | `REQ-ARQ-...` | o arquivo só é entregue a quem pode ver o exame — inclusive (e principalmente) o **não publicado** |

---

## Passo 4 — Escolha o nível ASVS e justifique

> 🔎 **Como o Grupo 5 fez:** *"L2 — dado de saúde e religião (sensíveis); L1 não basta. O L3 fica
> para os módulos mais críticos, como o acesso ao laudo, que não estão no nosso pacote."*

**▶ Agora no G2:** repare que o Grupo 5 citou o **acesso ao laudo** como candidato a **L3** — e o
acesso ao laudo **é o pacote de vocês**. Decidam: L2 ou L3? Justifiquem com o que classificaram
como Crítico. Digam que usam a **versão 4.0.3**.

---

## Passo 5 — Rode o teste do P0 e veja-o falhar

> 🔎 **Como o Grupo 5 fez:** leu o cenário em voz alta, rodou, viu `expected: <401> but was: <200>`
> e guardou a saída como evidência do "antes".

**▶ Agora no G2:**

1. Abra `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature` e leia.
2. No terminal (na raiz do repositório), religue o VidaPlus com o banco original e rode o teste
   do P0. O `rodar-testes.sh` roda os testes dentro de um container, sem precisar instalar Java (o
   que ele faz, passo a passo: [`comum/04` §2](../comum/04-testes-e-pipeline.md#2-rodar-os-testes-no-seu-computador)):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase1-idor-vermelho.txt
   ```

   (Como ler: [`comum/04` §2.1](../comum/04-testes-e-pipeline.md#21-lendo-o-resultado).)
3. Ache `expected: <403> but was: <200>` e explique, no rascunho, o que prova.

---

## Passo 6 — Crie os `.feature` dos requisitos só analisados

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

**▶ Agora no G2:** um arquivo `<assunto>-analise.feature` por requisito que **não** é o P0.
Modelo:

```gherkin
# language: pt
@F-08 @REQ-ARQ-01 @P1 @lgpd-art11 @lgpd-art46 @asvs-V12 @stride-<categoria> @analise
Funcionalidade: F-08 · <nome curto> (REQ-ARQ-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-ARQ-01.
  # Só analisado: entra no plano de remediação (Fase 4).

  Cenário: <o que deve acontecer>
    <Dado ...>
    Quando <...>
    Então <...>
```

Dicas do G2:

- Muitos cenários do G2 cabem **nas frases prontas** ([`comum/04` §1.2](../comum/04-testes-e-pipeline.md#12-frases-que-o-computador-já-sabe-executar)).
  Ex.: a médica que não atende o Bruno pedindo o exame dele:

  ```gherkin
  Cenário: médica não acessa exame de quem ela não atende
    Dado que estou autenticado como "dra.house@vidaplus.local" com senha "Medico123"
    Quando faço GET em "/api/exames/102"
    Então o status HTTP é 403
  ```

- **F-08 (arquivo sem login):** não há frase pronta para "sem login"; escreva uma nova:
  `Dado que não estou autenticado`.
- **F-09:** descreva a exportação: `Quando peço a exportação dos meus dados` / `Então recebo um
  arquivo estruturado com os meus exames` / `E o arquivo não contém dados de outro paciente`.
- **F-07 (quem publica):** o arquivo `rbac.feature` já existe e testa "paciente não pode publicar
  laudo" — mas é o **P0 do G3**. No G2, citem-no como teste do requisito de vocês, **sabendo que
  ele continua vermelho** (não é implementado no G2). Para a **integridade** do laudo publicado,
  criem um `@analise` próprio.

## Passo 7 — Monte a Forma 2 no dossiê

> 🔎 **Como o Grupo 5 fez:** tabela "requisito → arquivo → estado" ([`docs/12` §2, Forma 2](../../docs/12-exemplo-dossie-preenchido.md#forma-2--cenários-gherkin-feature-vinculados-à-tecnologia)).

**▶ Agora no G2:** `idor.feature` com `@security`; os outros com `@analise`. 

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
- [ ] Há requisito *shall* para F-02, F-09, F-07 e F-08.
- [ ] Nenhuma *shall* cita tecnologia; todas passam no teste de qualidade.
- [ ] Cada requisito tem um `.feature`.
- [ ] Rodei o teste do P0 e guardei a saída vermelha.

## Erros comuns

- **Escrever o requisito pensando só em pacientes.** E o médico? E a recepção?
- **Requisito que é solução** ("usar URL assinada"). O requisito é o efeito: "o arquivo só é
  entregue a quem pode ver o exame".
- **Esquecer `@analise`.**
