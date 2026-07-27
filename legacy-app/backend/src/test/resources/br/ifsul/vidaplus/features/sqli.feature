# language: pt
@F-01 @REQ-AUTH-SQLI-01 @P0 @lgpd-art46 @asvs-V5 @stride-tampering @security
Funcionalidade: F-01 · Login (injeção de SQL / REQ-AUTH-SQLI-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-AUTH-SQLI-01.
  Base legal: LGPD art. 46.
  # Implementado nesta PoC (@security → roda no gate).

  Cenário: payload de injeção no login não autentica
    Quando tento logar com email "admin@vidaplus.local' -- " e senha "qualquer"
    Então o status HTTP é 401
    E a resposta não contém "access_token"
