package com.military.assetmanagement.security;

import com.military.assetmanagement.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final Long baseId;
    private final String baseName;
    private final boolean enabled;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();

        this.role = user.getRole() != null
                ? user.getRole().name()
                : null;

        this.baseId = user.getBase() != null
                ? user.getBase().getId()
                : null;

        this.baseName = user.getBase() != null
                ? user.getBase().getName()
                : null;

        this.enabled = user.isEnabled();
    }

    /**
     * Returns the database ID of the authenticated user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the username of the authenticated user.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Returns the encrypted password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the application role.
     *
     * Example:
     * ADMIN
     * BASE_COMMANDER
     * LOGISTICS_OFFICER
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the ID of the base assigned to the user.
     *
     * ADMIN users can have null baseId because they can
     * access all bases.
     */
    public Long getBaseId() {
        return baseId;
    }

    /**
     * Returns the name of the assigned base.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Returns Spring Security authorities.
     *
     * Spring Security expects the ROLE_ prefix when
     * using hasRole()/hasAnyRole().
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (role == null || role.isBlank()) {
            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    /**
     * Account expiration check.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Account lock check.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Credential expiration check.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account enabled/disabled check.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}