package me.notej.notej_api.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {

    // 액세스 토큰 30분
    private static final long ACCESS_TOKEN_EXPIRE_MS  = 1000L * 60 * 30;
    // 리프레시 토큰 7일
    private static final long REFRESH_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24 * 7;

    @Value("${jwt.secret}")
    private String secret;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Access Token ---
    public String createToken(String memberUUID) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_MS);


        return Jwts.builder()
                .setSubject(memberUUID)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("type", "access")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        return validate(token, "access");
    }

    // --- Refresh Token ---
    public String createRefreshToken(String memberUUID) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_MS);

        return Jwts.builder()
                .setSubject(memberUUID)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, "refresh");
    }

    // 공통 검증 로직
    private boolean validate(String token, String type) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            if ("refresh".equals(type)) {
                String claimType = claims.getBody().get("type", String.class);
                if (!"refresh".equals(claimType)) {
                    log.error("[TokenProvider][validate] Not a refresh token");
                    return false;
                }
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.error("[TokenProvider][validate] {} Access token is expired", type);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[TokenProvider][validate] {} Access token invalid", type);
        }
        return false;
    }

    // 토큰에서 Authentication 추출 (액세스/리프레시 공통)
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        UserDetails user = new User(claims.getSubject(), "", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }

    /** 리프레시 토큰 만료 시간(ms) */
    public long getRefreshTokenExpiryMs() {
        return REFRESH_TOKEN_EXPIRE_MS;
    }

    /** 액세스 토큰 만료 시간(ms), 필요 시 추가 정의 */
    public long getAccessTokenExpiryMs() {
        return ACCESS_TOKEN_EXPIRE_MS;
    }
}

