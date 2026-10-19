# 07 · Ferramentas de segurança do zero — o que cada uma faz e como ler o resultado

**Objetivo:** conhecer as ferramentas de segurança usadas no projeto: **para que serve** cada uma,
**como rodar** (com o comando completo), **como ler** o que ela diz e — tão importante quanto —
**o que ela não consegue encontrar**.

**Usado nas Fases:** 4 (inventário da falha), 5 (verificação) e 6 (pipeline).

**Antes:** VidaPlus ligado ([`01`](01-subir-o-vidaplus.md)); terminal na **raiz** do repositório
(o comando `ls` mostra `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`).

---

## 1. Os tipos de ferramenta — e o que cada tipo enxerga

Pense num prédio. Para saber se ele é seguro, dá para: **ler a planta** (sem entrar); **testar as
portas** com o prédio funcionando; **conferir os materiais** comprados de fora; **procurar chaves
esquecidas** debaixo do tapete. Cada atividade acha um tipo de problema — e nenhuma acha todos.

| Tipo | Sigla | Analogia | O que faz | Ferramenta no projeto |
|---|---|---|---|---|
| Análise estática | **SAST** | ler a planta | lê o **código-fonte**, sem rodar o sistema, procurando padrões perigosos | Semgrep, CodeQL |
| Análise dinâmica | **DAST** | testar as portas | **liga** o sistema e o examina de fora, como um visitante | OWASP ZAP |
| Análise de composição | **SCA** | conferir os materiais | procura falhas **já conhecidas** (CVE) nas **bibliotecas** de terceiros | Dependency-Check, Trivy |
| Varredura de segredos | — | procurar chaves debaixo do tapete | procura **senhas e chaves** gravadas no código **e no histórico** | Gitleaks |
| Varredura de container | — | conferir o terreno e a estrutura | procura CVE no **sistema operacional e programas** dentro da imagem Docker | Trivy |
| Inventário (SBOM) | — | a nota fiscal dos materiais | lista **todos os componentes** do software | Syft |
| Testes de segurança | — | um roteiro de inspeção escrito pela equipe | executa **ataques específicos** e confere a resposta | JUnit + Cucumber (`.feature`) |
| Teste manual | — | o inspetor experiente | você, com `curl` e o navegador, tentando caminhos que ninguém previu | `curl`, DevTools ([`02`](02-observar-o-sistema.md)) |

**Que ferramenta acha que falha do VidaPlus:**

| Falha | Quem acha | Quem **não** acha (e por quê) |
|---|---|---|
| Injeção de SQL (SQL montado colando texto) | Semgrep, CodeQL, teste `sqli.feature`, `curl` | ZAP do pipeline (é passivo: não envia ataques) |
| Paciente vê exame de outro (IDOR) | teste `idor.feature`, `curl` | **todos os scanners**: o código não tem nada de "errado" no texto — falta uma pergunta que só quem conhece a regra de negócio sabe fazer |
| Paciente usa função de admin (RBAC) | teste `rbac.feature`, `curl` | todos os scanners, pelo mesmo motivo |
| Senha em MD5 | Semgrep (`vidaplus-weak-hash-md5`) | ZAP (não vê o código) |
| Senha/chave no código | Gitleaks, Semgrep (`vidaplus-hardcoded-secret`) | testes |
| *Stack trace* nas respostas; cabeçalhos ausentes | ZAP, `curl -i` | Semgrep (a configuração está num `.properties`, que ele não lê) |
| XSS na nota clínica | Semgrep (`vidaplus-react-dangerous-html`) | testes atuais |
| Biblioteca com CVE | Dependency-Check, Trivy | todos os outros |

> **A lição:** ferramenta automática acha **padrões**. Falha de **lógica** (quem pode ver o quê) só
> se acha **pensando** e **testando** — é por isso que os testes de cada grupo existem.

---

## 2. Semgrep — procura padrões perigosos no código

**O que é:** um SAST. Ele lê os arquivos do código e compara cada trecho com **regras** — modelos
do tipo "texto de SQL seguido de `+` e de uma variável".

