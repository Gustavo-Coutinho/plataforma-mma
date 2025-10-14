package br.gov.mma.facial.security;

import br.gov.mma.facial.entity.Role;
import br.gov.mma.facial.entity.User;
import br.gov.mma.facial.enums.StatusUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementação de {@link UserDetails} que expõe atributos do {@link User} para o Spring Security.
 */
public class UserDetailsImpl implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String email;
    private final String matricula;
    private final String orgao;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;

    private UserDetailsImpl(Long id,
                            String nome,
                            String email,
                            String matricula,
                            String orgao,
                            String password,
                            Collection<? extends GrantedAuthority> authorities,
                            boolean accountNonLocked,
                            boolean enabled) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.orgao = orgao;
        this.password = password;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles()
            .stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        String password = user.getPasswordHash() != null ? user.getPasswordHash() : "";
        boolean nonLocked = !Boolean.TRUE.equals(user.getAccountLocked());
        boolean enabled = user.getStatus() == StatusUsuario.ATIVO;

        return new UserDetailsImpl(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getMatricula(),
            user.getOrgao(),
            password,
            authorities,
            nonLocked,
            enabled
        );
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getOrgao() {
        return orgao;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return matricula;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDetailsImpl that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
