package com.be_mxh.config.security;

import com.be_mxh.entity.User;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public final class SecurityUtils {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}