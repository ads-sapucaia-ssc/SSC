# Frontend VidaPlus (React 17 — legado)

Este diretório contém o **esqueleto representativo** do portal em **React**, com os pontos
vulneráveis-chave já plantados para uso em aula. Não é o app completo de telas — os grupos
vão expandir os componentes conforme os pacotes de trabalho.

## Falhas plantadas (front)

| Arquivo | Falha | Catálogo |
|---------|-------|:--------:|
| `src/config/api.js` | HTTP puro (sem TLS) | V-13 |
| `src/services/auth.js` | JWT eterno em `localStorage` (roubável por XSS) | V-07/V-10 |
| `src/components/Prontuario.jsx` | `dangerouslySetInnerHTML` → **Stored XSS** | V-10 |
| `src/services/exame.js` + `App.jsx` | confia no id do exame; "esconder no front" não é controle | V-03 |

## O que os grupos expandem

- Telas de login, lista de exames, download de laudo, prontuário, cadastro.
- Módulos LGPD (consentimento, exportação, exclusão) — Grupo C.
- Interceptor/cliente HTTP (axios) para anexar o token de forma segura e tratar 401/403.

## Rodar em desenvolvimento

```bash
npm install
npm start   # http://localhost:3000  (fala com a API em http://localhost:8080)
```

> A estrutura React mínima (`public/index.html`, `src/index.js`, `src/App.jsx`) já está
> aqui; os arquivos que carregam o conteúdo didático de segurança são os da tabela acima.
> O `dangerouslySetInnerHTML` é o equivalente React do `innerHTML`/`bypassSecurityTrustHtml`
> do Angular — mesma falha (Stored XSS), outra sintaxe.
