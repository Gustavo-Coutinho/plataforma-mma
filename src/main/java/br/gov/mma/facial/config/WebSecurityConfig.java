package br.gov.mma.facial.config;

import br.gov.mma.facial.security.AuthEntryPointJwt;
import br.gov.mma.facial.security.AuthTokenFilter;
import br.gov.mma.facial.security.BiometricAuthenticationProvider;
import br.gov.mma.facial.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança do Spring Security
 * Define autenticação biométrica e baseada em JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }



    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService,
                                                             PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig,
                                                        DaoAuthenticationProvider daoAuthenticationProvider,
                                                        BiometricAuthenticationProvider biometricAuthenticationProvider) throws Exception {
        var authManager = authConfig.getAuthenticationManager();
        // Ensure both providers are registered
        if (authManager instanceof org.springframework.security.authentication.ProviderManager providerManager) {
            var providers = new java.util.ArrayList<>(providerManager.getProviders());
            if (!providers.contains(daoAuthenticationProvider)) {
                providers.add(daoAuthenticationProvider);
            }
            if (!providers.contains(biometricAuthenticationProvider)) {
                providers.add(biometricAuthenticationProvider);
            }
            return new org.springframework.security.authentication.ProviderManager(providers);
        }
        return authManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider daoAuthenticationProvider,
                                           BiometricAuthenticationProvider biometricAuthenticationProvider,
                                           CorsConfigurationSource corsConfigurationSource,
                                           AuthTokenFilter authTokenFilter) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/api/auth/login", "/api/auth/login-face", "/api/auth/register", "/api/auth/register/complete-face", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/health", "/actuator/health").permitAll()
                .requestMatchers("/", "/index.html", "/register", "/register.html", "/dashboard", "/dashboard.html", "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // Endpoints públicos de dados (ROLE_PERFIL_1)
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                // Endpoints para diretores (ROLE_PERFIL_2)
                .requestMatchers("/api/diretores/**").hasAnyAuthority("ROLE_PERFIL_2", "ROLE_MINISTRO")

                // Endpoints exclusivos do ministro (ROLE_MINISTRO)
                .requestMatchers("/api/ministerio/**").hasAuthority("ROLE_MINISTRO")

                // Endpoints de enrollment biométrico (requer autenticação)
                .requestMatchers("/api/auth/enroll-face").authenticated()

                // Endpoints de auditoria (acesso restrito)
                .requestMatchers("/api/audit/**").hasAnyAuthority("ROLE_PERFIL_2", "ROLE_MINISTRO")

                // Actuator endpoints
                .requestMatchers("/actuator/**").hasAuthority("ROLE_MINISTRO")

                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            );

        // Configurar provedores de autenticação
        http.authenticationProvider(daoAuthenticationProvider);
        http.authenticationProvider(biometricAuthenticationProvider);

        // Adicionar filtro JWT
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-origins}") String allowedOrigins,
            @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-methods}") String allowedMethods,
            @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-headers}") String allowedHeaders,
            @org.springframework.beans.factory.annotation.Value("${app.security.cors.allow-credentials}") boolean allowCredentials) {

        CorsConfiguration configuration = new CorsConfiguration();

        // Parse comma-separated properties from application.yml (or environment)
    List<String> origins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // Use origin patterns so values like http://localhost:* or production domains work
        configuration.setAllowedOriginPatterns(origins);

        List<String> methods = Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedMethods(methods);

        // If allowedHeaders is a single '*' keep it as wildcard, otherwise split
        if ("*".equals(allowedHeaders.trim())) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.stream(allowedHeaders.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            configuration.setAllowedHeaders(headers);
        }

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}