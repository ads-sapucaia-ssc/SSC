package br.ifsul.vidaplus.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acesso a exames.
 * Observacao: aqui usamos JdbcTemplate parametrizado (isto NAO e a falha).
 * A falha do fluxo de exame e de AUTORIZACAO (V-03 IDOR / V-04 RBAC),
 * cometida no ExameController, nao na query.
 */
@Repository
public class ExameDao {

    @Autowired
    private JdbcTemplate jdbc;

    public Map<String, Object> porId(long id) {
        List<Map<String, Object>> r = jdbc.queryForList(
                "SELECT * FROM exame WHERE id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<Map<String, Object>> doPaciente(long pacienteId) {
        return jdbc.queryForList(
                "SELECT id, tipo, publicado, criado_em FROM exame WHERE paciente_id = ?",
                pacienteId);
    }

    public boolean medicoAtende(long medicoId, long pacienteId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM atendimento WHERE medico_id = ? AND paciente_id = ?",
                Integer.class, medicoId, pacienteId);
        return n != null && n > 0;
    }

    public void publicar(long exameId, long publicadoPor) {
        // F-07: publica sem registrar hash/versao (integridade ausente)
        jdbc.update("UPDATE exame SET publicado = 1, publicado_por = ? WHERE id = ?",
                publicadoPor, exameId);
    }
}
