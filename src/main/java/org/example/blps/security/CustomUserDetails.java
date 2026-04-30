package org.example.blps.security;
import org.example.blps.entity.User;
import org.example.blps.enums.Privilege;
import org.example.blps.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record CustomUserDetails(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Privilege> privileges;
        if (user.getRole() == Role.ADMIN && user().isSuperUser()) {
            privileges = EnumSet.allOf(Privilege.class);
        }
        else {
            privileges = user.getRole().getPrivileges();
        }
        return privileges.stream().map(privilege -> new SimpleGrantedAuthority(privilege.name())).toList();
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
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
