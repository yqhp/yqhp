package com.yqhp.auth.web.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

/**
 * @author jiangyitao
 */
public class JWTToken {

    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String create(String subject, Duration expiration) {
        return Jwts.builder()
                .signWith(KEY)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
                .compact();
    }

    public static String parseSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(KEY).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
