# 03 · Privacy by Design + LINDDUN

## 1. Privacy by Design (PbD) — os 7 princípios de Cavoukian

O VidaPlus legado viola vários deles. O projeto reintroduz cada princípio de forma
verificável.

| # | Princípio | O que significa no VidaPlus | Evidência esperada |
|---|-----------|------------------------------|--------------------|
| 1 | Proativo, não reativo | Threat/privacy modeling **antes** de codar | LINDDUN na Fase 2 |
| 2 | Privacidade como padrão | Deny-by-default; coleta mínima; retenção mínima | Config e schema |
| 3 | Privacidade embarcada no design | Controles no design, não "colados" depois | Fase 3 |
| 4 | Funcionalidade total (soma positiva) | Segurança **sem** quebrar usabilidade | DoD |
| 5 | Segurança ponta-a-ponta | Cifra em trânsito e repouso; ciclo de vida do dado | Fases 3–4 |
| 6 | Visibilidade e transparência | Titular sabe o que é tratado e por quê | Avisos + portabilidade |
| 7 | Respeito ao usuário | Consentimento, acesso, eliminação | Módulos LGPD |

> **PbD ≠ LGPD**, mas se reforçam: PbD é o *método*; a LGPD é a *obrigação*. LINDDUN é a
> *ferramenta* que operacionaliza os dois.

---

## 2. LINDDUN — threat modeling de privacidade

LINDDUN é o "STRIDE da privacidade". Roda-se sobre o **mesmo DFD** usado no STRIDE. Para
cada elemento do DFD, pergunta-se quais das 7 categorias de ameaça se aplicam:

| Letra | Categoria | Pergunta-guia | Ameaça-exemplo no VidaPlus |
|:---:|-----------|---------------|-----------------------------|
| **L** | Linkability | Dá para ligar dois itens ao mesmo titular? | IDs sequenciais permitem correlacionar exames de um paciente |
| **I** | Identifiability | Dá para identificar o titular por trás do dado? | Logs guardam CPF em texto claro |
| **N** | Non-repudiation | O titular fica preso a algo que negaria? | Ação registrada de forma que exponha o titular indevidamente |
| **D** | Detectability | Dá para deduzir que um item existe? | Resposta diferente para "paciente existe" vs "não existe" |
| **D** | Disclosure of information | Vazamento de dado a quem não deveria | Laudo de B acessível por A (IDOR) |
| **U** | Unawareness | Titular não sabe/controla o tratamento | Coleta sem consentimento nem aviso |
| **N** | Non-compliance | Sistema desобedece política/lei | Retenção infinita; sem base legal registrada |

### Metodologia aplicada (passos da Fase 2)

1. **DFD** do recorte, com entidades, processos, armazenamentos, fluxos e **trust
   boundaries** (o mesmo do STRIDE).
2. **Mapear DFD → ameaças LINDDUN** (tabela *LINDDUN-per-element*): para cada elemento,
   marcar as categorias aplicáveis.
3. **Descrever** cada ameaça de privacidade concreta (não "vazamento genérico").
4. **Priorizar** por risco (probabilidade × impacto à privacidade do titular).
5. **Derivar controle/requisito** (PET — *Privacy Enhancing Technology* — ou requisito):
   minimização, pseudonimização, anonimização, cifra, consentimento, transparência.
6. Alimentar o **RIPD/DPIA** (LGPD art. 38) e o **catálogo de requisitos**.

---

## 3. Modelo de tabela LINDDUN-per-element (para os grupos preencherem)

| Elemento (DFD) | L | I | N | D | D | U | N | Ameaça concreta | Controle/PET | Requisito |
|----------------|:-:|:-:|:-:|:-:|:-:|:-:|:-:|-----------------|--------------|-----------|
| Paciente (entidade) | | | | | | | | | | |
| API REST (processo) | | | | | | | | | | |
| Banco (armazenamento) | | | | | | | | | | |
| Logs (armazenamento) | | | | | | | | | | |
| Fluxo login | | | | | | | | | | |
| Fluxo download de laudo | | | | | | | | | | |

### Exemplo resolvido (modelo de qualidade)

| Elemento | Categoria | Ameaça concreta | Controle/PET | Requisito |
|----------|-----------|------------------|--------------|-----------|
| Logs de auditoria | **I** Identifiability | Logs gravam CPF e nome em texto claro; qualquer leitura do log identifica o titular | **Pseudonimização**: registrar `user_id` opaco, nunca CPF; mascarar campos sensíveis | REQ-PRIV-LOG-01 |
| Download de laudo | **D** Disclosure | Paciente A baixa laudo de B trocando o ID (IDOR) | Validação server-side de *ownership*; deny-by-default | REQ-AC-IDOR-01 |
| Cadastro | **U** Unawareness | Coleta religião/estado civil sem finalidade nem aviso | **Minimização** (remover campo) + aviso + consentimento | REQ-PRIV-MIN-01 |
| Base de dados | **N** Non-compliance | Retenção infinita de exames de pacientes inativos | Política de retenção + rotina de anonimização | REQ-PRIV-RET-01 |

---

## 4. Ligação LINDDUN → STRIDE → LGPD

Um mesmo problema costuma aparecer nas três lentes — e é isso que fecha o "fio completo":

```
IDOR no download de laudo
   ├─ STRIDE:  Information Disclosure / Elevation of Privilege
   ├─ LINDDUN: Disclosure of information + Linkability
   └─ LGPD:    art. 46 (segurança) + art. 11 (dado sensível)
        → Requisito REQ-AC-IDOR-01 → controle (ownership server-side)
        → teste TC-IDOR-01 (DAST no CI) → evidência (HTTP 403, sem metadados)
```

---

## 5. Entregável da Fase 2 (privacidade)

1. DFD com trust boundaries.
2. Tabela **LINDDUN-per-element** preenchida.
3. Lista priorizada de ameaças de privacidade + controle/PET por ameaça.
4. Esqueleto do **RIPD/DPIA** (art. 38) derivado da tabela.
5. Requisitos de privacidade que entram no catálogo da Fase 3.

> **Regra:** toda ameaça LINDDUN priorizada como alta **deve** virar requisito com teste na
> matriz de rastreabilidade (Fase 8). Ameaça de privacidade sem controle = lacuna.
