# 07 · Rubrica de avaliação (alinhada às fases)

A avaliação é **contínua e por fase** — não há prova única. O foco da disciplina é
**SSDLC + LGPD + privacidade**, então o peso está nos **artefatos do processo**; a
remediação de código entra como **prova de conceito** e o pipeline como **evidência**, não
como a nota em si.

Escala **0–10**. A tabela abaixo é a **nota de artefatos** (do grupo); a **defesa** entra
depois (0–10, com fator individual) e as duas se combinam na **nota final** — ver
"Como fecha a nota (0–10)".

| # | Fase / critério | Peso (0–10) | O que se espera |
|---|-----------------|:-----------:|-----------------|
| 0 | Inventário & classificação de dados | 0,8 | Ativos e endpoints mapeados; dados classificados; fluxos críticos identificados |
| 1 | Requisitos & **LGPD** | 1,4 | RFS+RGS testáveis; **base legal por dado**; nível ASVS justificado |
| 2 | Threat modeling (STRIDE) | 1,0 | DFD com trust boundaries; STRIDE-per-element; árvore de ataque |
| 2p | **Privacidade — LINDDUN + PbD + RIPD/DPIA** | 1,8 | LINDDUN-per-element; controles/PET; minimização; **RIPD** derivado |
| 3 | Projeto seguro & priorização | 1,0 | Catálogo consolidado; risco→P0/P1/P2 por **gravidade (CVSS/DREAD) × exploração real (EPSS/KEV)**; **Definition of Done** segura |
| 4 | **Remediação — prova de conceito** | 0,8 | 1–2 controles corrigidos ponta-a-ponta **+ plano de remediação** do restante |
| 5 | Verificação & evidências | 1,2 | O teste do P0 vermelho → verde, com evidência real do CI; leitura correta dos relatórios dos scanners |
| 6 | DevSecOps / pipeline (evidência) | 0,6 | **Explicação** no dossiê do que cada gate verifica, do SBOM, da **assinatura + SBOM atestado** (e por que isso ainda não é SLSA L2) e da **promoção verificada** (staging → aprovação → prod) — o pipeline já provê; o aluno lê e justifica. Segredos no código (F-10) identificados e com plano de remediação |
| 7 | **Operação, incidente & LGPD art. 48** | 0,6 | Operação das funcionalidades do pacote (o que monitorar e registrar, sem PII; trilha íntegra, para quem tem o F-05); runbook de um incidente do pacote + fluxo de notificação ANPD |
| 8 | Rastreabilidade & defesa | 0,8 | Matriz completa; métricas; **defesa fecha o fio ao vivo** (pontuada à parte) |
| — | **Nota de artefatos (grupo)** | **10,0** | |

> **Onde está o peso:** Fases 1, 2, 2p e 8 (requisitos+LGPD, threat modeling, **privacidade**
> e rastreabilidade) somam **5,0** — metade da nota está no **processo e na privacidade**.
> A implementação (F4) vale **0,8**: importa **provar o ciclo**, não a quantidade de código.

> Sem bônus nem penalidades à parte: **a qualidade entra na nota da própria fase**. Ex.:
> PII em log ou "OK" na matriz sem prova **rebaixam** a fase correspondente (7 e 5/8); um
> requisito vago rebaixa a Fase 1; fazer além do mínimo (ex.: mais um módulo LGPD) **eleva**
> a fase respectiva.

---

## Defesa — como pontuar (banca: o professor)

A defesa é **do grupo** (10–15 min), mas **cada integrante demonstra domínio
individualmente**: cada um **conduz ao menos um elo do fio** e **responde a perguntas sobre
qualquer parte** do projeto — o professor direciona as perguntas, então ninguém se esconde
no grupo.

### Nota da defesa (0–10) — do grupo

| Critério | Pontos | O que avaliar |
|----------|:------:|---------------|
| **Fio ao vivo** | 4,0 | percorre ativo → ameaça → requisito → controle → teste → **evidência real** (mostra o teste no CI / a linha na matriz); correto e coerente |
| **Privacidade** | 1,5 | ameaça **LINDDUN** → PET/controle → trecho do **RIPD** |
| **Justificativa das decisões** | 2,0 | **priorização** P0/P1/P2; o que ficou no **plano de remediação** e por quê; **fail-secure** |
| **Respostas à arguição** | 2,5 | responde com precisão e segurança às perguntas do professor |

