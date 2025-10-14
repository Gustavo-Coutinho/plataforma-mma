package br.gov.mma.facial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for pending registration - stores user data before facial enrollment
 */
public class PendingRegistrationRequest {
    
    private String sessionToken;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Matrícula é obrigatória")
    @Size(min = 3, max = 20, message = "Matrícula deve ter entre 3 e 20 caracteres")
    private String matricula;

    @NotBlank(message = "Órgão é obrigatório")
    @Size(min = 3, max = 100, message = "Órgão deve ter entre 3 e 100 caracteres")
    private String orgao;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;
    
    private LocalDateTime expiresAt;

    // Constructors
    public PendingRegistrationRequest() {}

    public PendingRegistrationRequest(String sessionToken, String nome, String email, String matricula, String orgao, String password) {
        this.sessionToken = sessionToken;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.orgao = orgao;
        this.password = password;
        this.expiresAt = LocalDateTime.now().plusMinutes(10); // 10 minute expiration
    }

    // Getters and Setters
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getOrgao() { return orgao; }
    public void setOrgao(String orgao) { this.orgao = orgao; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
