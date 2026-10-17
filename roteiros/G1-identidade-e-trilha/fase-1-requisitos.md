# G1 · Fase 1 — Requisitos de segurança e LGPD

> **Seção do dossiê:** §2 (e a coluna "Base legal" da §1) · **Lidera:** 🛡️ Segurança e 📋 DPO ·
> **Aula:** 2 · **Semanas:** 2–3

Cada passo mostra **🔎 como o Grupo 5 fez** (o grupo do [`docs/12`](../../docs/12-exemplo-dossie-preenchido.md))
e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. A coluna **Base legal** da §1 preenchida.
2. Na §2, **Forma 1:** requisitos escritos como declarações *shall* — pelo menos **um por
   funcionalidade** do pacote (F-01, F-05, F-11, F-12, F-17).
3. O **nível ASVS** alvo, com justificativa.
4. Na §2, **Forma 2:** os arquivos `.feature` — o do P0 já existe; os outros vocês criam com
   `@analise`.

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

**Requisito de segurança** é uma frase que diz **o que o sistema deve garantir** para que uma
ameaça não aconteça. Precisa ser **verificável**: dá para testar se foi cumprido.

> **Frase-síntese da disciplina:** requisito bem-feito = **ativo + ameaça + atributo + critério
> verificável + rastreabilidade**.

Cada requisito tem **duas formas**:

| Forma | Onde fica | Como é |
|---|---|---|
| **Declaração *shall*** ("deve") | no dossiê, §2 | **sem tecnologia**: não cita Java, endpoint, status HTTP. Vale mesmo se o VidaPlus for reescrito |
| **Cenário Gherkin** (`.feature`) | `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/` | **com tecnologia**: endereço, status, texto. É o que o computador testa |

**Base legal (LGPD)** responde: *por que a clínica pode tratar este dado?* As hipóteses estão no
**art. 7º** (dado pessoal comum) e no **art. 11** (dado sensível, como saúde). O **art. 46** não é
base legal: é o **dever de proteger** — ele aparece como **origem** dos requisitos de segurança.
Material: [`docs/02`](../../docs/02-lgpd.md).

**ASVS** (OWASP, **versão 4.0.3**) é um catálogo de requisitos. Capítulos que servem ao G1:

| Capítulo | Assunto | Funcionalidades |
|---|---|---|
| **V2** Authentication | senha, armazenamento de credencial, força bruta, recuperação de senha | F-01, F-12 |
| **V3** Session Management | token, expiração, logout | F-11 |
| **V5** Validation, Sanitization and Encoding | injeção (V5.3.4 = consultas parametrizadas) | F-01 (P0) |
| **V7** Error Handling and Logging | o que registrar, sem dado sensível; proteção do log | F-05 |
| **V14** Configuration | cabeçalhos de segurança e CORS (V14.4, V14.5) | F-17 |

Níveis: **L1** (mínimo), **L2** (recomendado para dados sensíveis), **L3** (o mais rigoroso).

---

## Passo 1 — Preencha a base legal da §1

> 🔎 **Como o Grupo 5 fez:** para cada linha da tabela de ativos, perguntou *"para que a clínica
> trata isto?"*:
>
> - nota clínica → tutela da saúde por profissional de saúde → **art. 11, II, "f"**;
> - credenciais e token → a conta existe para prestar o serviço → **art. 7º, V** (execução de contrato);
> - religião, estado civil, renda → nenhuma finalidade → **"— (sem finalidade)"** (um achado!);
> - chave do token, senha do banco → **"— (não é dado pessoal)"**.

**▶ Agora no G1:** faça o mesmo para cada ativo da sua §1. Atenção aos casos do G1:

- e-mail, hash de senha, papel, token, código de recuperação: **para que** cada um existe?
- o **registro de acessos** (quando existir) trata dados de quem? Para que a clínica o mantém?
  (Pista: leia o art. 37 da LGPD.)
- segredos do sistema: não são dados pessoais.

---

## Passo 2 — Escreva as ameaças "em palavras"

> 🔎 **Como o Grupo 5 fez:** escreveu, sem jargão, o que podia dar errado com cada ativo, usando o
> que viu na Fase 0. Ex.: *"copiar o cadastro de todos os pacientes pela busca, sem login"*;
> *"o texto de uma nota clínica fazer o navegador de quem a lê executar algo"*.

**▶ Agora no G1:** para cada funcionalidade, escreva **uma ou mais** ameaças no formato:

> *alguém [quem] consegue [o quê], porque [o que você observou na Fase 0].*

Perguntas que ajudam (respondam com o que **viram**):

