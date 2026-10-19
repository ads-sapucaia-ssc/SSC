package br.ifsul.vidaplus.controller;

import br.ifsul.vidaplus.dao.PacienteDao;
import br.ifsul.vidaplus.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Pacientes (LEGADO).
 * V-01 (F-13): busca repassa 'termo' para SQL concatenado (SQLi).
 * V-14: retorna campos sobrecoletados.
 * V-04: editar() nao checa papel (RECEPCAO/ADMIN) nem registra auditoria.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteDao pacienteDao;

    /** V-01 (F-13): 'q' vai direto para a query concatenada no DAO. */
    @GetMapping("/busca")
    public List<Map<String, Object>> buscar(@RequestParam("q") String q) {
        return pacienteDao.buscar(q);
    }

    @GetMapping("/{id}")
    public Map<String, Object> porId(@PathVariable long id) {
        // (sem checagem de papel/vinculo)
        return pacienteDao.porId(id);
    }

    /**
     * Nota clinica do prontuario (consumida por Prontuario.jsx).
     * V-10: devolve HTML CRU, sem sanitizacao -> Stored XSS ao ser renderizado
     *       com dangerouslySetInnerHTML no front.
     * FALHA adicional: sem autenticacao e sem vinculo - qualquer um le a nota
     *       clinica de qualquer paciente (dado de saude, LGPD art. 11).
     */
    @GetMapping("/{id}/notas")
    public Map<String, Object> notas(@PathVariable long id) {
        return pacienteDao.notaDoPaciente(id);
    }

    /** V-04: qualquer autenticado edita qualquer cadastro; sem auditoria (V-06). */
    @PutMapping("/{id}")
    public Map<String, Object> editar(@PathVariable long id,
                                      @RequestBody Map<String, Object> dados,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        Long uid = auth != null && auth.startsWith("Bearer ")
                ? JwtUtil.usuarioIdDoToken(auth.substring(7)) : null;
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel; nao registra quem alterou o que
        return Map.of("id", id, "atualizado", true, "campos", dados.keySet());
    }
}