**As regras do projeto:** abra `security/semgrep/vidaplus.yml` (**Ctrl+P** → `vidaplus.yml`). Cada
bloco `- id:` é uma regra. Por exemplo:

```yaml
  - id: vidaplus-sql-concat              # o nome da regra
    languages: [java]                    # em que linguagem procurar
    severity: ERROR                      # gravidade
    message: >-                          # o texto que aparece quando acha
      Possivel SQL Injection (V-01): trecho de SQL concatenado com uma VARIAVEL. ...
    patterns:
      - pattern-regex: '"[^"\n]*\b(WHERE|AND|OR|SET|LIKE|VALUES)\b[^"\n]*"\s*\+\s*[A-Za-z_]'
```

A última linha é uma **expressão regular** (um padrão de texto). Em palavras: "um texto entre
aspas que contém `WHERE`, `AND`, `LIKE`… seguido de `+` e de um nome de variável".

**Como rodar no seu computador** (na raiz do repositório):

```bash
docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
  semgrep scan --config security/semgrep/vidaplus.yml --metrics=off legacy-app
```

| Parte | Significado |
|---|---|
| `docker run --rm` | roda um container e o apaga ao terminar |
| `-v "$PWD":/src -w /src` | mostra a pasta atual (o repositório) para o container, como `/src`, e começa lá |
| `returntocorp/semgrep` | a imagem com o Semgrep instalado (baixada na primeira vez) |
| `semgrep scan --config security/semgrep/vidaplus.yml` | roda usando **as regras do projeto** |
| `--metrics=off` | não envia estatísticas de uso para a empresa do Semgrep |
| `legacy-app` | a pasta a examinar |

**Como ler a saída.** Ela vem agrupada por **arquivo**, e dentro de cada arquivo por **regra**:

```
    legacy-app/backend/src/main/java/br/ifsul/vidaplus/dao/UsuarioDao.java
   ❯❯❱ security.semgrep.vidaplus-sql-concat
          Possivel SQL Injection (V-01): trecho de SQL concatenado com uma VARIAVEL.
          ...
           41┆ "WHERE email = '" + email + "' AND ativo = 1";
            ⋮┆----------------------------------------
           79┆ "WHERE u.email = '" + email + "' " +
```

- a primeira linha é o **arquivo**;
- `❯❯❱ ...vidaplus-sql-concat` é a **regra** que disparou (o número de `❯` indica a gravidade);
- depois, a **mensagem** da regra;
- e cada `41┆` é **a linha do arquivo** onde está o achado, com o trecho de código.

No fim: `Ran 5 rules on 42 files: 11 findings.` — 5 regras, 42 arquivos, 11 achados.

**Para contar os achados de cada regra** (útil para "antes × depois"):

```bash
docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
  semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
  | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c
```

Resultado no código original:

```
      1 vidaplus-hardcoded-secret
      1 vidaplus-jwt-no-expiration
      1 vidaplus-react-dangerous-html
      7 vidaplus-sql-concat
      1 vidaplus-weak-hash-md5
```

