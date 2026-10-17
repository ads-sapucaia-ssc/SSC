package br.ifsul.vidaplus.config;

import br.ifsul.vidaplus.security.LegacyHash;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * V-02: o PasswordEncoder configurado usa MD5 SEM SALT (via LegacyHash) -
 * a mesma fraqueza de sempre (CWE-916), agora expressa como uma
 * configuracao do Spring Security (spring-security-crypto), nao mais uma
 * chamada direta a um metodo estatico.
 *
 * Correcao esperada (Grupo 1): trocar o bean por
 * Argon2PasswordEncoder.defaultsInstance() - o Spring Security ja traz
 * pronto, nao e preciso escrever hashing. O salt fica embutido na propria
 * string retornada (formato "$argon2id$v=...$m=...$<salt>$<hash>") - nao
 * precisa de coluna nova no banco.
 *
 * ATENCAO: Argon2 gera um salt aleatorio a cada chamada de encode(), entao
 * so funciona com verificacao via passwordEncoder.matches(senha, hash) em
 * JAVA - nao da mais pra comparar com "=" dentro do SQL (ver
 * UsuarioDao.autenticar()). Ou seja: essa troca so fica limpa DEPOIS que a
 * query do login for parametrizada (corrigir o V-01 primeiro) - as duas
 * falhas estao acopladas neste endpoint.
 *
 * ATENCAO 2: os usuarios do seed.sql JA TEM hash MD5 gravado, e a coluna
 * usuario.senha_hash e CHAR(32). Trocar o bean "seco" faz TODO login existente
 * falhar (o teste login_valido_funciona fica vermelho). Planeje a migracao dos
 * hashes existentes e o tamanho da coluna junto com a troca.
 */
@Configuration
public class SenhaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // FALHA (V-02): encoder "caseiro" equivalente a MD5 sem salt.
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return LegacyHash.md5(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return LegacyHash.md5(rawPassword.toString()).equals(encodedPassword);
            }
        };
    }
}
