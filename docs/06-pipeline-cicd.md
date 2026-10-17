# 06 · Pipeline CI/CD — gates de segurança do SSDLC

O arquivo [`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml)
implementa o pipeline. A ideia pedagógica: **tornar a segurança visível e versionada**. O
pipeline nasce com quase todos os gates **vermelhos** (o legado é inseguro) e a **prova de
conceito** do grupo faz o(s) gate(s) da classe corrigida transitar **🔴 → 🟢**.

> ⚠️ **O pipeline é gerador de EVIDÊNCIA e FEEDBACK — não é a nota.** Como o foco é o
> processo (SSDLC + LGPD + privacidade), a avaliação vem dos **artefatos** e da **defesa**
> (ver [07-rubrica-avaliacao.md](07-rubrica-avaliacao.md)). Um gate verde **sustenta** a
> evidência de um controle; ele não substitui o requisito, o threat model, o RIPD e a
> rastreabilidade por trás. Nem todos os gates precisam ficar verdes — só os das PoCs
> escolhidas; os demais sustentam o **plano de remediação**.

---

## 1. Gates e o que cada um verifica

| Gate | Ferramenta | Fase SSDLC | Falhas que acusa |
|------|-----------|:----------:|------------------|
| Build backend | Maven | 4 | compila/roda + testes |
| Build frontend | React (Vite) | 4 | compila |
| **Secret Scanning** | Gitleaks | 4/6 | V-05 (segredos no código) |
| **SAST** | Semgrep + CodeQL | 4/5 | V-01, V-02, V-07, V-10 |
| **SCA** | Dependency-Check + Trivy (fs) | 5/6 | higiene de supply chain (CVE em dependências) |
| **Container Scanning** | Trivy (image) | 5/6 | CVE na imagem base/SO do backend/frontend |
| **SBOM** | Syft (CycloneDX) | 6 | inventário de componentes |
| **DAST autenticado** | OWASP ZAP (com JWT) | 5 | V-03 IDOR, V-11, V-12, V-10 (dinâmico, autenticado) |
| **Testes de segurança** | JUnit (`-Dgroups=security`) | 5 | V-03 IDOR, V-04 RBAC, V-01 SQLi no login |
| Placar | summary job | 8 | consolida o estado dos gates |

> IDOR (V-03) e RBAC (V-04) são falhas **lógicas** — não aparecem em scanner genérico.
> São cobertas por **testes de segurança** escritos pelos grupos (ex.: paciente A pede
> `GET /api/exames/{id_de_B}` e o teste exige **403 sem metadados**).

> **Por que não há IaC scanning (Checkov):** aplique o controle **proporcional ao contexto** —
> o VidaPlus roda **localmente** via Docker Compose, sem Terraform/Kubernetes a analisar.

---

## 1.1 Onde cada ferramenta vive — a pasta `security/`

A pasta [`security/`](../security/) **não guarda as ferramentas** do pipeline — guarda
apenas os **arquivos de configuração customizada** das ferramentas que precisam de ajuste
específico do projeto. As ferramentas em si **não ficam versionadas no repositório**: o
GitHub Actions **baixa cada uma em tempo de execução** (via `uses:` de uma *action* pronta,
ou `container:`/`run:` com uma imagem Docker).

Por isso `security/` tem só **4 arquivos** — são exatamente as 4 ferramentas que exigem
config própria. As demais rodam com **configuração padrão** (nada a versionar) ou têm a sua
config **junto do código** (`pom.xml`, `package.json`).

| Ferramenta | Como o pipeline a obtém | Config no repositório? |
|-----------|--------------------------|------------------------|
| **Gitleaks** | `gitleaks/gitleaks-action@v2` | ✅ [security/gitleaks/gitleaks.toml](../security/gitleaks/gitleaks.toml) |
| **Semgrep** | `container: returntocorp/semgrep` + conjuntos públicos `p/owasp-top-ten`, `p/java` | ✅ [security/semgrep/vidaplus.yml](../security/semgrep/vidaplus.yml) (regras próprias) |
| **Dependency-Check** | `dependency-check/Dependency-Check_Action@main` | ✅ [security/dependency-check/suppression.xml](../security/dependency-check/suppression.xml) |
| **OWASP ZAP** | `zaproxy/action-baseline@v0.12.0` | ✅ [security/zap/rules.tsv](../security/zap/rules.tsv) |
| **CodeQL** | `github/codeql-action/{init,analyze}@v3` | ❌ usa as *queries* padrão |
| **Trivy** (fs + imagem) | `aquasecurity/trivy-action@master` | ❌ config padrão |
| **Syft** (SBOM) | `anchore/sbom-action@v0` | ❌ config padrão |
| **cosign / Sigstore** | `sigstore/cosign-installer@v3` | ❌ *keyless* via OIDC (sem segredo) |
| **Maven / JUnit** | `actions/setup-java@v4` + `mvn` | ✅ em [legacy-app/backend/pom.xml](../legacy-app/backend/pom.xml) |
| **React / Vite** | `actions/setup-node@v4` + build | ✅ em [legacy-app/frontend/](../legacy-app/frontend/) |

> A quantidade de arquivos em `security/` **não** deveria bater com a quantidade de
> ferramentas do pipeline: ela reflete só **quantas precisam de configuração customizada**.
> Maven/JUnit e React/Vite são ferramentas de **build**, não de segurança — por isso a
> config delas vive junto do código, não em `security/`.

---

## 2. Informativo × bloqueante

Os gates usam `continue-on-error: true` (**informativo**): reportam vermelho, mas não travam o
merge — senão nada avançaria, porque o repositório de cada grupo ainda tem as falhas dos outros
pacotes. Um gate **bloqueante** seria o contrário: sem essa linha, e exigido na proteção da
`main`, qualquer regressão barraria o merge.

```yaml
secret-scan:
  name: "Gate - Secret Scanning (Gitleaks)"
  runs-on: ubuntu-latest
  continue-on-error: true      # <-- informativo: reporta, mas não trava o merge
  steps:
    - uses: actions/checkout@v4
    # ...
```

> **Neste projeto, os gates ficam informativos** e o grupo **não** precisa torná-los
> bloqueantes. O que se espera é **entender** a diferença e **explicar** no dossiê (§8) por que,
> num projeto real, o gate da classe corrigida deveria virar bloqueante. *(Material opcional
> para quem quiser fazer na prática: [docs/13](13-gate-bloqueante-do-grupo.md).)*

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção + teste de segurança.
3. Abre PR; o pipeline roda todos os gates.
4. **Revisão por pares intra-grupo** (regra dos "4 olhos"): **outro membro do grupo — não
   o autor do PR** — revisa com o [checklist de secure code review](07-rubrica-avaliacao.md#L113)
   e **aprova**.
5. Merge só com o gate da classe corrigida **verde** **e** a revisão **aprovada**.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

> **Quem revisa:** a revisão é **intra-grupo** e **bloqueante** — autor ≠ revisor. Isso
> mantém os grupos independentes (nenhum PR espera por outra equipe).
>
> **Torne "verde + aprovação" uma regra:** os passos 4–5 devem ser **impostos** por
> *branch protection* na `main` — exigir os **status checks verdes** dos gates das PoCs **e
> 1 aprovação** antes do merge. É a prática **PS.1 do NIST SSDF** (proteger o código-fonte
> contra adulteração/bypass). Nesta turma, o **professor** configura essa proteção em cada
> repositório ([08-setup-grupos.md §5.2](08-setup-grupos.md)); o grupo explica no dossiê.

---

## 4. Evidências

Cada execução do pipeline gera evidências reutilizáveis na defesa (Fase 8):
- SARIF de SAST na aba **Security** do repositório;
- relatório HTML do ZAP (artifact);
- relatório do Dependency-Check;
- **SBOM** (`sbom.cdx.json`);
- log dos testes de segurança;
- **placar de gates** no summary de cada run.

---

## 5. Entrega segura (já pré-fiada): assinatura, verificação e promoção

Fecha a Parte 9 (Deploy + Supply Chain) e é **entregável da Fase 6**. **O pipeline já faz
tudo isto** — você **não configura ferramenta** (usa **OIDC + `GITHUB_TOKEN`**, sem
segredos). Seu trabalho é **entender, tornar bloqueante quando fizer sentido e explicar no
dossiê** com a evidência do run.

### O fluxo de CD (roda na `main`)

```
release-sign  →  deploy-staging  →  deploy-prod
```

1. **`release-sign`** — *build once*: builda e **publica** a imagem no **GHCR**, **assina**
   com **cosign** (keyless/OIDC, registrado no **Rekor**) e **atesta o SBOM** da imagem →
   **proveniência assinada = SLSA L2**.
2. **`deploy-staging`** — "sobe" a app num **staging** efêmero e faz um *smoke* (o **DAST
   autenticado** exercita essa instância).
3. **`deploy-prod`** — **admission gate**: `cosign verify` + `verify-attestation` **recusam
   imagem não assinada/atestada**, e só executa **após aprovação manual** do ambiente
   `production`. É o "*build once, deploy many*" **com portão**.

**Conceitos (para a defesa):** **SLSA** L0→L3 mede a confiança da proveniência (meta **L2**,
assinada); **Sigstore/cosign** assina por **identidade** (não por chave); e assinar só tem
valor porque **alguém verifica** — por isso o `deploy-prod` recusa o não-assinado.

### O que você faz (não é configurar ferramenta)
- **Ler** os jobs `release-sign` / `deploy-staging` / `deploy-prod` numa execução na `main`
  (aba **Actions**).
- **Explicar no dossiê (§8)**: o fluxo acima + a **evidência** (imagem assinada em
  **Packages**; `cosign verify`/`verify-attestation` passando; a **pausa de aprovação** do
  `production`).
- **Nenhuma configuração:** o Environment `production` com revisor e a proteção da `main` são
  feitos pelo professor ([08-setup-grupos.md §5.2 e §5.3](08-setup-grupos.md)).

> ⚠️ A verificação amarra a identidade ao **seu** repositório
> (`--certificate-identity-regexp "https://github.com/<owner>/<repo>/.*"`) — **nunca** `.*`,
> que aceitaria qualquer signatário e esvaziaria a proveniência.

### Por que a identidade na verificação é o que importa

O ponto que costuma confundir: **assinar não vale nada se, ao verificar, você não checar
quem assinou.** O `--certificate-identity-regexp` é justamente o filtro de "quem"; pôr
`.*` ali é o mesmo que não checar nada.

**Assinatura *keyless* (por identidade).** O `release-sign` não assina com uma chave
guardada em segredo — assina por **identidade** (OIDC). Quando o job roda, o Sigstore emite
um certificado de curta duração que carimba na assinatura **quem assinou** — a identidade do
próprio workflow, algo como
`https://github.com/<owner>/<repo>/.github/workflows/ssdlc-pipeline.yml@refs/heads/main`.
A assinatura não diz só "isto foi assinado"; diz "isto foi assinado **por este workflow,
neste repositório**".

**Qualquer um consegue uma assinatura válida.** Um atacante abre o repositório dele, roda um
workflow e o Sigstore emite um certificado perfeitamente válido com a identidade *dele*,
registrado no Rekor. Por isso a pergunta certa na verificação **não** é "existe uma
assinatura válida?" — é "existe uma assinatura válida feita pela **identidade em que eu
confio**?".

| Valor de `--certificate-identity-regexp` | O que o `cosign verify` aceita |
|------------------------------------------|--------------------------------|
| `https://github.com/<owner>/<repo>/.*`   | **só** assinaturas feitas por um workflow **deste** repositório |
| `.*`                                     | **qualquer** identidade — de qualquer repositório, de qualquer pessoa |

**O ataque que o `.*` abriria.** Suponha o `deploy-prod` verificando com `.*`:

1. O atacante builda uma imagem maliciosa (`vidaplus-backend` com backdoor).
2. Assina com o **próprio** GitHub Actions, no **próprio** repositório — assinatura Sigstore
   legítima, identidade `https://github.com/atacante/malware/...`.
3. Consegue empurrar essa imagem/tag para o seu GHCR (ou você a puxa por engano).
4. O `deploy-prod` roda `cosign verify` → com `.*`, a identidade do atacante **casa** com o
   filtro → **verificação passa** → a imagem com backdoor vai para produção.

Com o regexp preso ao seu repositório, o passo 4 **falha**:
`https://github.com/atacante/malware/...` não casa com
`https://github.com/<owner>/<repo>/.*`, o `cosign verify` retorna erro e o *admission gate*
**recusa** a imagem.

**A analogia.** É o segurança conferindo documento na porta:
- **assinatura válida** = "o passaporte é autêntico" — todo mundo tem um, não filtra ninguém;
- **`--certificate-identity-regexp <seu repo>`** = "o nome está na lista de convidados" — *aí* filtra;
- **`.*`** = "deixe entrar qualquer um com passaporte autêntico" — é jogar a lista fora.

Por isso `.*` **esvazia a proveniência**: proveniência é saber *de onde* o artefato veio;
aceitar qualquer signatário destrói exatamente essa garantia — a verificação continua
existindo, mas não prova mais nada.

> O `--certificate-oidc-issuer "https://token.actions.githubusercontent.com"` é o **segundo
> cadeado**: exige que a identidade tenha sido emitida pelo OIDC **do GitHub Actions**. Os
> dois juntos dizem: "assinado por um workflow do GitHub Actions **E** desse workflow ser do
> meu repositório".
