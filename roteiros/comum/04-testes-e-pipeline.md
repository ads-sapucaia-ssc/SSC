# 04 · Testes e pipeline — rodar, ler e guardar como evidência

**Objetivo:** entender o que é um teste de segurança do VidaPlus, rodá-lo no seu computador, achar
o resultado dele no pipeline do GitHub e usar os scanners (Semgrep, Gitleaks) como evidência.

**Usado nas Fases:** 1 (escrever `.feature`), 4 (corrigir o P0), 5 (verificar) e 6 (pipeline).

---

## 1. O que é um teste de segurança aqui

Um **teste automático** é um pequeno programa que usa o sistema e confere o resultado. Se o
resultado for o esperado, o teste **passa** (🟢 verde); senão, **falha** (🔴 vermelho).

No VidaPlus, os testes de segurança são escritos em **Gherkin**: frases em português, num arquivo
`.feature`. Abra `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature`:

```gherkin
# language: pt
@F-02 @REQ-AC-IDOR-01 @P0 @lgpd-art11 @lgpd-art46 @asvs-V4 @stride-EoP @linddun-divulgacao @security
Funcionalidade: F-02 · Acesso a laudo (REQ-AC-IDOR-01)
  ...
  Cenário: paciente não acessa o laudo de outro paciente
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    Quando faço GET em "/api/exames/102"
    Então o status HTTP é 403
    E a resposta não contém "Glicemia"
```

Leia em voz alta: "Dado que estou logado como Ana, quando peço o exame 102 (do Bruno), então a
resposta tem que ser 403 e não pode mostrar o tipo do exame". É **o requisito escrito de um jeito
que o computador consegue conferir**.

### 1.1 As etiquetas (tags) da segunda linha

| Tag | Significado |
|---|---|
| `@F-02` | a funcionalidade do catálogo que este teste prova |
| `@REQ-AC-IDOR-01` | o requisito do dossiê (§2) que este teste prova |
| `@P0` | a prioridade |
| `@lgpd-art11`, `@asvs-V4`, `@stride-EoP`… | de onde vem o requisito e que ameaça ele combate |
| **`@security`** | requisito **implementado**: o teste **roda** no pipeline |
| **`@analise`** | requisito **só analisado**: o teste **não roda** (é uma especificação) |

### 1.2 Frases que o computador já sabe executar

Os passos abaixo já estão programados (no arquivo `PassosSeguranca.java`). Num `.feature`
`@security`, use **só estas frases**, trocando o que está entre aspas:

| Frase | O que faz |
|---|---|
| `Dado que estou autenticado como "<email>" com senha "<senha>"` | faz login e guarda o token |
| `Quando faço GET em "<endereço>"` | pede o endereço, com o token |
| `Quando faço POST em "<endereço>"` | envia um POST ao endereço, com o token |
| `Quando tento logar com email "<email>" e senha "<senha>"` | tenta o login (sem guardar token) |
| `Então o status HTTP é <número>` | confere o código de status |
| `E a resposta não contém "<texto>"` | confere que o texto **não** aparece |
| `E a resposta contém "<texto>"` | confere que o texto aparece |

Num `.feature` **`@analise`**, você pode escrever frases novas à vontade (ex.:
`Dado que o paciente revogou o consentimento`): como ele não roda, ninguém precisa programar a
frase. Ela documenta o comportamento esperado.

---

## 2. Rodar os testes no seu computador

O repositório traz o script [`rodar-testes.sh`](rodar-testes.sh). Ele roda os testes dentro de um
container, sem precisar instalar Java nem Maven, usando o banco do VidaPlus que está ligado.

1. Religue o VidaPlus com o banco original (alguns testes dependem dos dados originais):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   ```

2. Rode os testes:

   ```bash
   bash roteiros/comum/rodar-testes.sh "security & F-02"
   ```

   O texto entre aspas é o **filtro**: "testes `@security` **e** da `F-02`". Troque pela
   funcionalidade do seu P0:

   | Grupo | Filtro |
   |---|---|
   | G1 | `"security & F-01"` |
   | G2 | `"security & F-02"` |
   | G3 | `"security & F-03"` |
   | G4 | `"security & F-02"` |

   Sem filtro (`bash roteiros/comum/rodar-testes.sh`), roda os testes `@security` de **todos** os
   pacotes.

3. **A primeira vez demora** (5–10 minutos): o Maven baixa as bibliotecas. As próximas levam menos
   de um minuto.

**O que o script faz, por dentro** (abra `roteiros/comum/rodar-testes.sh` para ler):

- Os testes do VidaPlus são programas Java. Quem compila e roda programas Java aqui é o **Maven**
  (comando `mvn`). Em vez de instalar Java e Maven no seu computador, o script usa uma **imagem
  Docker** que já tem os dois (`maven:3.9-eclipse-temurin-21`) — a mesma ideia da "marmita" de
  [`01` §2](01-subir-o-vidaplus.md#2-docker-do-zero-em-5-minutos).
- Ele liga esse container **na mesma rede** do VidaPlus (`--network legacy-app_default`), para que
  os testes enxerguem o banco do VidaPlus, e mostra a pasta do código para dentro do container
  (`-v ...:/app`).
- Lá dentro, roda `mvn test -Dgroups="<filtro>"`: "compile e rode só os testes com estas
  etiquetas". É **o mesmo comando** que o pipeline roda no GitHub
  ([`06` §3.3](06-pipeline-do-zero.md#33-um-job-linha-por-linha)).
- As bibliotecas baixadas ficam guardadas num volume (`vidaplus-m2`), por isso as próximas vezes
  são rápidas.

### 2.1 Lendo o resultado

**Se falhou** (o esperado no código original), aparece algo assim:

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0 ... RunCucumberTest
org.opentest4j.AssertionFailedError: expected: <403> but was: <200>
RESULTADO: algum teste FALHOU (vermelho). ...
```

