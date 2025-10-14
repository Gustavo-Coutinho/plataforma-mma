package br.gov.mma.facial.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import br.gov.mma.facial.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = String.format("%s-%s", request.getMethod(), request.getRequestURI());
        logger.info("[AUTH-FILTER] Processing request: {}", requestId);
        
        try {
            String jwt = parseJwt(request);
            if (jwt == null) {
                logger.info("[AUTH-FILTER] No JWT found in Authorization header for {}", requestId);
                logger.info("[AUTH-FILTER] Request will proceed without authentication (may be public endpoint)");
            } else {
                String jwtPrefix = jwt.length() > 30 ? jwt.substring(0, 30) + "..." : jwt;
                logger.info("[AUTH-FILTER] JWT presented for {} (prefix: {})", requestId, jwtPrefix);
                
                logger.info("[AUTH-FILTER] Validating JWT for {}...", requestId);
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                
                if (isValid) {
                    logger.info("[AUTH-FILTER] ✓ JWT is valid for {}, extracting username...", requestId);
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.info("[AUTH-FILTER] Extracted username: {} for {}", username, requestId);

                    logger.info("[AUTH-FILTER] Loading user details for username: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("[AUTH-FILTER] ✓ Authentication set successfully for user: {} on {}", username, requestId);
                } else {
                    logger.warn("[AUTH-FILTER] ✗ JWT validation failed for {}, authentication NOT set", requestId);
                }
            }
        } catch (Exception e) {
            logger.error("[AUTH-FILTER] ✗ Exception during authentication for {}: {}", requestId, e.getMessage(), e);
        }

        logger.info("[AUTH-FILTER] Proceeding with filter chain for {}", requestId);
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}