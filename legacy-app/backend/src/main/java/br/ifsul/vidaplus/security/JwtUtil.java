package br.ifsul.vidaplus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * V-07 / V-05: emissao e validacao de token INSEGURAS (mesmo usando jjwt atual).
 *  - segredo FIXO no codigo (hardcoded, compartilhado com application.properties);
 *  - SEM expiracao (o token vale para sempre) -> nao ha rotacao nem invalidacao.
 * Os grupos devem: expiracao curta + refresh, segredo em cofre/variavel de ambiente,
 * invalidacao no logout/troca de senha.
 */
public class JwtUtil {

    // V-05: segredo hardcoded (>=256 bits apenas para o HS256 nao recusar a chave)
    private static final String SECRET = "vidaplus-secret-123-inseguro-de-proposito-ssc!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String gerarToken(Long usuarioId, String papel) {
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("papel", papel)
                // V-07: nenhum .expiration(...) -> token eterno
                .signWith(KEY)
                .compact();
    }

    /** V-07: valida assinatura, mas o token nunca expira. Retorna o id ou null. */
    public static Long usuarioIdDoToken(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(KEY).build()
                    .parseSignedClaims(token).getPayload();
            return Long.valueOf(c.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public static String papelDoToken(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(KEY).build()
                    .parseSignedClaims(token).getPayload();
            return (String) c.get("papel");
        } catch (Exception e) {
            return null;
        }
    }
}
