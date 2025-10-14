package br.gov.mma.facial.service;

import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.entity.Role;
import br.gov.mma.facial.enums.StatusUsuario;
import br.gov.mma.facial.repository.UserRepository;
import br.gov.mma.facial.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço para gerenciamento de usuários
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca usuário por ID
     */
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        logger.warn("Usuário não encontrado com ID: {}", id);
        return null;
    }

    /**
     * Busca usuário por email ou matrícula
     */
    public User findByEmailOrMatricula(String emailOrMatricula) {
        // Primeiro tenta buscar por email
        Optional<User> user = userRepository.findByEmail(emailOrMatricula);
        
        if (user.isPresent()) {
            return user.get();
        }

        // Se não encontrou por email, tenta por matrícula
        user = userRepository.findByMatricula(emailOrMatricula);
        
        if (user.isPresent()) {
            return user.get();
        }

        logger.warn("Usuário não encontrado com email/matrícula: {}", emailOrMatricula);
        return null;
    }

    /**
     * Busca usuário por email
     */
    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    /**
     * Busca usuário por matrícula
     */
    public User findByMatricula(String matricula) {
        Optional<User> user = userRepository.findByMatricula(matricula);
        return user.orElse(null);
    }

    /**
     * Busca usuário por username (pode ser email ou matrícula)
     */
    public User findByUsername(String username) {
        return findByEmailOrMatricula(username);
    }

    /**
     * Salva ou atualiza usuário
     */
    public User save(User user) {
        try {
            User savedUser = userRepository.save(user);
            logger.info("Usuário salvo/atualizado: {}", savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            logger.error("Erro ao salvar usuário: {}", user.getEmail(), e);
            throw new RuntimeException("Erro ao salvar usuário", e);
        }
    }

    /**
     * Registra novo usuário com hash de senha automático
     * Usa REQUIRES_NEW para garantir commit imediato antes do auto-login
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public User registerNewUser(String nome, String email, String matricula, String orgao, String plainPassword) {
        logger.info("Registrando novo usuário: {}", email);

        // Criar novo usuário
        User newUser = new User();
        newUser.setNome(nome);
        newUser.setEmail(email);
        newUser.setMatricula(matricula);
        newUser.setOrgao(orgao);
        
        // Hash da senha automaticamente
        String hashedPassword = passwordEncoder.encode(plainPassword);
        newUser.setPasswordHash(hashedPassword);
        
        newUser.setStatus(StatusUsuario.ATIVO);
        newUser.setAccountLocked(false);
        newUser.setFailedAttempts(0);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // Atribuir role padrão (ROLE_PERFIL_1)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_PERFIL_1")
            .orElseThrow(() -> new RuntimeException("Role ROLE_PERFIL_1 não encontrada"));
        roles.add(userRole);
        newUser.setRoles(roles);

        // Salvar usuário
        User savedUser = userRepository.save(newUser);
        logger.info("Usuário registrado com sucesso: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        
        return savedUser;
    }

    /**
     * Verifica se usuário existe por email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Verifica se usuário existe por matrícula
     */
    public boolean existsByMatricula(String matricula) {
        return userRepository.existsByMatricula(matricula);
    }

    /**
     * Deleta usuário (rollback durante registro com falha)
     */
    public void delete(User user) {
        userRepository.delete(user);
        logger.info("Usuário deletado: {} (ID: {})", user.getEmail(), user.getId());
    }

    /**
     * Ativa usuário
     */
    public void activateUser(Long userId) {
        User user = findById(userId);
        if (user != null) {
            user.setAccountLocked(false);
            user.resetFailedAttempts();
            save(user);
            logger.info("Usuário ativado: {}", user.getEmail());
        }
    }

    /**
     * Desativa usuário
     */
    public void deactivateUser(Long userId) {
        User user = findById(userId);
        if (user != null) {
            user.lockAccount();
            save(user);
            logger.info("Usuário desativado: {}", user.getEmail());
        }
    }

    /**
     * Reset tentativas de login falhas de um usuário
     */
    public void resetFailedAttempts(String emailOrMatricula) {
        User user = findByEmailOrMatricula(emailOrMatricula);
        if (user != null) {
            user.resetFailedAttempts();
            save(user);
            logger.info("Tentativas de login resetadas para usuário: {}", user.getEmail());
        }
    }

    /**
     * Incrementa tentativas de login falhas
     */
    public void incrementFailedAttempts(String emailOrMatricula) {
        User user = findByEmailOrMatricula(emailOrMatricula);
        if (user != null) {
            user.incrementFailedAttempts();
            
            // Bloqueia conta se exceder o limite
            if (user.getFailedAttempts() >= 10) {
                user.lockAccount();
                logger.warn("Conta bloqueada por excesso de tentativas: {}", user.getEmail());
            }
            
            save(user);
        }
    }

    /**
     * Verifica se usuário está bloqueado
     */
    public boolean isUserLocked(String emailOrMatricula) {
        User user = findByEmailOrMatricula(emailOrMatricula);
        return user != null && Boolean.TRUE.equals(user.getAccountLocked());
    }

    /**
     * Conta total de usuários
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Conta usuários ativos (não bloqueados)
     */
    public long countActiveUsers() {
        return userRepository.countByAccountLocked(false);
    }

    /**
     * Deleta usuário (soft delete - apenas marca como inativo)
     */
    public void deleteUser(Long userId) {
        User user = findById(userId);
        if (user != null) {
            user.lockAccount();
            save(user);
            logger.info("Usuário marcado como excluído: {}", user.getEmail());
        }
    }
}