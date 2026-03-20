package com.be_mxh.config.security.jwt;

import com.be_mxh.service.UserService;
import com.be_mxh.service.impl.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JWTService jwtService;

    // Sử dụng @Lazy ở constructor để giải quyết lỗi Circular Dependency
    public JWTAuthenticationFilter(@Lazy UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            // CHỈ validate nếu chuỗi trông có vẻ là JWT (có 2 dấu chấm)
            if (jwt != null && countPeriods(jwt) == 2 && jwtService.validateJwtToken(jwt)) {
                String username = jwtService.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Can NOT set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }
    // Hàm phụ trợ để đếm dấu chấm, tránh lỗi parsing của thư viện jjwt
    private int countPeriods(String token) {
        int count = 0;
        for (char c : token.toCharArray()) {
            if (c == '.') count++;
        }
        return count;
    }

    /**
     * Trích xuất JWT từ header Authorization
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Cách viết chuẩn hơn thay vì replace
        }
        return null;
    }
}