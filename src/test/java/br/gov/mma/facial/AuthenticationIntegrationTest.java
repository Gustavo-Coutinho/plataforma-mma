package br.gov.mma.facial;

import br.gov.mma.facial.entity.Role;
import br.gov.mma.facial.repository.RoleRepository;
import br.gov.mma.facial.service.BiometricService;
import br.gov.mma.facial.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @MockBean
    private BiometricService biometricService;

    @BeforeEach
    void setUp() {
        roleRepository.findByName(Role.ROLE_PERFIL_1).orElseGet(() -> {
            Role role = new Role();
            role.setName(Role.ROLE_PERFIL_1);
            role.setDescription("Perfil padrão de usuário");
            return roleRepository.save(role);
        });
    }

    @Test
    void userCanAuthenticateWithEncodedPassword() {
        String email = "integration.user@example.com";
        String matricula = "INT123456";
        String password = "Password123!";

        userService.registerNewUser(
            "Integration User",
            email,
            matricula,
            "MMA",
            password
        );

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isNotNull();

        UsernamePasswordAuthenticationToken tokenWithMatricula = new UsernamePasswordAuthenticationToken(matricula, password);
        Authentication matriculaAuth = authenticationManager.authenticate(tokenWithMatricula);
        assertThat(matriculaAuth.isAuthenticated()).isTrue();
    }
}
