package br.ifsul.vidaplus.controller;

import br.ifsul.vidaplus.dao.UsuarioDao;
import br.ifsul.vidaplus.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Gestao de usuarios (LEGADO) - funcionalidade nova, pensada para ser
 * exclusiva do papel ADMIN, mas SEM NENHUMA checagem de papel no backend.
 *
 * FALHA (F-03/V-04): todo autenticado - inclusive PACIENTE - lista todo
 * mundo (e-mail, papel, status) e ativa/desativa QUALQUER conta, inclusive
 * a de outro admin. Mesmo padrao de RBAC ausente do resto do legado.
 *
 * Correcao esperada (Grupo 3): so ADMIN acessa; deny-by-default; auditoria
 * (F-05) de quem ativou/desativou quem.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioDao usuarioDao;

    private Long usuarioId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return JwtUtil.usuarioIdDoToken(auth.substring(7));
    }

    /** V-04: retorna todos os usuarios, sem checar papel ADMIN. */
    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica JwtUtil.papelDoToken(...) == "ADMIN"
        return usuarioDao.listarTodos();
    }

    /** V-04: ativa/desativa qualquer usuario, sem checar papel nem auditar. */
    @PostMapping("/{id}/alternar-ativo")
    public Map<String, Object> alternarAtivo(@PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel nem registra quem alterou (F-05 auditoria)
        boolean ok = usuarioDao.alternarAtivo(id);
        return Map.of("id", id, "alterado", ok);
    }
}