### Domínio individual (fator por integrante)

Cada integrante recebe um conceito, pelo elo que conduziu **e** pelas respostas:

- **Pleno (× 1,0)** — conduz bem e responde sobre **qualquer** parte;
- **Parcial (× 0,8)** — conduz o seu, mas hesita fora dele;
- **Insuficiente (× 0,5)** — não sustenta nem o próprio elo.

> ⚠️ **A defesa é eliminatória, não só ponderada.** Como os artefatos podem ser produzidos
> com ajuda de IA, a defesa é a **prova individual** de que o aluno domina o que o grupo
> entregou. Por isso ela é **condição de aprovação**: **domínio individual "Insuficiente"
> reprova** — a nota final fica **limitada a 5,9**, qualquer que seja a nota de artefatos.
> Quem não sustenta nem o próprio elo do fio **não** é aprovado, ainda que o dossiê do grupo
> seja nota 10.

### Como fecha a nota (0–10)

Artefatos e Defesa são **cada um 0–10**. A nota final é **individual** e o fator de domínio
multiplica o **conjunto** (não só a defesa) — assim quem não domina **não** é carregado pelos
artefatos do grupo:

```
Nota final (individual) = (0,8 × Artefatos(grupo) + 0,2 × Defesa(grupo)) × fator individual
```

**Barreira (regra de aprovação):** se o domínio individual for **"Insuficiente"**, a nota
final é **reprovada** (≤ 5,9), independentemente da nota de artefatos.

**Por que as duas coisas juntas.** O fator sobre o conjunto já faz "Insuficiente ×0,5" ter
**teto de 5,0** (mesmo com tudo 10); a barreira torna a regra **explícita** para o aluno e
cobre arredondamentos. Exemplos:

| Cenário | Cálculo | Resultado |
|---|---|---|
| **Grupo só usou IA, ninguém defende** (Artef. 10 · Defesa 0 · Insuficiente) | `(0,8×10 + 0,2×0) × 0,5 = 4,0` **+ barreira** | **Reprova** |
| Domina bem (Pleno · Artef. 10 · Defesa 8) | `(0,8×10 + 0,2×8) × 1,0 = 9,6` | Aprova |
| Defende só o seu elo (Parcial · Artef. 9 · Defesa 6) | `(0,8×9 + 0,2×6) × 0,8 = 6,72` | Aprova |

> **Combate ao "caroneiro" e à entrega feita só por IA:** o grupo pode ir bem nos artefatos,
> mas a nota **só se realiza** para quem **defende** o que foi entregue.

---

## O papel do pipeline e do autograding

São **instrumentos de evidência e feedback**, **não a nota**:
- o **placar de gates** dá ao grupo um sinal objetivo do efeito da PoC (🔴→🟢);
- o **autograding** dá nota parcial automática de apoio.

A nota real vem do **dossiê de segurança e privacidade** (artefatos das fases) e da
**defesa**. Um pipeline verde sem os artefatos por trás **não** vale a nota; artefatos
sólidos com uma PoC bem-feita **valem**.

## Definition of Done — dois níveis (ver docs/05)

- **Analisada** (todas as funcionalidades do pacote): requisito + base legal + ameaça
  (STRIDE/LINDDUN) + controle proposto + linha na matriz + plano de remediação.
- **Implementada** (1–2 por grupo, prova de conceito): + correção em PR + teste + evidência
  + status "Verificado" + gate 🔴→🟢.

## Checklist de secure code review (para os PRs da PoC)

- [ ] Entrada validada e consultas parametrizadas (sem concatenação SQL).
- [ ] Autorização server-side (ownership + RBAC deny-by-default).
- [ ] Sem segredo no código; config por ambiente.
- [ ] Erro genérico ao cliente; detalhe só no log (sem PII).
- [ ] Evento de segurança auditado (append-only, sem PII).
- [ ] Dado tratado tem base legal e finalidade (LGPD).
- [ ] Teste cobre o caminho de ataque e o comportamento fail-secure.
