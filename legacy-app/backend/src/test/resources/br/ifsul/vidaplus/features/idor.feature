# language: pt
@F-02 @REQ-AC-IDOR-01 @P0 @lgpd-art11 @lgpd-art46 @asvs-V4 @stride-EoP @linddun-divulgacao @security
Funcionalidade: F-02 · Acesso a laudo (REQ-AC-IDOR-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-AC-IDOR-01.
  Base legal: LGPD art. 11 (dado sensível de saúde) + art. 46 (segurança).
  # Implementado nesta PoC (@security → roda no gate).

  Cenário: paciente não acessa o laudo de outro paciente
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    Quando faço GET em "/api/exames/102"
    Então o status HTTP é 403
    E a resposta não contém "Glicemia"
