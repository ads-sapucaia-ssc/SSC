# Frontend VidaPlus (React 19 + Vite)

Esqueleto representativo do portal em **React (stack atual)**, com os pontos vulneráveis-
chave já plantados para uso em aula. O "legado" é só o **contexto narrativo** — as falhas
estão no **código/design**, não na versão da tecnologia. Não é o app completo de telas: os
grupos expandem os componentes conforme os pacotes de trabalho.

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
- Cliente HTTP (axios) para anexar o token de forma segura e tratar 401/403.

## Rodar em desenvolvimento

```bash
npm install
npm run dev   # http://localhost:5173  (fala com a API em http://localhost:8080)
```

> `dangerouslySetInnerHTML` é o vetor de **Stored XSS** aqui — a correção é deixar o JSX
> escapar o texto (padrão do React) ou sanitizar (DOMPurify) no cliente **e** no servidor,
> além de CSP.
