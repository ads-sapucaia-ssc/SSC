package br.ifsul.vidaplus.controller;

import br.ifsul.vidaplus.dao.AtendimentoDao;
import br.ifsul.vidaplus.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agenda de atendimentos (LEGADO) - funcionalidades novas:
 *  - RECEPCAO: agenda completa + agendar novo atendimento.
 *  - MEDICO: "meus pacientes" (vinculo via atendimento).
 *
 * FALHA (F-03/V-04): nenhum endpoint aqui verifica o papel de quem chama -
 * mesmo padrao de RBAC ausente do resto do legado.
 *
 * Correcao esperada (Grupo 3): RECEPCAO/ADMIN para a agenda; MEDICO so ve a
 * propria carteira (medicoId do token, nao de path variable); auditoria.
 */
@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    @Autowired
    private AtendimentoDao atendimentoDao;

    private Long usuarioId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return JwtUtil.usuarioIdDoToken(auth.substring(7));
    }

    /** V-04: agenda completa, sem checar papel RECEPCAO. */
    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel
        return atendimentoDao.listarTodos();
    }

    /** V-04: agenda sem checar papel nem se os ids existem de fato. */
    @PostMapping
    public Map<String, Object> agendar(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        long medicoId = Long.parseLong(String.valueOf(body.get("medicoId")));
        long pacienteId = Long.parseLong(String.valueOf(body.get("pacienteId")));
        // FALHA: nao verifica papel nem valida os ids
        atendimentoDao.agendar(medicoId, pacienteId);
        return Map.of("medicoId", medicoId, "pacienteId", pacienteId, "agendado", true);
    }

    /** V-03/V-04: carteira de pacientes por id de medico livre, sem checar dono. */
    @GetMapping("/medico/{medicoId}")
    public List<Map<String, Object>> pacientesDoMedico(@PathVariable long medicoId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica se medicoId == o medico autenticado
        return atendimentoDao.pacientesDoMedico(medicoId);
    }
}
