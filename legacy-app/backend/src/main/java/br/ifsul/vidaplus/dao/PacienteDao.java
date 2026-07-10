package br.ifsul.vidaplus.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso a pacientes (LEGADO).
 * V-13: busca por nome/CPF por CONCATENACAO -> SQL Injection.
 * V-14: SELECT * expoe campos sobrecoletados (religiao, renda...).
 */
@Repository
public class PacienteDao {

    @Autowired
    private DataSource dataSource;

    /** V-13: termo concatenado direto na query. */
    public List<Map<String, Object>> buscar(String termo) {
        String sql = "SELECT * FROM paciente WHERE ativo = 1 AND " +
                "(nome LIKE '%" + termo + "%' OR cpf LIKE '%" + termo + "%')";
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("nome", rs.getString("nome"));
                p.put("cpf", rs.getString("cpf"));            // V-14: PII sem mascaramento
                p.put("email", rs.getString("email"));
                p.put("religiao", rs.getString("religiao"));   // V-14: dado sem finalidade
                p.put("renda_mensal", rs.getObject("renda_mensal"));
                out.add(p);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Erro na busca: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> porId(long id) {
        String sql = "SELECT * FROM paciente WHERE id = " + id; // V-13 tambem
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("nome", rs.getString("nome"));
                p.put("cpf", rs.getString("cpf"));
                p.put("email", rs.getString("email"));
                return p;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
