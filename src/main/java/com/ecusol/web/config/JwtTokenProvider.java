package com.ecusol.web.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Value("${jwt.expiration-ms}")
    private long validityInMilliseconds;

    public String createToken(String username, Integer usuarioWebId, Integer clienteIdCore) {

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(username)
                .claim("clienteIdCore", clienteIdCore)
                .claim("usuarioWebId", usuarioWebId)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            System.out.println("JWT expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT no soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT malformado: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Firma JWT inválida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Token vacío o nulo: " + e.getMessage());
        }

        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getClienteIdCore(String token) {
        return getClaims(token).get("clienteIdCore", Integer.class);
    }

    public Integer getUsuarioWebId(String token) {
        return getClaims(token).get("usuarioWebId", Integer.class);
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }
}
