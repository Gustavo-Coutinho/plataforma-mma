package br.gov.mma.facial.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import br.gov.mma.facial.security.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Utilitário para geração e validação de tokens JWT
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private int jwtRefreshExpirationMs;

    /**
     * Gera token JWT a partir da autenticação
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        logger.info("[JWT-GEN] Starting JWT token generation for user: {}", userPrincipal.getUsername());

        try {
            SecretKey key = getSigningKey();
            logger.info("[JWT-GEN] Successfully obtained signing key for user: {}", userPrincipal.getUsername());
        } catch (Exception e) {
            logger.error("[JWT-GEN] FAILED to obtain signing key for user: {}. Exception: {}", userPrincipal.getUsername(), e.getMessage(), e);
            throw e;
        }

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("nome", userPrincipal.getNome())
                .claim("matricula", userPrincipal.getMatricula())
                .claim("orgao", userPrincipal.getOrgao())
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("authMethod", "PASSWORD") // ou "BIOMETRIC"
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Gera token JWT para autenticação biométrica
     */
    public String generateBiometricJwtToken(UserDetailsImpl userDetails) {
        logger.info("[JWT-BIO] Starting biometric JWT token generation for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("nome", userDetails.getNome())
                .claim("matricula", userDetails.getMatricula())
                .claim("orgao", userDetails.getOrgao())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("authMethod", "BIOMETRIC")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Gera refresh token
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtém username do token JWT
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Obtém user ID do token JWT
     */
    public Long getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    /**
     * Obtém claims do token
     */
    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Valida token JWT
     */
    public boolean validateJwtToken(String authToken) {
        logger.info("[JWT-VALIDATE] Starting token validation (token prefix: {}...)", 
                    authToken != null && authToken.length() > 20 ? authToken.substring(0, 20) : "<invalid>");
        try {
            SecretKey key = getSigningKey();
            logger.info("[JWT-VALIDATE] Obtained signing key, parsing token...");
            
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken);
            
            logger.info("[JWT-VALIDATE] ✓ Token validation succeeded");
            return true;
        } catch (io.jsonwebtoken.io.DecodingException e) {
            logger.error("[JWT-VALIDATE] ✗ Token JWT decode error (possibly bad signing key or token encoding): {}", e.getMessage(), e);
        } catch (MalformedJwtException e) {
            logger.error("[JWT-VALIDATE] ✗ Token JWT inválido (malformed): {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("[JWT-VALIDATE] ✗ Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("[JWT-VALIDATE] ✗ Token JWT não suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("[JWT-VALIDATE] ✗ Claims JWT vazio: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("[JWT-VALIDATE] ✗ Unexpected exception during validation: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Verifica se token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromJwtToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Obtém data de expiração do token
     */
    public LocalDateTime getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromJwtToken(token);
        return claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Verifica se é um refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromJwtToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém método de autenticação do token
     */
    public String getAuthMethodFromToken(String token) {
        try {
            Claims claims = getClaimsFromJwtToken(token);
            return claims.get("authMethod", String.class);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private SecretKey getSigningKey() {
        // Build a short masked fingerprint for safer logging (don't log the secret itself)
        String masked = jwtSecret == null ? "<null>" :
                (jwtSecret.length() <= 8 ? "****" : jwtSecret.substring(0,4) + "..." + jwtSecret.substring(jwtSecret.length()-4));
        logger.info("[KEY-RESOLVE] Starting JWT signing key resolution (secret length={} masked={})", jwtSecret == null ? 0 : jwtSecret.length(), masked);
        
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("[KEY-RESOLVE] JWT secret is null or empty! Cannot generate signing key.");
            throw new IllegalStateException("JWT secret not configured");
        }

        // Try common encodings in order: base64url -> base64 -> raw UTF-8 -> SHA-256-derived key
        try {
            logger.info("[KEY-RESOLVE] Step 1: Attempting BASE64URL decode (allows '-' and '_') for secret masked={}", masked);
            byte[] keyBytes = Decoders.BASE64URL.decode(jwtSecret);
            logger.info("[KEY-RESOLVE] ✓ BASE64URL decode succeeded! Key bytes length={}", keyBytes.length);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("[KEY-RESOLVE] ✓ Successfully created HMAC signing key from BASE64URL");
            return key;
        } catch (Exception e1) {
            logger.warn("[KEY-RESOLVE] ✗ BASE64URL decode failed: {} - {}", e1.getClass().getSimpleName(), e1.getMessage());
            logger.debug("[KEY-RESOLVE] BASE64URL exception stack:", e1);
            
            try {
                logger.info("[KEY-RESOLVE] Step 2: Attempting standard BASE64 decode (rejects '-' and '_') for secret masked={}", masked);
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                logger.info("[KEY-RESOLVE] ✓ BASE64 decode succeeded! Key bytes length={}", keyBytes.length);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);
                logger.info("[KEY-RESOLVE] ✓ Successfully created HMAC signing key from BASE64");
                return key;
            } catch (Exception e2) {
                logger.warn("[KEY-RESOLVE] ✗ BASE64 decode also failed: {} - {}", e2.getClass().getSimpleName(), e2.getMessage());
                logger.debug("[KEY-RESOLVE] BASE64 exception stack:", e2);
                
                logger.warn("[KEY-RESOLVE] Step 3: JWT secret is not base64/base64url encoded. Falling back to UTF-8 bytes with SHA-256 derivation.");
                byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
                logger.info("[KEY-RESOLVE] UTF-8 byte array length={}", keyBytes.length);
                
                if (keyBytes.length < 32) {
                    logger.info("[KEY-RESOLVE] Key bytes < 32, deriving 256-bit key via SHA-256 hash");
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        keyBytes = digest.digest(keyBytes);
                        logger.info("[KEY-RESOLVE] ✓ SHA-256 derived key bytes length={}", keyBytes.length);
                    } catch (NoSuchAlgorithmException ex) {
                        logger.error("[KEY-RESOLVE] ✗ CRITICAL: SHA-256 algorithm not available: {}", ex.getMessage());
                        throw new IllegalStateException("SHA-256 not available", ex);
                    }
                }
                
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);
                logger.info("[KEY-RESOLVE] ✓ Successfully created HMAC signing key from UTF-8/SHA-256 fallback");
                return key;
            }
        }
    }
}