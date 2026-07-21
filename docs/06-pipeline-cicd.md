# 06 · Pipeline CI/CD — gates de segurança do SSDLC

O arquivo [`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml)
implementa o pipeline. A ideia pedagógica: **tornar a segurança visível e versionada**. O
pipeline nasce com quase todos os gates **vermelhos** (o legado é inseguro) e a **prova de
conceito** do grupo faz o(s) gate(s) da classe corrigida transitar **🔴 → 🟢**.

> ⚠️ **O pipeline é gerador de EVIDÊNCIA e FEEDBACK — não é a nota.** Como o foco é o
> processo (SSDLC + LGPD + privacidade), a avaliação vem dos **artefatos** e da **defesa**
> (ver [08-rubrica-avaliacao.md](08-rubrica-avaliacao.md)). Um gate verde **sustenta** a
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

## 2. Informativo → bloqueante

No legado, os gates usam `continue-on-error: true` (**informativo**): reportam vermelho
mas não travam o merge, senão nada avança. Conforme um grupo corrige uma classe de falha,
ele **remove o `continue-on-error`** daquele gate — que passa a ser **bloqueante**:
a partir dali, qualquer regressão volta a barrar o merge.

```yaml
secret-scan:
  name: "Gate - Secret Scanning (Gitleaks)"
  runs-on: ubuntu-latest
  continue-on-error: true      # <-- legado (informativo). REMOVA só esta linha => gate bloqueante
  steps:
    - uses: actions/checkout@v4
    # ... (o resto do job permanece inalterado)
```

> Remova **apenas** a linha `continue-on-error: true`; o job (`name`/`runs-on`/`steps`)
> **permanece**. A meta é tornar o gate **bloqueante**, não excluí-lo — apagar o bloco
> inteiro removeria o próprio gate (o Gitleaks deixaria de rodar).

Essa transição é, ela própria, um **entregável avaliável** (Fase 6): "quais gates o grupo
tornou bloqueantes e mantém verdes?".

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção + teste de segurança.
3. Abre PR; o pipeline roda todos os gates.
4. **Revisão por pares intra-grupo** (regra dos "4 olhos"): **outro membro do grupo — não
   o autor do PR** — revisa com o [checklist de secure code review](08-rubrica-avaliacao.md#L55)
   e **aprova**.
5. Merge só com o gate da classe corrigida **verde** **e** a revisão **aprovada**.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

> **Quem revisa:** a revisão é **intra-grupo** e **bloqueante** — autor ≠ revisor. Isso
> mantém os grupos independentes (nenhum PR espera por outra equipe).
>
> **Torne "verde + aprovação" uma regra:** os passos 4–5 devem ser **impostos** por
> *branch protection* na `main` — exigir os **status checks verdes** dos gates das PoCs **e
> 1 aprovação** antes do merge. É a prática **PS.1 do NIST SSDF** (proteger o código-fonte
> contra adulteração/bypass). Passo a passo para o aluno configurar em
> [09-setup-grupos.md §5.2](09-setup-grupos.md).

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
- **Rodar** na `main` e **ler** os jobs `release-sign` / `deploy-staging` / `deploy-prod`.
- **Tornar bloqueantes** os gates de segurança das suas PoCs (Seção 2).
- **Explicar no dossiê (§8)**: o fluxo acima + a **evidência** (imagem assinada em
  **Packages**; `cosign verify`/`verify-attestation` passando; a **pausa de aprovação** do
  `production`).
- **Único passo de setup** (é política do repo, não ferramenta): criar o **Environment
  `production` com *required reviewer*** — ver [09-setup-grupos.md §5.3](09-setup-grupos.md).

> ⚠️ A verificação amarra a identidade ao **seu** repositório
> (`--certificate-identity-regexp "https://github.com/<owner>/<repo>/.*"`) — **nunca** `.*`,
> que aceitaria qualquer signatário e esvaziaria a proveniência.
