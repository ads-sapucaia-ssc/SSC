# language: pt
@F-03 @REQ-AC-RBAC-01 @P0 @lgpd-art46 @asvs-V4 @stride-EoP @security
Funcionalidade: F-03 · Publicação de laudo por papel (REQ-AC-RBAC-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-AC-RBAC-01.
  Base legal: LGPD art. 46.
  # Implementado nesta PoC (@security → roda no gate).

  Cenário: paciente não pode publicar laudo
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    Quando faço POST em "/api/exames/104/publicar"
    Então o status HTTP é 403
