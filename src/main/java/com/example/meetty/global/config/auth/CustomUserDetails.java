package com.example.meetty.global.config.auth;

import com.example.meetty.auth.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@ToString
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final UserEntity userEntity;
    private Map<String, Object> attributes;

    // 일반 로그인
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.attributes = Collections.emptyMap();
    }

    // OAuth2 로그인
    public CustomUserDetails(UserEntity userEntity, Map<String, Object> attributes) {
        this.userEntity = userEntity;
        this.attributes = attributes;
    }

    public Long getUserId() {
        return userEntity.getUserId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        return userEntity.getProvider() != null ? String.valueOf(attributes.get("sub")) : userEntity.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userEntity.getRole().getType()));
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