- **F-01:** o que acontece com 100 senhas erradas seguidas? Como a senha está guardada? A resposta
  do login revela algo além de "certo/errado"?
- **F-11:** por quanto tempo um token vale? O que acontece com ele quando a pessoa clica em Sair?
- **F-12:** a resposta diz se o e-mail existe? O código de recuperação é adivinhável? Aparece na
  própria resposta?
- **F-17:** que sites a API aceita como origem? Que proteções o site pede ao navegador?
- **F-05:** o que acontece hoje se alguém entra na conta de outra pessoa — dá para descobrir
  depois?

Ainda **não** classifique em STRIDE/LINDDUN: é Fase 2.

---

## Passo 3 — Transforme cada ameaça num requisito *shall*

> 🔎 **Como o Grupo 5 fez** (com a busca de pacientes):
>
> - **1º rascunho:** *"A busca deve usar PreparedStatement."* → **rejeitado**: diz **como**
>   programar, e cita tecnologia.
> - **2º rascunho:** *"A busca deve ser segura contra injeção."* → **rejeitado**: "segura" não se
>   testa.
> - **Versão final (REQ-INJ-BUSCA-01):** *"A busca de pacientes **deve** devolver somente
>   cadastros que correspondam ao termo informado, qualquer que seja o conteúdo do termo."* →
>   **aceito**: não cita tecnologia, e dá para testar (mande um termo malicioso e veja se volta
>   alguém que não corresponde).

**▶ Agora no G1:** use a receita

```
O sistema deve <garantia>, <em que situação/escopo>, <sem que / de forma que ...>.
```

e passe cada frase no **teste de qualidade** — responda "sim" às quatro:

1. Diz **o que** deve acontecer, não **como** programar?
2. **Não cita** tecnologia (HTTP, JSON, Spring, MySQL, JWT, MD5, Argon2)?
3. Dá para **imaginar um teste** que passa ou falha?
4. Combate **uma ameaça concreta** do Passo 2?

**O requisito do P0 já está pronto** (é o do teste-semente):

| ID | *shall* | Prior. | Origem | Ameaça |
|---|---|:-:|---|---|
| REQ-AUTH-SQLI-01 | O sistema **deve** rejeitar tentativas de autenticação com conteúdo de injeção, sem autenticar e sem emitir credencial de sessão. | P0 | art. 46 · ASVS V5.3.4 | entrar como outro usuário sem saber a senha |

Escreva os outros. IDs sugeridos:

| Func. | Prefixo | Observação |
|---|---|---|
| F-01 (credencial, tentativas, enumeração) | `REQ-AUTH-...` | o recorte do G1 é **o fluxo de autenticação inteiro**, não só a injeção |
| F-05 | `REQ-AUDIT-...` | pense em **três** garantias: o que registrar; o que **não** registrar; como provar que ninguém alterou |
| F-11 | `REQ-SESS-...` | já existe `sessao-analise.feature` com `REQ-SESS-01` (veja o Passo 6) |
| F-12 | `REQ-RESET-...` | |
| F-17 | `REQ-HDR-...` / `REQ-CORS-...` | |

Preencha a tabela da Forma 1 com: **ID**, **shall**, **Prioridade** (P0/P1/P2, por enquanto a do
catálogo — ela é revista na Fase 3), **Base legal / ASVS** (ex.: `art. 46 · ASVS V2.2.1`) e
**Ameaça em palavras** (a do Passo 2).

---

## Passo 4 — Escolha o nível ASVS e justifique

> 🔎 **Como o Grupo 5 fez:** *"L2 — na Fase 0 classificamos como Crítico o resultado de exame, a
> nota clínica e o cadastro (dado de saúde, art. 11; religião é sensível). O L1 não basta para
> dado sensível. O L3 fica para os módulos mais críticos, como o acesso ao laudo, que não estão no
> nosso pacote."*

**▶ Agora no G1:** uma ou duas frases ligando o nível aos ativos que **vocês** classificaram como
Crítico. Uma pergunta para a justificativa: se a porta de entrada (o login) cair, o que fica
exposto? (Pista: tudo o que o usuário enganado pode ver.) Diga também que usam a **versão 4.0.3**.

---

## Passo 5 — Rode o teste do P0 e veja-o falhar

> 🔎 **Como o Grupo 5 fez:** abriu o `sqli.feature`, leu o cenário em voz alta e rodou o teste. O
> resultado foi `expected: <401> but was: <200>`: o sistema **aceitou** o login com o ataque.
> Guardou a saída como a evidência do "antes".

**▶ Agora no G1** — o P0 de vocês é **o mesmo teste**:

