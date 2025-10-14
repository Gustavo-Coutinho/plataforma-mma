package br.gov.mma.facial.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;

/**
 * Token de autenticação que carrega dados do template facial para validação biométrica.
 */
public class BiometricAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final byte[] probeTemplate;

    public BiometricAuthenticationToken(String username, byte[] probeTemplate) {
        super(null);
        Assert.hasText(username, "Username must not be empty");
        this.principal = username;
        this.probeTemplate = probeTemplate != null ? probeTemplate.clone() : null;
        setAuthenticated(false);
    }

    public BiometricAuthenticationToken(Object principal,
                                        Collection<? extends GrantedAuthority> authorities,
                                        byte[] probeTemplate) {
        super(authorities);
        this.principal = principal;
        this.probeTemplate = probeTemplate != null ? probeTemplate.clone() : null;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public byte[] getProbeTemplate() {
        return probeTemplate != null ? probeTemplate.clone() : null;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        if (probeTemplate != null) {
            Arrays.fill(probeTemplate, (byte) 0);
        }
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Use constructor with authorities to mark as authenticated");
        }
        super.setAuthenticated(false);
    }
}
