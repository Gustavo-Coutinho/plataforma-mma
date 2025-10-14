package br.gov.mma.facial.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade para registro de auditoria do sistema
 * Armazena todas as ações relevantes para conformidade com LGPD
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_resource", columnList = "resource")
})
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID do usuário que executou a ação (pode ser null para ações do sistema)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Nome do usuário (armazenado para histórico, caso usuário seja removido)
     */
    @Size(max = 100)
    @Column(name = "user_name", length = 100)
    private String userName;

    /**
     * Matrícula do usuário
     */
    @Size(max = 20)
    @Column(name = "user_matricula", length = 20)
    private String userMatricula;

    /**
     * Ação executada
     */
    @NotBlank(message = "Ação é obrigatória")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * Recurso acessado/modificado
     */
    @NotBlank(message = "Recurso é obrigatório")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String resource;

    /**
     * Resultado da ação (SUCCESS, FAILURE, BLOCKED, etc.)
     */
    @NotBlank(message = "Resultado é obrigatório")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String result;

    /**
     * Detalhes adicionais da ação (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Endereço IP de origem
     */
    @Size(max = 45) // IPv6 support
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User Agent do cliente
     */
    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * ID de sessão
     */
    @Size(max = 100)
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * ID de correlação para rastrear requisições relacionadas
     */
    @Size(max = 50)
    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    /**
     * Nível de severidade do evento
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity = SeverityLevel.INFO;

    /**
     * Categoria do evento para classificação
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Constructors
    public AuditLog() {}

    public AuditLog(String action, String resource, String result, EventCategory category) {
        this.action = action;
        this.resource = resource;
        this.result = result;
        this.category = category;
    }

    // Builder pattern para facilitar criação
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private AuditLog auditLog = new AuditLog();

        public AuditLogBuilder user(Long userId, String userName, String userMatricula) {
            auditLog.userId = userId;
            auditLog.userName = userName;
            auditLog.userMatricula = userMatricula;
            return this;
        }

        public AuditLogBuilder action(String action) {
            auditLog.action = action;
            return this;
        }

        public AuditLogBuilder resource(String resource) {
            auditLog.resource = resource;
            return this;
        }

        public AuditLogBuilder result(String result) {
            auditLog.result = result;
            return this;
        }

        public AuditLogBuilder details(String details) {
            auditLog.details = details;
            return this;
        }

        public AuditLogBuilder request(String ipAddress, String userAgent, String sessionId) {
            auditLog.ipAddress = ipAddress;
            auditLog.userAgent = userAgent;
            auditLog.sessionId = sessionId;
            return this;
        }

        public AuditLogBuilder correlationId(String correlationId) {
            auditLog.correlationId = correlationId;
            return this;
        }

        public AuditLogBuilder severity(SeverityLevel severity) {
            auditLog.severity = severity;
            return this;
        }

        public AuditLogBuilder category(EventCategory category) {
            auditLog.category = category;
            return this;
        }

        public AuditLog build() {
            return auditLog;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserMatricula() { return userMatricula; }
    public void setUserMatricula(String userMatricula) { this.userMatricula = userMatricula; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public SeverityLevel getSeverity() { return severity; }
    public void setSeverity(SeverityLevel severity) { this.severity = severity; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", action='" + action + "'" +
                ", resource='" + resource + "'" +
                ", result='" + result + "'" +
                ", severity=" + severity +
                ", category=" + category +
                ", timestamp=" + timestamp +
                '}';
    }
}

/**
 * Níveis de severidade para eventos de auditoria
 */
enum SeverityLevel {
    INFO,    // Informações gerais
    WARNING, // Avisos, eventos suspeitos
    ERROR,   // Erros de sistema
    CRITICAL // Eventos críticos de segurança
}

/**
 * Categorias de eventos para classificação
 */
enum EventCategory {
    AUTHENTICATION,     // Eventos de autenticação
    AUTHORIZATION,      // Eventos de autorização
    DATA_ACCESS,        // Acesso a dados
    DATA_MODIFICATION,  // Modificação de dados
    BIOMETRIC,          // Eventos biométricos
    SYSTEM,             // Eventos de sistema
    SECURITY,           // Eventos de segurança
    LGPD_COMPLIANCE     // Eventos relacionados à LGPD
}