1. Abra `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/sqli.feature` e leia.
2. No terminal (na raiz do repositório), religue o VidaPlus com o banco original e rode o teste
   do P0. O `rodar-testes.sh` roda os testes dentro de um container, sem precisar instalar Java (o
   que ele faz, passo a passo: [`comum/04` §2](../comum/04-testes-e-pipeline.md#2-rodar-os-testes-no-seu-computador)):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   bash roteiros/comum/rodar-testes.sh "security & F-01" 2>&1 | tee entregaveis/evidencias/fase1-sqli-vermelho.txt
   ```

   (Como ler: [`comum/04` §2.1](../comum/04-testes-e-pipeline.md#21-lendo-o-resultado). A primeira
   vez demora.)
3. Ache a linha `expected: <401> but was: <200>` e explique, em uma frase no rascunho, o que ela
   prova.

---

## Passo 6 — Crie os `.feature` dos requisitos só analisados

> 🔎 **Como o Grupo 5 fez:** para o requisito de busca mínima (que não ia implementar), criou
> `busca-minima-analise.feature`, com a tag **`@analise`** e cenários usando as frases prontas:
>
> ```gherkin
> # language: pt
> @F-13 @REQ-PRIV-BUSCA-01 @P1 @lgpd-art6 @linddun-identificabilidade @analise
> Funcionalidade: F-13 · Busca mínima (REQ-PRIV-BUSCA-01)
>   # Só analisado: entra no plano de remediação (§6.2).
>
>   Cenário: termo curto demais é recusado
>     Dado que estou autenticado como "recepcao@vidaplus.local" com senha "Recep123"
>     Quando faço GET em "/api/pacientes/busca?q=Si"
>     Então o status HTTP é 400
> ```

**▶ Agora no G1:** para cada requisito que **não** é o P0, crie um arquivo
`<assunto>-analise.feature` na pasta dos `.feature`. Modelo:

```gherkin
# language: pt
@F-12 @REQ-RESET-01 @P1 @lgpd-art46 @asvs-V2 @stride-<categoria> @analise
Funcionalidade: F-12 · <nome curto do requisito> (REQ-RESET-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-RESET-01.
  # Só analisado: entra no plano de remediação (Fase 4).

  Cenário: <o que deve acontecer, em uma frase>
    <Dado ...>
    Quando <...>
    Então <...>
```

Regras e dicas:

- **`@analise` no fim da segunda linha é obrigatório:** é o que impede o arquivo de rodar no
  pipeline.
- Use as **frases prontas** de [`comum/04` §1.2](../comum/04-testes-e-pipeline.md#12-frases-que-o-computador-já-sabe-executar)
  quando servirem. Ex.: para F-12, `Quando faço POST em ...` não serve (o reset precisa de corpo
  JSON) — então escreva uma frase nova em português claro, como
  `Quando peço a recuperação de senha para o e-mail "naoexiste@teste.com"`.
- **F-11:** abra o `sessao-analise.feature`, que já existe. Se o requisito de vocês for o mesmo,
  adotem-no (ajustem o texto da *shall* no dossiê); se for outro, criem outro arquivo.
- **F-05:** os cenários descrevem o registro, por exemplo: `Então existe um registro do evento
  "LOGIN_FALHOU" com data, hora e o número interno do usuário` e `E o registro não contém a senha
  digitada`.
- Na tag `@stride-...`, deixe um palpite; vocês ajustam na Fase 2a.

## Passo 7 — Monte a Forma 2 no dossiê

> 🔎 **Como o Grupo 5 fez:** uma tabela "requisito → arquivo → estado"
> ([`docs/12` §2, Forma 2](../../docs/12-exemplo-dossie-preenchido.md#forma-2--cenários-gherkin-feature-vinculados-à-tecnologia)).

**▶ Agora no G1:** a mesma tabela. `sqli.feature` com `@security`; os outros com `@analise`.

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
- [ ] Há requisito *shall* para F-01, F-05, F-11, F-12 e F-17.
- [ ] Nenhuma *shall* cita tecnologia; todas passam no teste de qualidade.
- [ ] Cada requisito tem um `.feature`.
- [ ] Rodei o teste do P0 e guardei a saída vermelha.

## Erros comuns

- **Requisito que é solução** ("usar Argon2id"). O requisito é o **efeito**: "guardar a credencial
  de forma que, se a base vazar, recuperar as senhas não seja viável".
- **Requisito vago** ("o login deve ser seguro").
- **Esquecer `@analise`:** o arquivo roda no pipeline e falha.
- **Base legal = art. 46.**
