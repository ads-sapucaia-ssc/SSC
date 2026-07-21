package br.ifsul.vidaplus.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso a usuarios (LEGADO).
 * V-01: login por CONCATENACAO de SQL -> SQL Injection (A03/CWE-89).
 *       ex.: email = "' OR '1'='1' -- " ignora a senha.
 * V-02: hash de senha fraco - o PasswordEncoder configurado (ver
 *       SenhaConfig) usa MD5 sem salt.
 */
@Repository
public class UsuarioDao {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
     *
     * V-02: o hash vem do PasswordEncoder configurado (ver SenhaConfig), hoje
     * MD5 - mas ainda e CONCATENADO na propria SQL abaixo (nao comparado via
     * passwordEncoder.matches()). Isso e proposital: preserva o V-01 (o
     * "-- " no email comenta esta clausula inteira, ignorando a senha).
     * Argon2/bcrypt NAO funcionam aqui (salt aleatorio por chamada nunca
     * bateria com "="): so dá pra trocar o encoder de verdade DEPOIS de
     * parametrizar esta query (corrigir o V-01 primeiro).
     */
    public Map<String, Object> autenticar(String email, String senha) {
        String senhaHash = passwordEncoder.encode(senha);
        String sql = "SELECT u.id, u.email, u.papel, u.paciente_id, m.id AS medico_id, " +
                "COALESCE(p.nome, m.nome, SUBSTRING_INDEX(u.email, '@', 1)) AS nome " +
                "FROM usuario u " +
                "LEFT JOIN paciente p ON p.id = u.paciente_id " +
                "LEFT JOIN medico m ON m.usuario_id = u.id " +
                "WHERE u.email = '" + email + "' " +
                "AND u.senha_hash = '" + senhaHash + "' AND u.ativo = 1";
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getLong("id"));
                u.put("email", rs.getString("email"));
                u.put("papel", rs.getString("papel"));
                u.put("paciente_id", rs.getObject("paciente_id"));
                u.put("medico_id", rs.getObject("medico_id"));
                u.put("nome", rs.getString("nome"));
                return u;
            }
            return null;
        } catch (Exception e) {
            // V-11: deixa a excecao subir crua
            throw new RuntimeException("Erro no login: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos os usuarios (tela de gestao do ADMIN).
     * FALHA (F-03/V-04): nenhum controller que chama isto verifica o papel de
     * quem pediu - qualquer autenticado, inclusive PACIENTE, ve email/papel/
     * status de todo mundo. Nunca devolve senha_hash.
     */
    public List<Map<String, Object>> listarTodos() {
        String sql = "SELECT id, email, papel, ativo FROM usuario ORDER BY id";
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getLong("id"));
                u.put("email", rs.getString("email"));
                u.put("papel", rs.getString("papel"));
                u.put("ativo", rs.getInt("ativo") == 1);
                out.add(u);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Ativa/desativa um usuario (um usuario inativo nao consegue mais logar -
     * ver autenticar(), que exige ativo = 1).
     * FALHA (F-03/V-04): sem checagem de papel nem auditoria (F-05) - qualquer
     * autenticado desativa/reativa QUALQUER usuario, inclusive outro admin.
     */
    public boolean alternarAtivo(long id) {
        String sql = "UPDATE usuario SET ativo = 1 - ativo WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao alternar usuario: " + e.getMessage(), e);
        }
    }
}
