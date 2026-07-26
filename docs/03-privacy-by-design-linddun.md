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

LINDDUN é o "STRIDE da privacidade" (o nome vem das iniciais das 7 categorias em inglês —
Linking, Identifying, Non-repudiation, Detecting, Data Disclosure, Unawareness,
Non-compliance — por isso as letras abaixo seguem essa ordem, mesmo com os nomes em
português). Roda-se sobre o **mesmo DFD** usado no STRIDE. Para cada elemento do DFD,
pergunta-se quais das 7 categorias de ameaça se aplicam:

| Letra | Categoria | Pergunta-guia | Ameaça-exemplo no VidaPlus |
|:---:|-----------|---------------|-----------------------------|
| **L** | Vinculação (*Linking*) | Dá para ligar dois itens ao mesmo titular? | IDs sequenciais permitem correlacionar exames de um paciente |
| **I** | Identificabilidade | Dá para identificar o titular por trás do dado? | Logs guardam CPF em texto claro |
| **N** | Não-repúdio | O titular fica preso a algo que negaria? | Ação registrada de forma que exponha o titular indevidamente |
| **D** | Detectabilidade | Dá para deduzir que um item existe? | Resposta diferente para "paciente existe" vs "não existe" |
| **D** | Divulgação de informação | Vazamento de dado a quem não deveria | Laudo de B acessível por A (IDOR) |
| **U** | Desconhecimento (*Unawareness*) / incapacidade de intervenção (*Unintervenability*) | Titular não sabe do tratamento **ou** sabe mas não consegue agir sobre ele | Coleta sem consentimento nem aviso; ou aviso existe, mas não há como revogar |
| **N** | Não-conformidade | Sistema desobedece política/lei | Retenção infinita; sem base legal registrada |

> **Por que "Vinculação" mantém a letra L, e "Desconhecimento" mantém a letra U?** As
> letras do LINDDUN seguem as palavras em **inglês** (Linking, Unawareness). Nem toda
> tradução para português começa com a mesma letra — por isso, sempre que a letra do
> acrônimo não corresponder à primeira letra do termo em português, este documento
> mostra as duas línguas (como acima), para não perder a referência à sigla.
>
> **"U" cobre duas coisas diferentes — vale separar.** *Desconhecimento* (o titular não
> sabe que o tratamento acontece, um problema de **transparência**) é diferente de
> *incapacidade de intervenção* (o titular sabe, mas não tem como agir — dar/revogar
> consentimento, corrigir, apagar — um problema de **controle**). No VidaPlus, o F-04
> (consentimento) é sobretudo o segundo caso: não é que o app engane o titular sobre o
> que faz — é que **não existe nenhum mecanismo** para ele consentir ou revogar.

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

## 3. Exemplo resolvido (modelo de qualidade)

> O **entregável avaliado** é a tabela LINDDUN-per-element do
> [`DOSSIE.md` §4.1](../entregaveis/DOSSIE.md) (cada grupo preenche a sua, sobre o próprio
> DFD). A tabela abaixo não é para preencher de novo — é o **padrão de qualidade** que a
> tabela do dossiê deve seguir: um exemplo já resolvido, por elemento, mostrando o nível de
> concretude esperado (ameaça específica, não "vazamento genérico"; controle/PET nomeado;
> requisito com ID).

