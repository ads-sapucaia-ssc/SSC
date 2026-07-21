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
     * Busca usuario pelo e-mail (usado pelo reset de senha).
     * V-08: e o que permite a ENUMERACAO - o controller denuncia na resposta se
     *       o e-mail existe ou nao.
     * V-01 tambem: e-mail concatenado na query.
     */
    public Map<String, Object> porEmail(String email) {
        String sql = "SELECT id, email, papel FROM usuario " +
                "WHERE email = '" + email + "' AND ativo = 1";
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getLong("id"));
                u.put("email", rs.getString("email"));
                u.put("papel", rs.getString("papel"));
                return u;
            }
            return null;
        } catch (Exception e) {
            // V-11: excecao crua
            throw new RuntimeException("Erro na consulta por e-mail: " + e.getMessage(), e);
        }
    }

    /**
     * V-01: monta a query concatenando entradas do usuario. NAO usar como referencia.
     * LEFT JOINs so para exibir um nome de saudacao na UI - nao alteram a falha
     * (o email/senha continuam concatenados do mesmo jeito).
     */
    public Map<String, Object> autenticar(String email, String senha) {
        String senhaMd5 = LegacyHash.md5(senha);
        String sql = "SELECT u.id, u.email, u.papel, u.paciente_id, " +
                "COALESCE(p.nome, m.nome, SUBSTRING_INDEX(u.email, '@', 1)) AS nome " +
                "FROM usuario u " +
                "LEFT JOIN paciente p ON p.id = u.paciente_id " +
                "LEFT JOIN medico m ON m.usuario_id = u.id " +
                "WHERE u.email = '" + email + "' " +
                "AND u.senha_hash = '" + senhaMd5 + "' AND u.ativo = 1";
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getLong("id"));
                u.put("email", rs.getString("email"));
                u.put("papel", rs.getString("papel"));
                u.put("paciente_id", rs.getObject("paciente_id"));
                u.put("nome", rs.getString("nome"));
                return u;
            }
            return null;
        } catch (Exception e) {
            // V-11: deixa a excecao subir crua
            throw new RuntimeException("Erro no login: " + e.getMessage(), e);
        }
    }
}
