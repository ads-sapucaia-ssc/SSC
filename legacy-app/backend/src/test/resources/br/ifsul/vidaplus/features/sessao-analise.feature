# language: pt
@F-11 @REQ-SESS-01 @P1 @lgpd-art46 @asvs-V3 @stride-spoofing @analise
Funcionalidade: F-11 · Sessão / token (REQ-SESS-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-SESS-01.
  Base legal: LGPD art. 46.
  # ANALISADA, NÃO implementada nesta PoC → tag @analise: NÃO roda no gate
  # (o passo "que a sessão expirou" nem tem glue — e tudo bem, pois é filtrada).

  Cenário: token expirado é rejeitado
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    E que a sessão expirou
    Quando faço GET em "/api/exames/101"
    Então o status HTTP é 401