| Elemento | Categoria | Ameaça concreta | Controle/PET | Requisito |
|----------|-----------|------------------|--------------|-----------|
| Logs de auditoria | **I** Identificabilidade | Logs gravam CPF e nome em texto claro; qualquer leitura do log identifica o titular | **Pseudonimização**: registrar `user_id` opaco, nunca CPF; mascarar campos sensíveis | REQ-PRIV-LOG-01 |
| Download de laudo | **D** Divulgação | Paciente A baixa laudo de B trocando o ID (IDOR) | Validação, no servidor, de quem é o dono do recurso; negar por padrão | REQ-AC-IDOR-01 |
| Cadastro | **U** Desconhecimento (*Unawareness*) | Coleta religião/estado civil sem finalidade nem aviso | **Minimização** (remover campo) + aviso + consentimento | REQ-PRIV-MIN-01 |
| Base de dados | **N** Não-conformidade | Retenção infinita de exames de pacientes inativos | Política de retenção + rotina de anonimização | REQ-PRIV-RET-01 |
| Busca de pacientes | **I** Identificabilidade (nó `I.2.3`) | Um filtro estreito (nome + CPF) devolve um único registro completo — a pessoa fica **distinguível das demais**, mesmo que a query esteja parametrizada (sem SQLi) | Limitar/mascarar campos no retorno da busca; exigir critério mínimo de abrangência | REQ-PRIV-BUSCA-01 |
| IDs de exame/paciente | **L** Vinculação (*Linking*, nó `L.1.1`) | IDs sequenciais e previsíveis (101, 102, 103...) permitem correlacionar e enumerar registros de um mesmo titular | IDs não sequenciais (UUID) para recursos referenciados por URL | REQ-PRIV-ID-01 |
| Conta do usuário (ADMIN) | **U** Incapacidade de intervenção (*Unintervenability*, nó `U.2.3`) | "Desativar" uma conta (`ativo = 0`) só bloqueia login — os dados do titular continuam intactos e processados; **desativar não é eliminar** | F-06 precisa apagar/anonimizar de verdade, não só sinalizar um status | REQ-PRIV-ELIM-01 |
| Controle de acesso interno | **N** Não-conformidade (nó `Nc.3`) | Nenhum endpoint verifica papel — RBAC ausente não é só uma falha STRIDE (elevação de privilégio), é também descumprimento do dever de segurança do art. 46 | `SecurityFilterChain`/checagem de papel deny-by-default | REQ-AC-RBAC-01 |

> ⚠️ **F-13 — duas falhas diferentes, duas correções diferentes.** Corrigir a injeção
> de SQL da busca (parametrizar a query) **não corrige** o problema acima: mesmo
> parametrizada, uma busca que aceita nome+CPF e devolve o registro completo ainda
> torna o titular **distinguível**. São ameaças distintas (uma é injeção de código;
> a outra é design da consulta) — cada uma com seu próprio requisito e teste.

### Casos reais para citar em aula

Dois incidentes documentados no catálogo de exemplos LINDDUN se conectam diretamente
ao VidaPlus e são úteis para justificar, na defesa, por que threat modeling de
privacidade vai além do STRIDE:

- **Vazamento de dados de rastreamento de contato (Países Baixos, covid).** Milhões de
  registros foram vendidos por **funcionários com acesso legítimo** aos sistemas — não
  houve invasão. *"Essa ameaça não seria encontrada pelo STRIDE clássico de divulgação
  de informação, pois não houve violação do sistema."* É exatamente o padrão da tela de
  gestão de usuários do VidaPlus: qualquer autenticado lista todo mundo via API
  "legítima", sem exploração nenhuma.
- **Vazamento de dados do Facebook (500 milhões de registros, 2021).** O vazamento
  atingiu até usuários que **já haviam apagado a própria conta** — os dados continuavam
  retidos apesar da "exclusão". Mesmo padrão do achado acima sobre desativação ×
  eliminação no VidaPlus.

---

## 4. Ligação LINDDUN → STRIDE → LGPD

Um mesmo problema costuma aparecer nas três lentes — e é isso que fecha o "fio completo":

```
IDOR no download de laudo
   ├─ STRIDE:  Divulgação de informação (Information Disclosure) / Elevação de privilégio
   ├─ LINDDUN: Divulgação + Vinculação (Linking, ID sequencial do exame)
   └─ LGPD:    art. 46 (segurança) + art. 11 (dado sensível)
        → Requisito REQ-AC-IDOR-01 → controle (checagem de dono do recurso no servidor)
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