- `Tests run: 1, Failures: 1` — rodou 1 teste e 1 falhou;
- `expected: <403> but was: <200>` — **a frase mais importante**: o teste esperava **403** (acesso
  negado), mas o sistema respondeu **200** (entregou o exame). É a falha, provada.

**Se passou**, o Maven não escreve quase nada (ele roda em modo silencioso) e o script termina com:

```
RESULTADO: todos os testes do filtro "security & F-02" PASSARAM (verde).
```

> **Guarde as duas saídas** — a vermelha (antes da correção) e a verde (depois) — como evidência:
> `bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase4-teste-antes.txt`.
> O `2>&1` junta as mensagens de erro na mesma saída, para o `tee` gravar tudo.

---

## 3. Os testes no pipeline do GitHub

> Nunca viu um pipeline? Leia antes [`06-pipeline-do-zero.md`](06-pipeline-do-zero.md): ele explica
> o que é, como o arquivo funciona e **cada tela** do GitHub Actions.

A cada `push`, o GitHub roda o **pipeline** (`.github/workflows/ssdlc-pipeline.yml`): uma lista de
verificações automáticas. Cada verificação é um **job**; os jobs de segurança se chamam **gates**.

### 3.1 Ver uma execução

1. No repositório do grupo no GitHub, clique na aba **Actions**.
2. Clique na execução mais recente (ou na do seu PR). À esquerda, a lista de jobs:
   ✓ verde = passou; ✗ vermelho = falhou.
3. Role a página principal da execução até o **"Placar de gates SSDLC"**: um resumo de todos os
   gates.

> **Vermelho é o normal no começo.** O VidaPlus é inseguro de propósito. Os gates são
> **informativos**: mostram o problema, mas não impedem nada. A execução termina "concluída" mesmo
> com gates vermelhos.

### 3.2 Achar o SEU teste no log

O job **"Gate - Testes de seguranca"** roda os testes `@security` de **todos** os pacotes. Ele
continua vermelho por causa dos testes dos outros grupos, mesmo depois que vocês corrigirem o P0.
Por isso, a sua evidência é **o seu cenário** no log:

1. Clique no job **Gate - Testes de seguranca**.
2. Clique na etapa **Testes de aceite de seguranca (@Tag security)** para abri-la.
3. No canto superior direito, em **Search logs**, procure o nome do seu cenário (por exemplo,
   `paciente não acessa o laudo`).
4. Leia:
   - **antes da correção:** o seu cenário aparece numa linha com `FAILURE!`, seguida de
     `expected: <...> but was: <...>`;
   - **depois da correção:** o seu cenário **não aparece mais** na lista de falhas, e a linha
     final `Tests run: X, Failures: Y` mostra **uma falha a menos** (ou duas, contando o teste
     JUnit equivalente do `SegurancaAceiteTest`).
5. Tire um **print** da tela do log (com a URL do navegador aparecendo) ou copie o link da
   execução: o link já identifica o commit testado.

---

## 4. Scanners no seu computador

> O que é cada ferramenta e como ler **cada campo** da saída: [`07-ferramentas-de-seguranca.md`](07-ferramentas-de-seguranca.md).

Os gates de SAST e de segredos também podem rodar no seu computador, com Docker. Rode na **raiz
do repositório**.

### 4.1 Semgrep (procura padrões inseguros no código)

```bash
docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
  semgrep scan --config security/semgrep/vidaplus.yml --metrics=off legacy-app
```

- `-v "$PWD":/src` mostra a pasta atual para o container;
- `--config security/semgrep/vidaplus.yml` usa as **regras próprias** do projeto (as que procuram
  as falhas plantadas no VidaPlus).

No fim aparece `Ran 5 rules on ... files: N findings.` Para cada achado, o Semgrep mostra o
arquivo, a linha e o nome da regra (`vidaplus-sql-concat`, `vidaplus-weak-hash-md5`…).

> O pipeline roda também os conjuntos públicos `p/owasp-top-ten` e `p/java`, então o total do
> pipeline é maior. Para comparar antes e depois da sua correção, use **o mesmo comando** nas duas
> vezes.

### 4.2 Gitleaks (procura senhas e chaves gravadas)

```bash
docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
  detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
  --log-opts="--all -- legacy-app" --no-banner --redact -v
```

O `-v` mostra cada achado: arquivo, linha, regra e o **commit** onde o segredo apareceu. O
`--log-opts="--all -- legacy-app"` limita a busca à pasta do **sistema** (`docs/` e `roteiros/`
são material da disciplina), e o `--redact` troca o valor do segredo por `REDACTED`, para que ele
não apareça na tela nem nas evidências. Hoje são **8 achados** (`leaks found: 8`).

> Repare: o Gitleaks varre **o histórico inteiro** do Git, não só os arquivos de hoje. Ele pode
> acusar um arquivo que **já foi apagado**. É a prova de que apagar o segredo do arquivo não basta
> — ele continua no histórico, e por isso o valor precisa ser **trocado**.

---

## 5. Checklist

- [ ] Sei ler um `.feature` e sei a diferença entre `@security` e `@analise`.
- [ ] Rodei `rodar-testes.sh` com o filtro do meu grupo e li o `expected ... but was ...`.
- [ ] Achei, no log do GitHub Actions, o cenário do meu P0.
- [ ] Rodei o Semgrep e o Gitleaks no meu computador.
