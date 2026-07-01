package com.github_stale.repo_clean.service;

import com.github_stale.repo_clean.config.AppConfig.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final AppProperties appProperties;
    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = appProperties.getTokenSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateApprovalToken(Long branchId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(branchId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(appProperties.getTokenExpiryDays(), ChronoUnit.DAYS)))
                .claim("type", "APPROVAL")
                .signWith(signingKey).compact();
    }

    public Long validateAndExtractBranchId(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                    .parseClaimsJws(token).getBody();
            if (!"APPROVAL".equals(claims.get("type")))
                return null;
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.warn("Token expired");
            return null;
        } catch (JwtException | NumberFormatException e) {
            log.warn("Invalid token");
            return null;
        }
    }
}
