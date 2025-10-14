package br.gov.mma.facial.repository;

import br.gov.mma.facial.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para logs de auditoria
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Busca logs por usuário
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Busca logs por ação
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Busca logs por recurso
     */
    Page<AuditLog> findByResourceContainingIgnoreCase(String resource, Pageable pageable);

    /**
     * Busca logs por resultado
     */
    Page<AuditLog> findByResult(String result, Pageable pageable);

    /**
     * Busca logs por categoria
     */
    Page<AuditLog> findByCategory(br.gov.mma.facial.enums.EventCategory category, Pageable pageable);

    /**
     * Buscar logs por nível de severidade
     */
    List<AuditLog> findBySeverity(br.gov.mma.facial.enums.SeverityLevel severity);

    /**
     * Busca logs por período
     */
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * Busca logs por usuário e período
     */
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUserIdAndTimestampBetween(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Busca logs de autenticação por usuário
     */
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.category = 'AUTHENTICATION' ORDER BY al.timestamp DESC")
    List<AuditLog> findAuthenticationLogsByUser(@Param("userId") Long userId);

    /**
     * Busca tentativas de login falhadas
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = 'LOGIN' AND al.result = 'FAILURE' AND al.timestamp > :since")
    List<AuditLog> findFailedLoginAttemptsSince(@Param("since") LocalDateTime since);

    /**
     * Busca logs de acesso a dados sensíveis
     */
    @Query("SELECT al FROM AuditLog al WHERE al.category = 'DATA_ACCESS' AND al.severity IN ('WARNING', 'CRITICAL')")
    Page<AuditLog> findSensitiveDataAccessLogs(Pageable pageable);

    /**
     * Busca logs por IP
     */
    List<AuditLog> findByIpAddress(String ipAddress);

    /**
     * Busca logs por correlation ID
     */
    List<AuditLog> findByCorrelationId(String correlationId);

    /**
     * Conta logs por categoria e período
     */
    @Query("SELECT al FROM AuditLog al WHERE al.category = :category AND al.timestamp BETWEEN :startTime AND :endTime")
    List<AuditLog> findByCategoryAndTimeRange(@Param("category") br.gov.mma.facial.enums.EventCategory category,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * Busca logs de eventos críticos
     */
    @Query("SELECT al FROM AuditLog al WHERE al.severity = 'CRITICAL' ORDER BY al.timestamp DESC")
    List<AuditLog> findCriticalEvents();

    /**
     * Remove logs antigos para limpeza
     */
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    void deleteLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}