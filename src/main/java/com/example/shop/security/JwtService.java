
package com.example.shop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

  private final Key key;
  private final String issuer;
  private final long expirationMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.issuer = issuer;
    this.expirationMinutes = expirationMinutes;
  }

  public String generateToken(String username, Collection<String> roles) {
    Instant now = Instant.now();
    Date iat = Date.from(now);
    Date exp = Date.from(now.plusSeconds(expirationMinutes * 60));

    return Jwts.builder()
        .setSubject(username)
        .setIssuer(issuer)
        .setIssuedAt(iat)
        .setExpiration(exp)
        .claim("roles", roles)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .requireIssuer(issuer)
        .build()
        .parseClaimsJws(token);
  }

  public List<String> extractRoles(Claims claims) {
    Object raw = claims.get("roles");
    if (raw instanceof Collection) {
      return ((Collection<?>) raw).stream().map(String::valueOf).collect(Collectors.toList());
    }
    if (raw instanceof String s) {
      return Arrays.stream(s.split(",")).map(String::trim).toList();
    }
    return List.of();
  }
}
