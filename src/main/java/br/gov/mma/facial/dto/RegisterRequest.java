package br.gov.mma.facial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro de novo usuário
 */
public class RegisterRequest {

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

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

    // Constructors
    public RegisterRequest() {
    }

    public RegisterRequest(String nome, String email, String matricula, String orgao, String password, String confirmPassword) {
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.orgao = orgao;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getOrgao() {
        return orgao;
    }

    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
