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

    /**
     * Fila de laudos pendentes de publicacao, de TODOS os pacientes
     * (tela do BIOQUIMICO).
     * FALHA: lista o sistema inteiro, incluindo exames sensiveis ainda em
     * rascunho de qualquer paciente - sem filtrar por nada alem de
     * publicado = 0 (ver nota F-07 x F-08 em docs/04: "publicado" e so
     * rotulo de fluxo, nao controle de acesso - esta fila prova isso de novo).
     */
    public List<Map<String, Object>> pendentes() {
        return jdbc.queryForList(
                "SELECT e.id, e.tipo, e.criado_em, e.paciente_id, p.nome AS paciente_nome " +
                "FROM exame e JOIN paciente p ON p.id = e.paciente_id " +
                "WHERE e.publicado = 0 ORDER BY e.criado_em");
    }

    /**
     * Paciente solicita um novo exame (autoatendimento).
     * FALHA (V-03 IDOR tambem aqui): nao verifica se 'pacienteId' e o proprio
     * paciente autenticado - da para solicitar exame em nome de outro paciente.
     */
    public long solicitar(long pacienteId, String tipo) {
        jdbc.update("INSERT INTO exame (paciente_id, tipo, publicado) VALUES (?, ?, 0)",
                pacienteId, tipo);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
