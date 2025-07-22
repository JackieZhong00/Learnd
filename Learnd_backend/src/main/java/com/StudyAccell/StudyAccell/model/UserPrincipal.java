package com.StudyAccell.StudyAccell.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final User user;
    public UserPrincipal(User user) {
        this.user = user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //not sure what this method is supposed to do
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //implement later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //implement later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //implement later
    }

    @Override
    public boolean isEnabled() {
        return true; //implement later
    }
}
