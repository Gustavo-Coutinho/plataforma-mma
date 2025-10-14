package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de usuários
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuário por matrícula
     */
    Optional<User> findByMatricula(String matricula);
    
    /**
     * Busca usuário por username (usando matrícula como username)
     */
    default Optional<User> findByUsername(String username) {
        return findByEmailOrMatricula(username);
    }

    /**
     * Busca usuário por email ou matrícula
     */
    @Query("SELECT u FROM User u WHERE u.email = :emailOrMatricula OR u.matricula = :emailOrMatricula")
    Optional<User> findByEmailOrMatricula(@Param("emailOrMatricula") String emailOrMatricula);

    /**
     * Verifica se email já existe
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se matrícula já existe
     */
    boolean existsByMatricula(String matricula);

    /**
     * Busca usuários por status
     */
    List<User> findByStatus(br.gov.mma.facial.enums.StatusUsuario status);

    /**
     * Busca usuários por órgão
     */
    Page<User> findByOrgaoContainingIgnoreCase(String orgao, Pageable pageable);

    /**
     * Busca usuários com templates biométricos
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.faceTemplates ft WHERE ft.isPrimary = true")
    List<User> findUsersWithBiometricTemplates();

    /**
     * Busca usuários por role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Busca usuários bloqueados
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    List<User> findLockedUsers();

    /**
     * Busca usuários com falhas recentes de login
     */
    @Query("SELECT u FROM User u WHERE u.failedAttempts > :maxAttempts")
    List<User> findUsersWithFailedAttempts(@Param("maxAttempts") Integer maxAttempts);

    /**
     * Atualiza último login do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Reset tentativas de login falhadas
     */
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.accountLocked = false, u.lockTime = null WHERE u.id = :userId")
    void resetFailedAttempts(@Param("userId") Long userId);

    /**
     * Incrementa tentativas de login falhadas
     */
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.id = :userId")
    void incrementFailedAttempts(@Param("userId") Long userId);

    /**
     * Bloqueia conta do usuário
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.lockTime = :lockTime WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * Busca usuários criados após uma data
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Conta usuários por role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Conta usuários por status de bloqueio
     */
    Long countByAccountLocked(Boolean accountLocked);
    
    /**
     * Conta usuários que fizeram login após uma data
     */
    Long countByLastLoginAfter(LocalDateTime date);
}