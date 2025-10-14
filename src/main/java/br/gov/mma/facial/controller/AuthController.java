package br.gov.mma.facial.controller;

import br.gov.mma.facial.dto.BiometricLoginRequest;
import br.gov.mma.facial.dto.FaceEnrollmentRequest;
import br.gov.mma.facial.dto.JwtResponse;
import br.gov.mma.facial.dto.LoginRequest;
import br.gov.mma.facial.dto.RegisterRequest;
import br.gov.mma.facial.dto.ApiResponse;
import br.gov.mma.facial.dto.PendingRegistrationRequest;
import br.gov.mma.facial.dto.CompleteFaceRegistrationRequest;
import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.security.BiometricAuthenticationToken;
import br.gov.mma.facial.security.UserDetailsImpl;
import br.gov.mma.facial.service.BiometricService;
import br.gov.mma.facial.service.UserService;
import br.gov.mma.facial.service.PendingRegistrationService;
import br.gov.mma.facial.util.JwtUtils;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para endpoints de autenticação
 * Gerencia login tradicional, biométrico e cadastro facial
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final BiometricService biometricService;
    private final PasswordEncoder passwordEncoder;
    private final PendingRegistrationService pendingRegistrationService;
    
    // JWT expiration time in milliseconds (24 hours)
    private final long jwtExpirationMs = 86400000;

    public AuthController(AuthenticationManager authenticationManager,
                         JwtUtils jwtUtils,
                         UserService userService,
                         BiometricService biometricService,
                         PasswordEncoder passwordEncoder,
                         PendingRegistrationService pendingRegistrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.biometricService = biometricService;
        this.passwordEncoder = passwordEncoder;
        this.pendingRegistrationService = pendingRegistrationService;
    }

    /**
     * Login tradicional (fallback)
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Tentativa de login tradicional para: {}", loginRequest.getEmailOrMatricula());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmailOrMatricula(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

            // Atualizar último login
            User user = userService.findById(userDetails.getId());
            user.setLastLogin(LocalDateTime.now());
            user.resetFailedAttempts();
            userService.save(user);

            // Generate refresh token and expiration time
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
            LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);
            
            JwtResponse response = new JwtResponse(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getMatricula(),
                userDetails.getOrgao(),
                roles,
                expirationTime
            );

            logger.info("Login tradicional bem-sucedido para usuário: {}", userDetails.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            logger.warn("Falha no login tradicional para: {} - {}", loginRequest.getEmailOrMatricula(), e.getMessage());
            
            // Incrementar tentativas falhadas
            try {
                User user = userService.findByEmailOrMatricula(loginRequest.getEmailOrMatricula());
                user.incrementFailedAttempts();
                if (user.getFailedAttempts() >= 10) {
                    user.lockAccount();
                    logger.warn("Conta bloqueada por excesso de tentativas: {}", user.getEmail());
                }
                userService.save(user);
            } catch (Exception ex) {
                logger.debug("Usuário não encontrado para incrementar tentativas: {}", loginRequest.getEmailOrMatricula());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Credenciais inválidas"));
        }
    }

    /**
     * STEP 1: Initial registration - validates credentials and creates pending session
     * Does NOT save user to database yet
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("[REGISTER-STEP1] Initial registration request for: {}", registerRequest.getEmail());

        try {
            // Validar se as senhas coincidem
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("As senhas não coincidem"));
            }

            // Verificar se email já existe
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email já cadastrado"));
            }

            // Verificar se matrícula já existe
            if (userService.existsByMatricula(registerRequest.getMatricula())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Matrícula já cadastrada"));
            }

            // Create pending registration session (NOT saved to DB yet)
            String sessionToken = pendingRegistrationService.createPendingRegistration(
                registerRequest.getNome(),
                registerRequest.getEmail(),
                registerRequest.getMatricula(),
                registerRequest.getOrgao(),
                registerRequest.getPassword()
            );

            logger.info("[REGISTER-STEP1] Pending registration created for: {} with session: {}", 
                       registerRequest.getEmail(), sessionToken);

            // Return session token to client so they can complete registration with facial enrollment
            return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                    "success", true,
                    "message", "Por favor, capture sua biometria facial para completar o cadastro",
                    "sessionToken", sessionToken,
                    "expiresIn", 600 // 10 minutes in seconds
                ));

        } catch (Exception e) {
            logger.error("[REGISTER-STEP1] Erro ao criar sessão de registro: {}", registerRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao iniciar cadastro: " + e.getMessage()));
        }
    }

    /**
     * STEP 2: Complete registration with facial biometric enrollment
     * Only after successful enrollment, user is saved to database
     */
    @PostMapping("/register/complete-face")
    public ResponseEntity<?> completeRegistrationWithFace(@Valid @RequestBody CompleteFaceRegistrationRequest request) {
        logger.info("[REGISTER-STEP2] Completing registration with facial enrollment for session: {}", request.getSessionToken());

        try {
            // Retrieve pending registration
            PendingRegistrationRequest pending = pendingRegistrationService.getPendingRegistration(request.getSessionToken());
            
            if (pending == null) {
                logger.warn("[REGISTER-STEP2] Pending registration not found or expired: {}", request.getSessionToken());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Sessão de registro inválida ou expirada. Por favor, reinicie o cadastro."));
            }

            logger.info("[REGISTER-STEP2] Found pending registration for: {}", pending.getEmail());

            // NOW create and save user to database FIRST (required for biometric enrollment)
            User newUser = userService.registerNewUser(
                pending.getNome(),
                pending.getEmail(),
                pending.getMatricula(),
                pending.getOrgao(),
                pending.getPassword()
            );

            // Enroll face and save biometric template
            logger.info("[REGISTER-STEP2] Enrolling facial biometric for user: {}", newUser.getEmail());
            boolean biometricSaved = biometricService.enrollUserFace(
                newUser,
                request.getFaceImagesBase64(),
                false // Don't replace existing (first enrollment)
            );

            if (!biometricSaved) {
                logger.error("[REGISTER-STEP2] Failed to save biometric data for user: {}", newUser.getId());
                // Rollback user creation
                userService.delete(newUser);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha no processamento da biometria facial. Por favor, tente novamente com melhor iluminação."));
            }

            logger.info("[REGISTER-STEP2] Facial biometric enrolled successfully for: {}", newUser.getEmail());

            // Complete and remove pending registration
            pendingRegistrationService.completePendingRegistration(request.getSessionToken());

            logger.info("[REGISTER-STEP2] User registered successfully with facial biometric: {}", newUser.getEmail());

            // Auto-login after successful registration
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    newUser.getEmail(),
                    pending.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
            LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);

            JwtResponse response = new JwtResponse(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getMatricula(),
                userDetails.getOrgao(),
                roles,
                expirationTime
            );

            logger.info("[REGISTER-STEP2] User registered, authenticated and logged in: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("[REGISTER-STEP2] Erro ao completar registro com biometria facial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao completar cadastro: " + e.getMessage()));
        }
    }

    /**
     * Login biométrico facial
     */
    @PostMapping("/login-face")
    public ResponseEntity<?> authenticateBiometric(@Valid @RequestBody BiometricLoginRequest biometricRequest) {
        logger.info("Tentativa de login biométrico - Session: {}", biometricRequest.getSessionId());

        try {
            // Validar que a imagem facial foi fornecida
            if (biometricRequest.getFaceImageBase64() == null || biometricRequest.getFaceImageBase64().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Imagem facial é obrigatória"));
            }

            // Processar imagem facial e extrair template
            byte[] faceTemplate = biometricService.extractFaceTemplate(biometricRequest.getFaceImageBase64());
            
            if (faceTemplate == null) {
                logger.warn("Falha na extração do template facial - Session: {}", biometricRequest.getSessionId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha no processamento da imagem facial"));
            }

            // Verificar liveness se habilitado
            if (Boolean.TRUE.equals(biometricRequest.getEnableLivenessCheck())) {
                boolean isLive = biometricService.verifyLiveness(biometricRequest.getFaceImageBase64());
                if (!isLive) {
                    logger.warn("Falha na verificação de liveness - Session: {}", biometricRequest.getSessionId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Falha na verificação de vitalidade"));
                }
            }

            // Identificar usuário pelo template biométrico
            User user = biometricService.identifyUser(faceTemplate);
            
            if (user == null) {
                logger.warn("Usuário não identificado biometricamente - Session: {}", biometricRequest.getSessionId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Usuário não identificado"));
            }

            // Se credenciais foram fornecidas, validar que correspondem ao usuário identificado
            if (biometricRequest.getEmailOrMatricula() != null && !biometricRequest.getEmailOrMatricula().isEmpty()) {
                String providedCredential = biometricRequest.getEmailOrMatricula().trim();
                boolean credentialsMatch = providedCredential.equalsIgnoreCase(user.getEmail()) || 
                                          providedCredential.equalsIgnoreCase(user.getMatricula());
                
                if (!credentialsMatch) {
                    logger.warn("Credenciais fornecidas ({}) não correspondem ao usuário identificado biometricamente ({}/{}) - Session: {}", 
                        providedCredential, user.getEmail(), user.getMatricula(), biometricRequest.getSessionId());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("As credenciais fornecidas não correspondem à identidade facial detectada"));
                }

                // Validar senha se fornecida
                if (biometricRequest.getPassword() != null && !biometricRequest.getPassword().isEmpty()) {
                    if (!passwordEncoder.matches(biometricRequest.getPassword(), user.getPasswordHash())) {
                        logger.warn("Senha incorreta para usuário {} identificado biometricamente - Session: {}", 
                            user.getEmail(), biometricRequest.getSessionId());
                        user.incrementFailedAttempts();
                        userService.save(user);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.error("Senha incorreta"));
                    }
                }
                
                logger.info("Validação de credenciais bem-sucedida para login biométrico: {} - Session: {}", 
                    user.getEmail(), biometricRequest.getSessionId());
            }

            // Verificar se a conta está bloqueada
            if (Boolean.TRUE.equals(user.getAccountLocked())) {
                logger.warn("Tentativa de login em conta bloqueada: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Conta temporariamente bloqueada"));
            }

            // Criar token de autenticação biométrica
            BiometricAuthenticationToken biometricToken = 
                new BiometricAuthenticationToken(user.getEmail(), faceTemplate);

            Authentication authentication = authenticationManager.authenticate(biometricToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

            // Atualizar último login
            user.setLastLogin(LocalDateTime.now());
            user.resetFailedAttempts();
            user.setTotalLogins((user.getTotalLogins() != null ? user.getTotalLogins() : 0) + 1);
            userService.save(user);

            // Generate refresh token and expiration time
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
            LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);

            JwtResponse response = new JwtResponse(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getMatricula(),
                userDetails.getOrgao(),
                roles,
                expirationTime
            );

            logger.info("Login biométrico bem-sucedido para usuário: {} - Session: {}", 
                user.getEmail(), biometricRequest.getSessionId());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            logger.warn("Falha na autenticação biométrica - Session: {} - {}", 
                biometricRequest.getSessionId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Falha na autenticação biométrica"));
        } catch (Exception e) {
            logger.error("Erro interno na autenticação biométrica - Session: {}", 
                biometricRequest.getSessionId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno no servidor"));
        }
    }

    /**
     * Cadastro de biometria facial
     */
    @PostMapping("/enroll-face")
    public ResponseEntity<?> enrollFace(@Valid @RequestBody FaceEnrollmentRequest enrollmentRequest) {
        logger.info("Tentativa de cadastro biométrico para usuário: {}", enrollmentRequest.getUserId());

        try {
            // Verificar se o usuário existe
            User user = userService.findById(Long.valueOf(enrollmentRequest.getUserId()));
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuário não encontrado"));
            }

            // Processar múltiplas imagens para criar template robusto
            boolean enrollmentSuccess = biometricService.enrollUserFace(
                user, 
                enrollmentRequest.getFaceImagesBase64(),
                Boolean.TRUE.equals(enrollmentRequest.getReplaceExisting())
            );

            if (enrollmentSuccess) {
                logger.info("Cadastro biométrico realizado com sucesso para usuário: {}", user.getEmail());
                return ResponseEntity.ok(ApiResponse.success("Cadastro biométrico realizado com sucesso", null));
            } else {
                logger.warn("Falha no cadastro biométrico para usuário: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha no processamento das imagens faciais"));
            }

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("ID de usuário inválido"));
        } catch (Exception e) {
            logger.error("Erro interno no cadastro biométrico para usuário: {}", 
                enrollmentRequest.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno no servidor"));
        }
    }

    /**
     * Renovar token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    User user = userService.findByEmailOrMatricula(username);
                    
                    UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    
                    String newJwt = jwtUtils.generateJwtToken(authentication);
                    
                    List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());

                    JwtResponse response = new JwtResponse();
                    response.setToken(newJwt);
                    response.setUserId(userDetails.getId());
                    response.setNome(userDetails.getUsername());
                    response.setEmail(userDetails.getEmail());
                    response.setMatricula(userDetails.getMatricula());
                    response.setOrgao(userDetails.getOrgao());
                    response.setRoles(roles);

                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token inválido"));
                
        } catch (Exception e) {
            logger.error("Erro ao renovar token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno no servidor"));
        }
    }

    /**
     * Reset de senha com autenticação biométrica
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody BiometricLoginRequest resetRequest) {
        logger.info("Tentativa de reset de senha biométrico - Email/Matrícula: {}", resetRequest.getEmailOrMatricula());

        try {
            // Validar que email ou matrícula foi fornecido
            if (resetRequest.getEmailOrMatricula() == null || resetRequest.getEmailOrMatricula().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email ou matrícula é obrigatório"));
            }

            // Validar que nova senha foi fornecida
            if (resetRequest.getPassword() == null || resetRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Nova senha é obrigatória"));
            }

            // Validar comprimento mínimo da senha
            if (resetRequest.getPassword().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("A senha deve ter no mínimo 6 caracteres"));
            }

            User requestedUser;

            // Se userId foi fornecido, significa que já verificamos a identidade na etapa anterior
            if (resetRequest.getMetadata() != null && resetRequest.getMetadata().containsKey("userId")) {
                Long userId = Long.valueOf(resetRequest.getMetadata().get("userId").toString());
                requestedUser = userService.findById(userId);
                
                if (requestedUser == null) {
                    logger.warn("Usuário não encontrado para reset - userId: {}", userId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuário não encontrado"));
                }

                // Validar que o email/matrícula corresponde ao userId fornecido
                String providedCredential = resetRequest.getEmailOrMatricula().trim();
                boolean credentialsMatch = providedCredential.equalsIgnoreCase(requestedUser.getEmail()) || 
                                          providedCredential.equalsIgnoreCase(requestedUser.getMatricula());
                
                if (!credentialsMatch) {
                    logger.warn("Credenciais fornecidas ({}) não correspondem ao userId verificado ({})", 
                        providedCredential, userId);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Erro na validação de identidade"));
                }

                logger.info("Usando userId pré-verificado para reset: {}", userId);
            } else {
                // Fluxo legado: verificação biométrica completa
                // Processar imagem facial e extrair template
                byte[] faceTemplate = biometricService.extractFaceTemplate(resetRequest.getFaceImageBase64());
                
                if (faceTemplate == null) {
                    logger.warn("Falha na extração do template facial para reset - Email/Matrícula: {}", 
                        resetRequest.getEmailOrMatricula());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Falha no processamento da imagem facial"));
                }

                // Identificar usuário pelo template biométrico
                User identifiedUser = biometricService.identifyUser(faceTemplate);
                
                if (identifiedUser == null) {
                    logger.warn("Usuário não identificado biometricamente para reset - Email/Matrícula fornecido: {}", 
                        resetRequest.getEmailOrMatricula());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Identidade facial não reconhecida"));
                }

                // Buscar usuário pelas credenciais fornecidas
                requestedUser = userService.findByEmailOrMatricula(resetRequest.getEmailOrMatricula().trim());
                
                if (requestedUser == null) {
                    logger.warn("Usuário não encontrado para reset - Email/Matrícula: {}", 
                        resetRequest.getEmailOrMatricula());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuário não encontrado"));
                }

                // Validar que a face identificada corresponde ao usuário solicitado
                if (!identifiedUser.getId().equals(requestedUser.getId())) {
                    logger.warn("Face identificada ({}/{}) não corresponde ao usuário solicitado ({}/{}) para reset", 
                        identifiedUser.getEmail(), identifiedUser.getMatricula(),
                        requestedUser.getEmail(), requestedUser.getMatricula());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("A identidade facial não corresponde ao usuário informado"));
                }
            }

            // Atualizar senha do usuário com a nova senha fornecida
            requestedUser.setPasswordHash(passwordEncoder.encode(resetRequest.getPassword()));
            userService.save(requestedUser);
            
            logger.info("Senha resetada com sucesso biometricamente para usuário: {} - Session: {}", 
                requestedUser.getEmail(), resetRequest.getSessionId());

            // Retornar resposta de sucesso
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("email", requestedUser.getEmail());
            responseData.put("message", "Senha alterada com sucesso. Você já pode fazer login com a nova senha.");

            return ResponseEntity.ok(ApiResponse.success("Senha alterada com sucesso", responseData));

        } catch (Exception e) {
            logger.error("Erro ao resetar senha biometricamente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno no servidor"));
        }
    }

    /**
     * Endpoint de status para verificar saúde da API
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(ApiResponse.success("API de autenticação operacional", null));
    }
}