package com.blc.blc_backend.auth.token;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Firebase 인증 토큰을 Spring Security의 Authentication 객체로 래핑하는 클래스
 */
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;
    private final FirebaseToken firebaseToken;
    private final String token;

    /**
     * 인증되지 않은 토큰 생성자 (인증 전)
     */
    public FirebaseAuthenticationToken(String token) {
        super(null);
        this.principal = null;
        this.firebaseToken = null;
        this.token = token;
        setAuthenticated(false);
    }

    /**
     * 인증된 토큰 생성자 (인증 후)
     */
    public FirebaseAuthenticationToken(Object principal, FirebaseToken firebaseToken, 
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.firebaseToken = firebaseToken;
        this.token = null;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public FirebaseToken getFirebaseToken() {
        return firebaseToken;
    }

    public String getUid() {
        return firebaseToken != null ? firebaseToken.getUid() : null;
    }

    public String getEmail() {
        return firebaseToken != null ? firebaseToken.getEmail() : null;
    }

    public String getName() {
        return firebaseToken != null ? firebaseToken.getName() : null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}