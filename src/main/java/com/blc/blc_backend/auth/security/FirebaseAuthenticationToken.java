package com.blc.blc_backend.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class FirebaseAuthenticationToken implements Authentication {
    // Firebase Token 기반으로 변경
    private final String uid;
    private final String email;
    private final String name;
    private boolean authenticated = true;

    public FirebaseAuthenticationToken(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public FirebaseAuthenticationToken(String uid, String email) {
        this(uid, email, null);
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return uid;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return name != null ? name : email;
    }
}