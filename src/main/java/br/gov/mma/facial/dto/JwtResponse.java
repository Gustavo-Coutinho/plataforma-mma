package br.gov.mma.facial.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para resposta de autenticação JWT
 */
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private Long userId;
    private String nome;
    private String email;
    private String matricula;
    private String orgao;
    private List<String> roles;
    private LocalDateTime expiresAt;
    private LocalDateTime lastLogin;

    // Constructors
    public JwtResponse() {}

    public JwtResponse(String token, String refreshToken, Long userId, String nome, String email, 
                      String matricula, String orgao, List<String> roles, LocalDateTime expiresAt) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.orgao = orgao;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    // Getters e Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getOrgao() { return orgao; }
    public void setOrgao(String orgao) { this.orgao = orgao; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}