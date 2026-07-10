# Frontend VidaPlus (Angular 12 — legado)

Este diretório contém o **esqueleto representativo** do portal, com os pontos
vulneráveis-chave já plantados para uso em aula. Não é o app completo de telas — os
grupos vão expandir os componentes conforme os pacotes de trabalho.

## Falhas plantadas (front)

| Arquivo | Falha | Catálogo |
|---------|-------|:--------:|
| `core/api.config.ts` | HTTP puro (sem TLS) | V-13 |
| `auth/auth.service.ts` | JWT eterno em `localStorage` (roubável por XSS) | V-07/V-10 |
| `prontuario/prontuario.component.ts` | `bypassSecurityTrustHtml` + `[innerHTML]` → **Stored XSS** | V-10 |
| `prontuario/exame.service.ts` | confia no id do exame; "esconder no front" não é controle | V-03 |

## O que os grupos expandem

- Telas de login, lista de exames, download de laudo, prontuário, cadastro.
- Módulos LGPD (consentimento, exportação, exclusão) — Grupo C.
- Interceptor HTTP para anexar o token de forma segura e tratar 401/403.

## Rodar em desenvolvimento

```bash
npm install
npm start   # http://localhost:4200  (fala com a API em http://localhost:8080)
```

> A estrutura Angular mínima (angular.json, main.ts, index.html, app.module) deve ser
> gerada com `ng new` ou completada pelos grupos; os arquivos aqui são os que carregam o
> conteúdo didático de segurança.
