package br.gov.mma.facial.controller;

import br.gov.mma.facial.dto.ApiResponse;
import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.repository.FaceTemplateRepository;
import br.gov.mma.facial.repository.UserRepository;
import br.gov.mma.facial.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para gerenciamento de usuários e dados do dashboard
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final FaceTemplateRepository faceTemplateRepository;

    public UserController(UserRepository userRepository, FaceTemplateRepository faceTemplateRepository) {
        this.userRepository = userRepository;
        this.faceTemplateRepository = faceTemplateRepository;
    }

    /**
     * Get current user statistics (ROLE_PERFIL_1+)
     */
    @GetMapping("/me/stats")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_1', 'ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getCurrentUserStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Optional<User> userOpt = userRepository.findById(userDetails.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error("Usuário não encontrado"));
            }
            
            User user = userOpt.get();
            Long templateCount = faceTemplateRepository.countByUser(user);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTemplates", templateCount);
            stats.put("lastLogin", user.getLastLogin());
            stats.put("totalLogins", user.getTotalLogins() != null ? user.getTotalLogins() : 0);
            stats.put("accountCreated", user.getCreatedAt());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting user stats", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar estatísticas"));
        }
    }

    /**
     * Get all users (ROLE_PERFIL_2+ only)
     */
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            // Map to DTO to avoid exposing sensitive data
            List<Map<String, Object>> userDTOs = users.stream().map(user -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", user.getId());
                dto.put("nome", user.getNome());
                dto.put("email", user.getEmail());
                dto.put("matricula", user.getMatricula());
                dto.put("orgao", user.getOrgao());
                dto.put("accountLocked", user.getAccountLocked());
                dto.put("lastLogin", user.getLastLogin());
                dto.put("createdAt", user.getCreatedAt());
                dto.put("totalLogins", user.getTotalLogins() != null ? user.getTotalLogins() : 0);
                
                // Get role names
                List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName().toString())
                    .collect(Collectors.toList());
                dto.put("roles", roleNames);
                
                return dto;
            }).collect(Collectors.toList());
            
            logger.info("Retrieved {} users for admin view", userDTOs.size());
            return ResponseEntity.ok(userDTOs);
            
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar usuários"));
        }
    }

    /**
     * Get user by ID (ROLE_PERFIL_2+ only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PERFIL_2', 'ROLE_MINISTRO')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error("Usuário não encontrado"));
            }
            
            User user = userOpt.get();
            Long templateCount = faceTemplateRepository.countByUser(user);
            
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("nome", user.getNome());
            userDetails.put("email", user.getEmail());
            userDetails.put("matricula", user.getMatricula());
            userDetails.put("orgao", user.getOrgao());
            userDetails.put("accountLocked", user.getAccountLocked());
            userDetails.put("failedAttempts", user.getFailedAttempts());
            userDetails.put("lastLogin", user.getLastLogin());
            userDetails.put("createdAt", user.getCreatedAt());
            userDetails.put("totalLogins", user.getTotalLogins() != null ? user.getTotalLogins() : 0);
            userDetails.put("totalTemplates", templateCount);
            
            List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());
            userDetails.put("roles", roleNames);
            
            return ResponseEntity.ok(userDetails);
            
        } catch (Exception e) {
            logger.error("Error getting user by ID", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Erro ao buscar usuário"));
        }
    }
}
