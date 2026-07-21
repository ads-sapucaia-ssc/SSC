package br.ifsul.vidaplus.controller;

import br.ifsul.vidaplus.dao.UsuarioDao;
import br.ifsul.vidaplus.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Autenticacao (LEGADO).
 * V-01/V-02: login via UsuarioDao (SQLi + MD5).
 * V-07: emite JWT sem expiracao.
 * V-08: reset de senha com token previsivel e resposta que enumera e-mail.
 * (Sem rate limit -> brute force, F-01.)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioDao usuarioDao;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        // V-01: email/senha vao direto para SQL concatenado
        Map<String, Object> u = usuarioDao.autenticar(email, senha);
        Map<String, Object> resp = new HashMap<>();
        if (u == null) {
            resp.put("erro", "credenciais invalidas");
            return resp;
        }
        String token = JwtUtil.gerarToken((Long) u.get("id"), (String) u.get("papel"));
        resp.put("access_token", token);      // V-07: token eterno
        resp.put("papel", u.get("papel"));
        resp.put("paciente_id", u.get("paciente_id"));
        return resp;
    }

    /**
     * V-08: gera "token" previsivel (id + timestamp) e responde diferente
     * conforme o e-mail exista (enumeracao / LINDDUN Detectability).
     */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, Object> resp = new HashMap<>();
        // V-08: consulta o banco de verdade e DENUNCIA o resultado na resposta.
        Map<String, Object> u = (email != null) ? usuarioDao.porEmail(email) : null;
        if (u != null) {
            // V-08: token previsivel (id do usuario + timestamp), sem uso unico nem expiracao
            String token = u.get("id") + "-" + System.currentTimeMillis();
            resp.put("mensagem", "e-mail encontrado, enviando link de reset");
            resp.put("reset_token", token); // V-08: token exposto na resposta
        } else {
            resp.put("mensagem", "e-mail nao cadastrado"); // V-08: denuncia inexistencia
        }
        return resp;
    }
}
