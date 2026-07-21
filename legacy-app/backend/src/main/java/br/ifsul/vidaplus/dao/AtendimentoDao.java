package br.ifsul.vidaplus.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acesso a atendimentos (vinculo medico-paciente / agenda).
 * A tabela ja existia no schema (so era usada por ExameDao.medicoAtende(),
 * nunca ligada a nenhum endpoint); aqui vira a base de duas funcionalidades
 * novas: agenda (RECEPCAO) e "meus pacientes" (MEDICO).
 */
@Repository
public class AtendimentoDao {

    @Autowired
    private JdbcTemplate jdbc;

    /** Agenda completa (RECEPCAO). FALHA: sem paginacao nem filtro por unidade. */
    public List<Map<String, Object>> listarTodos() {
        return jdbc.queryForList(
                "SELECT a.id, a.data, a.medico_id, m.nome AS medico_nome, " +
                "a.paciente_id, p.nome AS paciente_nome " +
                "FROM atendimento a " +
                "JOIN medico m ON m.id = a.medico_id " +
                "JOIN paciente p ON p.id = a.paciente_id " +
                "ORDER BY a.data DESC");
    }

    /**
     * Agenda um atendimento.
     * FALHA (F-03/V-04): nao checa se quem chama e RECEPCAO nem se os ids
     * (medicoId/pacienteId) realmente existem.
     */
    public void agendar(long medicoId, long pacienteId) {
        jdbc.update("INSERT INTO atendimento (medico_id, paciente_id) VALUES (?, ?)",
                medicoId, pacienteId);
    }

    /**
     * Pacientes atendidos por um medico (tela "Meus pacientes").
     * FALHA (F-03/V-04): nao verifica se 'medicoId' e o proprio medico
     * autenticado - qualquer um ve a carteira de pacientes de qualquer medico.
     */
    public List<Map<String, Object>> pacientesDoMedico(long medicoId) {
        return jdbc.queryForList(
                "SELECT DISTINCT p.id, p.nome, p.cpf " +
                "FROM atendimento a JOIN paciente p ON p.id = a.paciente_id " +
                "WHERE a.medico_id = ? ORDER BY p.nome",
                medicoId);
    }
}
