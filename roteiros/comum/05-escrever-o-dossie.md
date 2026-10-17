# 05 · Escrever o dossiê — Markdown, evidências e diagramas

**Objetivo:** saber escrever no `entregaveis/DOSSIE.md` (tabelas, imagens, código), guardar
evidências do jeito certo e desenhar o DFD.

---

## 1. O dossiê é um arquivo Markdown

**Markdown** é texto comum com algumas marcações que viram formatação. Abra
`entregaveis/DOSSIE.md` no VS Code e aperte **Ctrl+Shift+V** para ver a versão formatada ao lado.

| Você escreve | Aparece |
|---|---|
| `## Título` | um título de seção |
| `**negrito**` | **negrito** |
| `_itálico_` | _itálico_ |
| `` `codigo` `` | `codigo` (use para nomes de arquivo, endpoint, comando) |
| `- item` | um item de lista |
| `1. item` | lista numerada |
| `[texto](endereço)` | um link |
| `![descrição](img/arquivo.png)` | uma imagem |
| `> texto` | um bloco de destaque |

### 1.1 Tabelas

O dossiê é quase todo tabelas. Cada linha começa e termina com `|`, e as colunas são separadas
por `|`:

```markdown
| Ativo | Nível | Observação |
|---|:-:|---|
| Laudo (PDF) | Crítico | dado de saúde |
```

- A segunda linha (`|---|:-:|---|`) é obrigatória; `:-:` centraliza a coluna.
- **Não precisa alinhar** as barras com espaços: o resultado é o mesmo.
- Quer escrever uma barra `|` **dentro** de uma célula? Use `\|`.
- Uma célula não aceita "Enter". Para separar ideias numa célula, use `;` ou `<br>`.

### 1.2 Blocos de código e comandos

Para mostrar um comando ou um trecho de saída, use três crases antes e depois:

````markdown
```bash
curl -s -i "localhost:8080/api/pacientes/busca?q=Silva"
```
````

### 1.3 As linhas em itálico do modelo

O `DOSSIE.md` vem com linhas de exemplo em _itálico_. **Apague-as** e escreva as do seu grupo.
Não deixe exemplo misturado com conteúdo de vocês.

---

## 2. Evidências: onde guardar e como nomear

Uma **evidência** é a prova de que algo aconteceu: a saída de um comando, um print, o link de uma
execução do pipeline. **Toda afirmação importante do dossiê aponta para uma evidência.**

1. Guarde tudo em **`entregaveis/evidencias/`** (crie a pasta se não existir).
2. Nomeie com **fase + assunto**, sem espaços nem acentos:
   - `fase0-busca-sem-login.txt`
   - `fase4-idor-antes.txt`, `fase4-idor-depois.txt`
   - `fase5-ci-cenario-verde.png`
3. Prefira **texto** (`.txt`, com `| tee`) a print: texto dá para procurar e comparar. Use print
   para tela do navegador e do GitHub.
4. No dossiê, cite a evidência com um link:
   `[saída do curl](evidencias/fase0-busca-sem-login.txt)`.

> ⚠️ **Não use a extensão `.log`**: o repositório ignora arquivos `.log` (estão no
> `.gitignore`), e eles não seriam enviados ao GitHub. Use `.txt`.

> ⚠️ **Antes de anexar um print, confira o que aparece nele.** Nada de token do GitHub, abas
> pessoais do navegador ou notificações com dados seus. Os dados do VidaPlus são fictícios e podem
> aparecer.

### 2.1 Um bom registro de evidência responde a quatro perguntas

| Pergunta | Exemplo |
|---|---|
| **O que** foi feito? | pedido `GET /api/exames/102` com o token da Ana |
| **Em que estado** estava o sistema? | banco zerado, código original (commit `a1b2c3d`) |
| **O que** se esperava? | 403 |
| **O que** aconteceu? | 200, com o exame do Bruno |

Para saber o commit atual: `git log --oneline -1`.

---

## 3. Desenhar o DFD (Fase 2a)

Use o **draw.io** (gratuito, sem conta): abra **`app.diagrams.net`** no navegador → **Create New
Diagram** → **Blank Diagram** → escolha **Device** como local de gravação.

### 3.1 As formas do DFD

| Elemento | Forma | Onde achar no draw.io (painel esquerdo) |
|---|---|---|
| **Entidade externa** (quem usa: paciente, médico…) | retângulo | *General* → retângulo |
| **Processo** (o que o sistema faz: "Autenticar") | círculo | *General* → elipse (segure **Shift** ao redimensionar para ficar redonda) |
| **Depósito de dados** (tabela, arquivo, `localStorage`) | duas linhas paralelas, com o nome entre elas | *General* → retângulo; em **Format → Style**, deixe só a borda de cima e a de baixo (ou desenhe duas linhas) |
| **Fluxo de dados** (o que passa: "e-mail e senha") | seta com rótulo | passe o mouse sobre uma forma e arraste a setinha azul até a outra; clique duas vezes na seta para escrever o rótulo |
| **Fronteira de confiança** (onde o nível de confiança muda) | linha tracejada vermelha | uma linha; em **Format**, escolha **Pattern: dashed** e a cor vermelha |

Dicas:

- **Numere os processos** (1.0, 2.0…) e os depósitos (D1, D2…): as tabelas STRIDE e LINDDUN vão
  citar esses números.
- **Cada seta tem um nome** que diz **o dado** que passa ("id do paciente", "laudo em PDF"), não a
  ação ("clicar").
- Veja o DFD do exemplo: [`docs/img/dfd-exemplo-pacote5.svg`](../../docs/img/dfd-exemplo-pacote5.svg).

### 3.2 Gravar e colocar no dossiê

1. **File → Save as** → grave como `dfd.drawio` (o arquivo editável) em `entregaveis/img/`.
2. **File → Export as → SVG** (ou PNG) → grave como `dfd.svg` em `entregaveis/img/`.
3. No dossiê, na §3:

   ```markdown
   ![DFD do nosso recorte](img/dfd.svg)
   ```

4. Grave os dois arquivos no Git (o `.drawio` permite que outro colega edite depois).

---

## 4. Escrever bem — o que diferencia um dossiê bom

| Fraco | Bom |
|---|---|
| "O sistema tem falha de acesso." | "Logada como Ana (paciente 1), `GET /api/exames/102` devolve 200 com o exame de glicemia do Bruno (paciente 2)." |
| "Risco alto." | "Risco Crítico: probabilidade Alta (basta trocar um número na URL) × impacto Alto (dado de saúde, art. 11)." |
| "Implementar segurança no login." | "O sistema deve recusar a autenticação quando ... sem emitir credencial de sessão." |
| "Corrigido." | "Corrigido no PR #5; o cenário X passou de 🔴 para 🟢 (log em `evidencias/fase5-...txt`)." |

Regras práticas:

1. **Concreto:** nome do endpoint, do usuário, do arquivo, da tabela.
2. **Verificado:** só escreva o que você viu acontecer. "Parece que" não entra.
3. **Ligado:** cada ameaça aponta para um requisito; cada requisito, para um teste; cada teste,
   para uma evidência. É o "fio" que a defesa vai cobrar.
4. **Honesto:** o que ficou faltando vai para o plano de remediação, com prioridade. Um dossiê que
   diz "está tudo resolvido" numa prova de conceito é um dossiê que não olhou direito.
