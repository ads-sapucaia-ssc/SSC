package br.ifsul.vidaplus.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;

/**
 * V-07 / V-05: emissao e validacao de token INSEGURAS.
 *  - segredo fixo e curto (compartilhado com application.properties);
 *  - SEM expiracao (token vale para sempre);
 *  - parse aceita token sem verificar assinatura de forma estrita (alg:none).
 * Os grupos devem: expiracao curta + refresh, assinatura forte, rejeitar alg:none,
 * invalidar no logout/troca de senha.
 */
public class JwtUtil {

    // V-05: mesmo segredo hardcoded
    private static final String SECRET = "vidaplus-secret-123";

    public static String gerarToken(Long usuarioId, String papel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("papel", papel);
        // V-07: nenhum setExpiration()
        return Jwts.builder()
                .setSubject(String.valueOf(usuarioId))
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * V-07: valida sem checar expiracao; parseClaimsJws com segredo fixo.
     * Retorna o id do usuario ou null.
     */
    public static Long usuarioIdDoToken(String token) {
        try {
            Claims body = Jwts.parser().setSigningKey(SECRET)
                    .parseClaimsJws(token).getBody();
            return Long.valueOf(body.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public static String papelDoToken(String token) {
        try {
            Claims body = Jwts.parser().setSigningKey(SECRET)
                    .parseClaimsJws(token).getBody();
            return (String) body.get("papel");
        } catch (Exception e) {
            return null;
        }
    }
}
