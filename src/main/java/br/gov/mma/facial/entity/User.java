package br.gov.mma.facial.entity;

import br.gov.mma.facial.enums.StatusUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade representando um usuário do sistema
 * Contém informações pessoais e de autenticação
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "matricula")
       })
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Email(message = "Email deve ter formato válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Matrícula é obrigatória")
    @Size(min = 3, max = 20, message = "Matrícula deve ter entre 3 e 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @NotBlank(message = "Órgão é obrigatório")
    @Size(max = 200, message = "Órgão deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String orgao;

    /**
     * Hash da senha para fallback de autenticação
     * Usado apenas quando autenticação biométrica falha
     */
    @Column(length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusUsuario status = StatusUsuario.ATIVO;

    /**
     * Relacionamento Many-to-Many com roles
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * Templates biométricos do usuário
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FaceTemplate> faceTemplates = new HashSet<>();

    /**
     * Controle de tentativas de login
     */
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "total_logins")
    private Integer totalLogins = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String nome, String email, String matricula, String orgao) {
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.orgao = orgao;
    }

    // Métodos utilitários
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    public void addFaceTemplate(FaceTemplate template) {
        this.faceTemplates.add(template);
        template.setUser(this);
    }

    public void removeFaceTemplate(FaceTemplate template) {
        this.faceTemplates.remove(template);
        template.setUser(null);
    }

    /**
     * Incrementa tentativas de login falhadas
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    /**
     * Reset tentativas de login
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.accountLocked = false;
        this.lockTime = null;
    }

    /**
     * Bloqueia conta por excesso de tentativas
     */
    public void lockAccount() {
        this.accountLocked = true;
        this.lockTime = LocalDateTime.now();
    }

    /**
     * Verifica se conta está bloqueada
     */
    public boolean isAccountLocked() {
        if (!accountLocked) return false;

        // Auto-unlock após 30 minutos
        if (lockTime != null && lockTime.plusMinutes(30).isBefore(LocalDateTime.now())) {
            resetFailedAttempts();
            return false;
        }

        return true;
    }

    /**
     * Atualiza último login
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getOrgao() { return orgao; }
    public void setOrgao(String orgao) { this.orgao = orgao; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public StatusUsuario getStatus() { return status; }
    public void setStatus(StatusUsuario status) { this.status = status; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<FaceTemplate> getFaceTemplates() { return faceTemplates; }
    public void setFaceTemplates(Set<FaceTemplate> faceTemplates) { this.faceTemplates = faceTemplates; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public LocalDateTime getLockTime() { return lockTime; }
    public void setLockTime(LocalDateTime lockTime) { this.lockTime = lockTime; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public Integer getTotalLogins() { return totalLogins; }
    public void setTotalLogins(Integer totalLogins) { this.totalLogins = totalLogins; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nome='" + nome + "'" +
                ", email='" + email + "'" +
                ", matricula='" + matricula + "'" +
                ", orgao='" + orgao + "'" +
                ", status=" + status +
                '}';
    }
}

