package br.ifsul.vidaplus.controller;

import br.ifsul.vidaplus.dao.ExameDao;
import br.ifsul.vidaplus.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Exames/laudos (LEGADO) - o ativo mais critico (saude, art. 11).
 *
 * V-03 (IDOR): getExame() e getPdf() NAO verificam se o exame pertence ao
 *              usuario autenticado -> paciente A baixa laudo de B trocando o id.
 * V-04 (RBAC): publicar() nao checa se o papel e BIOQUIMICO.
 * V-09: caminho do PDF previsivel e servido de pasta publica.
 *
 * Correcao esperada (Grupo B):
 *   - validar ownership/vinculo server-side (senao 403 sem vazar metadados);
 *   - RBAC deny-by-default;
 *   - registrar auditoria (F-05); gerar hash do laudo na publicacao (F-07);
 *   - download por autorizacao (stream/URL assinada) em vez de pasta publica.
 */
@RestController
@RequestMapping("/api/exames")
public class ExameController {

    @Autowired
    private ExameDao exameDao;

    private Long usuarioId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return JwtUtil.usuarioIdDoToken(auth.substring(7));
    }

    /** V-03: retorna qualquer exame, sem checar dono. */
    @GetMapping("/{id}")
    public Map<String, Object> getExame(@PathVariable long id,
                                        @RequestHeader(value = "Authorization", required = false) String auth) {
        Long uid = usuarioId(auth);
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nenhuma verificacao de que 'id' pertence a 'uid'
        return exameDao.porId(id);
    }

    /**
     * V-03 (tambem na listagem): historico de exames do paciente, sem checar
     * se 'pacienteId' e o proprio autenticado nem se o medico o atende
     * (ver ExameDao.medicoAtende - existe e NAO e chamado aqui de proposito).
     * Usado pela UI (Dashboard do paciente e Ficha do paciente).
     */
    @GetMapping("/paciente/{pacienteId}")
    public List<Map<String, Object>> porPaciente(@PathVariable long pacienteId,
                                        @RequestHeader(value = "Authorization", required = false) String auth) {
        Long uid = usuarioId(auth);
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nenhuma verificacao de que 'pacienteId' pertence/e atendido por 'uid'
        return exameDao.doPaciente(pacienteId);
    }

    /** V-03/V-09: "download" do PDF por caminho previsivel, sem autorizacao. */
    @GetMapping("/{id}/pdf")
    public Map<String, Object> getPdf(@PathVariable long id,
                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        Long uid = usuarioId(auth);
        if (uid == null) throw new RuntimeException("nao autenticado");
        Map<String, Object> exame = exameDao.porId(id);
        // FALHA: devolve o caminho publico do PDF sem checar ownership
        return Map.of("id", id, "laudo_pdf", exame.get("laudo_pdf"));
    }

    /** V-04: qualquer usuario autenticado publica laudo (sem RBAC). */
    @PostMapping("/{id}/publicar")
    public Map<String, Object> publicar(@PathVariable long id,
                                        @RequestHeader(value = "Authorization", required = false) String auth) {
        Long uid = usuarioId(auth);
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica JwtUtil.papelDoToken(...) == "BIOQUIMICO"
        exameDao.publicar(id, uid);
        return Map.of("id", id, "publicado", true);
    }

    /**
     * Fila de laudos pendentes de TODOS os pacientes (tela do BIOQUIMICO).
     * FALHA (F-03/V-04): nao verifica papel BIOQUIMICO.
     */
    @GetMapping("/pendentes")
    public List<Map<String, Object>> pendentes(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel
        return exameDao.pendentes();
    }

    /**
     * Paciente solicita um novo exame (autoatendimento).
     * FALHA (V-03 IDOR): nao verifica se 'pacienteId' e o proprio autenticado.
     */
    @PostMapping("/solicitar")
    public Map<String, Object> solicitar(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        long pacienteId = Long.parseLong(String.valueOf(body.get("pacienteId")));
        String tipo = String.valueOf(body.get("tipo"));
        // FALHA: nao verifica se pacienteId == o proprio paciente autenticado
        long id = exameDao.solicitar(pacienteId, tipo);
        return Map.of("id", id, "pacienteId", pacienteId, "tipo", tipo, "solicitado", true);
    }
}
