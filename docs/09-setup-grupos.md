# 09 · Setup dos grupos — passo a passo

Guia para cada grupo **começar** o projeto: gerar o repositório, subir o VidaPlus, rodar o
pipeline pela primeira vez e entender por que ele nasce **vermelho**.

---

## 1. Gerar o repositório do grupo

**Via GitHub Classroom (recomendado):** aceite o convite do assignment; o Classroom cria
automaticamente um repositório **privado** do grupo a partir do template, já com o
autograding ligado.

**Ou manualmente:** na página do template `SSC`, clique em **"Use this template" →
"Create a new repository"**, marque **Private** e dê um nome (ex.: `ssc-grupo-A`).

Depois, clone:

```bash
git clone git@github.com:<sua-conta-ou-org>/ssc-grupo-A.git
cd ssc-grupo-A
```

---

## 2. Subir o VidaPlus localmente

```bash
cd legacy-app
docker compose up --build
# Frontend: http://localhost:4200   API: http://localhost:8080   MySQL: localhost:3306
```

Explore as falhas (usuários de teste e comandos de exploração estão em
[../legacy-app/README.md](../legacy-app/README.md)). **Você deve conseguir**, por exemplo,
baixar o exame de outro paciente trocando o id (IDOR) — é isso que vão corrigir.

---

## 3. Rodar o pipeline pela primeira vez

Ao fazer o primeiro `push`, o **GitHub Actions** executa o
[`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml). Veja em
**Actions** e, ao final, o **"Placar de gates SSDLC"** no _summary_ da execução.

> ⚠️ **É esperado que quase tudo apareça vermelho.** O VidaPlus é legado e inseguro **de
> propósito**. O vermelho é o seu **ponto de partida**; a meta do semestre é transformar
> cada gate em verde, corrigindo as falhas de verdade.

### Como ler o placar

| Gate | O que um vermelho significa | Você conserta em… |
|------|-----------------------------|-------------------|
| Secret Scanning | Há segredo no código (V-05) | Fase 4/6 |
| SAST (Semgrep/CodeQL) | Padrões inseguros: SQLi, MD5, XSS, JWT (V-01/02/07/10) | Fase 4 |
| SCA | Dependências com CVE (V-18) | Fase 5/6 |
| DAST (ZAP) | Faltam headers, stack trace, SQLi/XSS dinâmico | Fase 5 |
| Testes de segurança | IDOR/RBAC/auditoria sem teste que prove correção | Fase 5 |

---

## 4. O aviso de "segredo detectado" no primeiro push

O repositório contém **segredos falsos plantados** (senha do banco, segredo do JWT — V-05)
para vocês aprenderem a removê-los. Por isso:

- o gate **Gitleaks** vai **acusar** — isso é **esperado e correto**;
- o **push protection** do GitHub **pode bloquear** o primeiro push ao detectar um padrão
  de segredo. Se isso acontecer:
  - **não** apague o histórico às cegas;
  - como são segredos **de exemplo** (não credenciais reais), você pode **permitir o push**
    pela própria mensagem de erro do GitHub (link "allow secret") **ou** já iniciar a
    correção (mover os segredos para variável de ambiente) antes de reenviar;
  - documente essa decisão — ela é o começo da funcionalidade **F-10 (gestão de segredos)**.

---

## 5. Fluxo de trabalho (por funcionalidade)

```
1. branch:      git checkout -b fix/F-02-idor
2. requisito:   registre RFS+RGS + ameaça (STRIDE/LINDDUN) + base legal
3. correção:    implemente o controle (deny-by-default, parametrizado, etc.)
4. teste:       escreva o teste que prova a correção (ex.: A pede exame de B -> 403)
5. PR:          abra o Pull Request; o pipeline roda os gates
6. review:      revisão por pares (checklist em docs/08)
7. merge:       só com o gate da classe corrigida verde
8. matriz:      atualize a rastreabilidade (requisito -> teste -> evidência -> status)
```

Quando terminar de corrigir uma classe de falha, **torne o gate bloqueante** removendo o
`continue-on-error: true` daquele job no workflow (ver [06-pipeline-cicd.md](06-pipeline-cicd.md)).

---

## 5.1 Testes de aceite já fornecidos

O professor já incluiu **testes de aceite mínimos** em
`legacy-app/backend/src/test/.../security/SegurancaAceiteTest.java` (`@Tag("security")`),
cobrindo IDOR, RBAC e SQLi no login. Eles **nascem vermelhos** no legado — são o **alvo**
do "pronto". O gate `Gate - Testes de seguranca` do pipeline os executa (com um MySQL de
serviço). **Você deve expandir** a cobertura com testes próprios na Fase 5; estes são só o
mínimo.

## 6. Autograding (como sua nota parcial é calculada)

O `.github/classroom/autograding.json` roda a cada push e pontua **quando o padrão
vulnerável desaparece** (sem MD5, sem SQL concatenado, JWT com expiração, sem
`bypassSecurityTrustHtml`, sem CORS `*`, segredo em variável de ambiente) e quando os
**testes de segurança passam**. Atenção: "enganar" o teste sem corrigir de verdade é
**anti-padrão** e será pego na revisão manual dos PRs.

---

## 7. Checklist para o kickoff (Fase 0)

- [ ] Repositório do grupo criado a partir do template.
- [ ] `docker compose up` funcionando; app explorada.
- [ ] Primeiro push feito; pipeline executou; placar vermelho registrado (print).
- [ ] Papéis do grupo definidos (Security Champion, Dev, DevSecOps, DPO).
- [ ] Inventário de ativos e classificação de dados iniciados (entregável da Fase 0).
