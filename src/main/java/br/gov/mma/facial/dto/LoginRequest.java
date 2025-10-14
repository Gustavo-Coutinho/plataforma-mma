package br.gov.mma.facial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de login tradicional (fallback)
 */
public class LoginRequest {

    @NotBlank(message = "Email ou matrícula é obrigatório")
    @Size(max = 150, message = "Email/matrícula deve ter no máximo 150 caracteres")
    private String emailOrMatricula;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String emailOrMatricula, String password) {
        this.emailOrMatricula = emailOrMatricula;
        this.password = password;
    }

    // Getters e Setters
    public String getEmailOrMatricula() { return emailOrMatricula; }
    public void setEmailOrMatricula(String emailOrMatricula) { this.emailOrMatricula = emailOrMatricula; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "emailOrMatricula='" + emailOrMatricula + "'" +
                '}';
    }
}