package com.franklintju.streamlab.auth;

import com.franklintju.streamlab.users.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {
    private final JwtConfig jwtConfig;

    public Jwt generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }

    public Jwt generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration());
    }

    private Jwt generateToken(User user, long tokenExpiration) {
        var now = new Date();
        var expiration = new Date(now.getTime() + 1000 * tokenExpiration);

        var jwt = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiration)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .signWith(jwtConfig.getSecretKey())
                .compact();

        var claims = Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