**No pipeline:** job **Gate - SAST (Semgrep)**. Ele usa as regras do projeto **e** dois conjuntos
públicos (`p/owasp-top-ten` e `p/java`), por isso acha **mais** coisas que o comando acima. Os
alertas vão para a aba **Security → Code scanning** ([`06` §5.7](06-pipeline-do-zero.md#57-ver-alertas-na-aba-security-sast)).

**O que o Semgrep não vê:** falhas de lógica (IDOR, RBAC); configuração fora do código Java (o
`application.properties`); SQL montado de outro jeito (`String.format`, `StringBuilder`) que a
regra não cobre. **Zero achados não prova que não há falha.**

---

## 3. CodeQL — segue o caminho do dado

**O que é:** outro SAST, de outro jeito. Em vez de comparar texto, ele monta um **mapa do
programa** e segue o **caminho do dado**: de onde ele **entra** (um parâmetro do pedido HTTP) até
onde ele é **usado de forma perigosa** (uma consulta ao banco). Se o caminho existe sem proteção no
meio, ele avisa.

**Como rodar:** só no pipeline (job **Gate - SAST (CodeQL)**). Rodar no computador exige instalar
o CodeQL — não é necessário nesta disciplina.

**Onde ver:** aba **Security → Code scanning**, filtro **Tool** = CodeQL
([`06` §5.7](06-pipeline-do-zero.md#57-ver-alertas-na-aba-security-sast)). Cada alerta mostra a
**fonte** (de onde o dado veio) e o **sorvedouro** (onde foi usado).

**Diferença prática em relação ao Semgrep:** o CodeQL sabe que um número (`long`) não carrega
comando SQL; o Semgrep, que só lê o texto, não sabe. Por isso os dois podem discordar — e explicar
a diferença é um ótimo argumento na defesa.

**O que não vê:** falhas de lógica (IDOR, RBAC) e configuração.

---

## 4. Gitleaks — procura segredos, inclusive no passado

**O que é:** uma varredura de **segredos**: senhas, chaves, tokens gravados em arquivos. Ele
examina **o histórico inteiro do Git**, commit por commit — não só os arquivos de hoje.

**As regras do projeto:** `security/gitleaks/gitleaks.toml` (ele usa as regras padrão **mais**
três do VidaPlus: a chave do JWT e a senha do banco nos arquivos de configuração, e senhas ou
chaves escritas como constante no código Java — como a do `JwtUtil.java`, que as regras padrão
não reconhecem por ser um texto legível).

**Como rodar** (na raiz do repositório):

```bash
docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
  detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
  --log-opts="--all -- legacy-app" --no-banner --redact -v
```

(`detect` = procurar; `--source /repo` = onde; `--log-opts="--all -- legacy-app"` = só o
histórico da pasta `legacy-app`, que é o sistema — `docs/` e `roteiros/` são material da
disciplina; `--redact` = esconder o valor do segredo; `-v` = mostrar cada achado em detalhe.)

**Como ler a saída.** Um bloco por achado:

```
Finding:     vidaplus.jwt.secret=REDACTED
Secret:      REDACTED
RuleID:      vidaplus-jwt-secret
Entropy:     3.761446
Tags:        [secret jwt V-05]
File:        legacy-app/backend/src/test/resources/application.properties
Line:        7
Commit:      ec24e658ca707a36966cd7afc479c19b55e31c4e
Author:      Nome de Quem Fez o Commit
Email:       email@exemplo.com
Date:        2026-07-10T17:39:37Z
```

| Campo | Significado |
|---|---|
| `Finding` | o trecho onde o segredo aparece (com o valor escondido) |
| `Secret` | o segredo em si — `REDACTED` por causa do `--redact`; o valor de verdade está no arquivo e na linha indicados |
| `RuleID` | a regra que o reconheceu |
| `Entropy` | o quanto o texto "parece aleatório" (senhas e chaves costumam ser) |
| `File`, `Line` | arquivo e linha |
| `Commit`, `Author`, `Date` | **em que momento da história** o segredo entrou, e por quem |

No fim: `leaks found: N`. No VidaPlus original, **8**: a senha do banco e a chave do token em
`application.properties` (o de produção e o de teste), a chave no `JwtUtil.java` e três senhas no
`docker-compose.yml`. Depois da correção guiada da F-10, na Fase 6
([`08`](08-segredos-fora-do-codigo.md)), a última linha passa a ser `no leaks found`.

> **Por que `--redact`?** Sem ele, o Gitleaks imprime o segredo na tela — e, se você grava a
> saída como evidência, o segredo vai **de novo** para o Git, num arquivo novo. Quem procura
> segredos não pode espalhá-los.

**A lição do `Commit`:** o Gitleaks pode acusar um arquivo que **já foi apagado** — porque ele
continua no histórico. Por isso, quando um segredo vaza no Git, apagar o arquivo **não basta**: o
valor precisa ser **trocado**.

**No pipeline:** job **Gate - Secret Scanning (Gitleaks)** — leia o log ([`06` §5.3](06-pipeline-do-zero.md#53-ler-o-log-de-um-job-e-procurar-um-texto)).

> ⚠️ Não copie os valores dos segredos para o dossiê: escreva **o que** é e **onde** está.

---

## 5. OWASP ZAP — examina o sistema ligado, de fora

**O que é:** um DAST. Ele age como um **navegador curioso**: visita as páginas e a API do sistema
**ligado** e examina **as respostas** — cabeçalhos ausentes, mensagens de erro que revelam demais,
cookies sem proteção.

**Baseline = passivo.** O modo usado no projeto (*baseline*) **só observa** as respostas: não envia
ataques. Por isso ele **não** acha injeção de SQL nem IDOR; mas acha muito bem o que **aparece** nas
respostas (cabeçalhos, *stack traces*).

**As regras do projeto:** `security/zap/rules.tsv`. Cada linha: número da regra, ação, comentário.
`WARN` = avisa; `FAIL` = conta como falha; `IGNORE` = não mostra. Exemplo:

```
10038	FAIL	Content Security Policy ausente (V-12)
90022	WARN	Application Error disclosure (V-11 stack trace)
```

**No pipeline:** job **Gate - DAST autenticado (OWASP ZAP)**. Ele liga o VidaPlus dentro do runner,
faz login como a Dra. House (para examinar as páginas que exigem login) e roda o baseline contra a
API. O resultado sai **no log** do job (procure por `WARN-NEW` e `FAIL-NEW`) e num **relatório**
nos artefatos da execução ([`06` §5.5](06-pipeline-do-zero.md#55-baixar-um-artefato-ex-o-sbom-o-relatório-do-zap)).

**Como rodar no seu computador (opcional, no Ubuntu do laboratório).** Com o VidaPlus ligado, este
comando examina o **site** (porta 4200) e grava a saída como evidência. A primeira vez baixa a
imagem do ZAP (grande — alguns minutos); o exame leva uns 2 minutos:

```bash
mkdir -p entregaveis/evidencias
docker run --rm --network host ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py -t http://localhost:4200 -I | tee entregaveis/evidencias/zap-site.txt
```

Esperado: muitas linhas `PASS: ...`, algumas `WARN-NEW: ...` e, no fim, a linha de resumo
`FAIL-NEW: 0  ...  WARN-NEW: 8  ...  PASS: 59` (os números podem variar um pouco).

> **No Windows (Docker Desktop)**, o `--network host` pode não funcionar. Use
> `docker run --rm ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://host.docker.internal:4200 -I`
> (sem o `--network host`; `host.docker.internal` é o nome do seu computador visto de dentro do
> container).

| Parte | Significado |
|---|---|
| `--network host` | o container usa a rede do seu computador, e assim enxerga o `localhost:4200` |
| `ghcr.io/zaproxy/zaproxy:stable` | a imagem com o ZAP |
| `zap-baseline.py -t http://localhost:4200` | o exame *baseline* (passivo) contra o endereço do site |
| `-I` | não terminar com "erro" se encontrar avisos (só queremos ler) |

(Aqui o `rules.tsv` do projeto não é usado, então todos os achados aparecem como `WARN`.)

**Como ler a saída.** Trecho real, do VidaPlus original:

```
WARN-NEW: Content Security Policy (CSP) Header Not Set [10038] x 3
	http://localhost:4200 (200 OK)
	http://localhost:4200/robots.txt (200 OK)
	http://localhost:4200/sitemap.xml (200 OK)
...
FAIL-NEW: 0	FAIL-INPROG: 0	WARN-NEW: 8	WARN-INPROG: 0	INFO: 0	IGNORE: 0	PASS: 59
```

- `WARN-NEW` / `FAIL-NEW`: a ação (do `rules.tsv`, no pipeline) e "novo nesta execução";
- o **nome** do problema e, entre colchetes, o **número da regra** (o mesmo do `rules.tsv`);
- `x 3`: em quantas respostas ele apareceu; abaixo, exemplos de endereços;
- a última linha é o **resumo**: quantos avisos, falhas e verificações aprovadas (`PASS`).

Repare num detalhe curioso: `robots.txt` e `sitemap.xml` não existem no VidaPlus, mas respondem
`200 OK` — o servidor do site devolve a página inicial para **qualquer** endereço desconhecido.

**O que não vê:** ataques (injeção, IDOR, RBAC) — ele não tenta; páginas que ele não visita.

---

## 6. Dependency-Check e Trivy — falhas conhecidas nas peças de terceiros

**O que são:** SCA. O VidaPlus usa dezenas de **bibliotecas** prontas (o Spring, o driver do MySQL,
a biblioteca do token…). Quando alguém descobre uma falha numa biblioteca, ela ganha um número
público, a **CVE** (ex.: `CVE-2021-44228`). O Dependency-Check e o Trivy comparam as bibliotecas do
projeto com essas listas.

**No pipeline:** job **Gate - SCA (Dependency-Check + Trivy)** — o Trivy imprime uma **tabela** no
log: biblioteca | CVE | gravidade | versão instalada | versão corrigida. O job
**Gate - Container Scanning (Trivy image)** faz o mesmo com as **imagens** Docker (sistema
operacional e programas dentro delas, como o nginx).

**Como ler:** procure no log (Search logs) por `CRITICAL` e `HIGH`. Se aparecer uma CVE, ela entra
na tabela de priorização da Fase 3 com **CVSS + EPSS + KEV**.

**O que não vê:** falhas no código **de vocês** (só nas peças de terceiros).

---

## 7. Syft e o SBOM — a lista de ingredientes

**O que é:** o Syft gera o **SBOM** (*Software Bill of Materials*): um arquivo com **todos** os
componentes do software, com nome e versão. Ele não procura falhas: ele **inventaria**. A utilidade
aparece quando sai uma CVE nova: com o SBOM, a equipe responde em minutos "nós usamos isso? em que
versão?".

**No pipeline:** job **SBOM (CycloneDX)**, que deixa o artefato `sbom`. Para baixar e abrir:
[`06` §5.5](06-pipeline-do-zero.md#55-baixar-um-artefato-ex-o-sbom-o-relatório-do-zap).

**Como ler:** o arquivo é JSON. Cada componente tem `"name"` e `"version"`. Para listar só nomes e
versões de quem tem "jjwt" no nome (troque `jjwt` pelo que procura):

```bash
grep -o '"name":"[^"]*jjwt[^"]*","version":"[^"]*"' ~/Downloads/artefato-sbom/sbom.cdx.json | sort -u
```

Esperado: três linhas, `"name":"jjwt-api","version":"0.12.6"`, `"name":"jjwt-impl",...` e
`"name":"jjwt-jackson",...` — as três partes da biblioteca do token, na versão que o projeto usa.

---

## 8. cosign — assinatura da imagem

**O que é:** a ferramenta que **assina** a imagem do backend e depois **verifica** a assinatura
(veja [`06` §6.2](06-pipeline-do-zero.md#62-assinatura-provar-quem-fez)). Você não roda o cosign:
você **lê** o que ele fez nos jobs `Release - build, push, sign & attest SBOM` e
`Deploy prod - verifica assinatura + aprovacao`.

**Como ler:** no log do Deploy prod, abra a etapa **Verificar assinatura da imagem**. Uma
verificação aprovada lista as conferências feitas, como:

```
The following checks were performed on each of these signatures:
  - The cosign claims were validated
  - Existence of the claims in the transparency log was verified offline
  - The code-signing certificate was verified using trusted certificate authority certificates
```

Em palavras: a assinatura é válida; ela está registrada no livro-razão público; e o certificado de
quem assinou é legítimo. Logo abaixo aparece um JSON com a **identidade** de quem assinou (o
endereço do workflow do repositório de vocês). Se a verificação falhasse, o job pararia ali e nada
seria publicado.

---

## 9. Checklist

- [ ] Sei dizer o que é SAST, DAST, SCA, varredura de segredos e SBOM, com um exemplo de cada.
- [ ] Rodei o Semgrep e o Gitleaks no meu computador e sei ler cada campo da saída.
- [ ] Sei explicar por que **nenhum** scanner acha o IDOR e o RBAC.
- [ ] Sei onde, no pipeline, ver o resultado de cada ferramenta.
