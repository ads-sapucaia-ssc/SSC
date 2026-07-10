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
| Build frontend | Angular CLI | 4 | compila |
| **Secret Scanning** | Gitleaks | 4/6 | V-05 (segredos no código) |
| **SAST** | Semgrep + CodeQL | 4/5 | V-01, V-02, V-07, V-10 |
| **SCA** | Dependency-Check + Trivy | 5/6 | V-18 (dependências com CVE) |
| **SBOM** | Syft (CycloneDX) | 6 | inventário de componentes |
| **DAST** | OWASP ZAP baseline | 5 | V-11, V-12, V-01, V-10 (dinâmico) |
| **Testes de segurança** | JUnit (`-Dgroups=security`) | 5 | V-03 IDOR, V-04 RBAC, auth, V-06 auditoria |
| Placar | summary job | 8 | consolida o estado dos gates |

> IDOR (V-03) e RBAC (V-04) são falhas **lógicas** — não aparecem em scanner genérico.
> São cobertas por **testes de segurança** escritos pelos grupos (ex.: paciente A pede
> `GET /api/exames/{id_de_B}` e o teste exige **403 sem metadados**).

---

## 2. Informativo → bloqueante

No legado, os gates usam `continue-on-error: true` (**informativo**): reportam vermelho
mas não travam o merge, senão nada avança. Conforme um grupo corrige uma classe de falha,
ele **remove o `continue-on-error`** daquele gate — que passa a ser **bloqueante**:
a partir dali, qualquer regressão volta a barrar o merge.

```yaml
# antes (legado, informativo):
secret-scan:
  continue-on-error: true
# depois (corrigido, bloqueante): remova a linha acima
```

Essa transição é, ela própria, um **entregável avaliável** (Fase 6): "quais gates o grupo
tornou bloqueantes e mantém verdes?".

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção + teste de segurança.
3. Abre PR; o pipeline roda todos os gates.
4. Revisão por pares (checklist de secure code review).
5. Merge só com o gate da classe corrigida **verde**.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

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

## 5. Extensões sugeridas (bônus)

- **Container scanning** (Trivy image) do backend/frontend.
- **IaC scanning** (Checkov) se houver Terraform/K8s.
- **DAST autenticado** no ZAP (com token) para exercitar IDOR de forma automatizada.
- **Branch protection** exigindo os gates verdes para merge na `main`.

## 6. Atividade opcional (bônus) — supply chain: assinatura e proveniência

Fecha a Parte 9 do conteúdo (Deploy + Supply Chain). O grupo demonstra que sabe **provar a
origem** de um artefato, não só produzi-lo.

**SLSA** (*Supply-chain Levels for Software Artifacts*, OpenSSF) — 4 níveis:
`L0` sem proveniência · `L1` proveniência básica · `L2` proveniência **assinada** ·
`L3` build endurecido/auditado. Meta acadêmica realista: **chegar a L2**.

**Sigstore / cosign** — assinatura por **identidade OIDC** (sem gerenciar chaves de longa
duração). Passos:

```yaml
# job opcional no pipeline (após build da imagem do backend)
- uses: sigstore/cosign-installer@v3
- name: Assinar imagem (keyless, via OIDC do Actions)
  env: { COSIGN_EXPERIMENTAL: "1" }
  run: cosign sign --yes ghcr.io/<grupo>/vidaplus-backend:${{ github.sha }}
- name: Verificar assinatura
  run: cosign verify ghcr.io/<grupo>/vidaplus-backend:${{ github.sha }} \
        --certificate-identity-regexp '.*' --certificate-oidc-issuer-regexp '.*'
```

**Entregável:** imagem assinada + verificação passando + o **SBOM** (já gerado no pipeline)
anexado/atestado. Isso equivale, na prática, a **SLSA L2** para o artefato do grupo.
