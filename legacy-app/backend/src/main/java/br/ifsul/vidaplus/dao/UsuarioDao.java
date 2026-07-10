package br.ifsul.vidaplus.dao;

import br.ifsul.vidaplus.security.LegacyHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Acesso a usuarios (LEGADO).
 * V-01: login por CONCATENACAO de SQL -> SQL Injection (A03/CWE-89).
 *       ex.: email = "' OR '1'='1' -- " ignora a senha.
 * V-02: compara com MD5 (via LegacyHash).
 */
@Repository
public class UsuarioDao {

    @Autowired
    private DataSource dataSource;

    /**
     * V-01: monta a query concatenando entradas do usuario. NAO usar como referencia.
     */
    public Map<String, Object> autenticar(String email, String senha) {
        String senhaMd5 = LegacyHash.md5(senha);
        String sql = "SELECT id, email, papel, paciente_id FROM usuario " +
                "WHERE email = '" + email + "' " +
                "AND senha_hash = '" + senhaMd5 + "' AND ativo = 1";
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getLong("id"));
                u.put("email", rs.getString("email"));
                u.put("papel", rs.getString("papel"));
                u.put("paciente_id", rs.getObject("paciente_id"));
                return u;
            }
            return null;
        } catch (Exception e) {
            // V-11: deixa a excecao subir crua
            throw new RuntimeException("Erro no login: " + e.getMessage(), e);
        }
    }
}
