package com.be_mxh.config.security;

import com.be_mxh.config.security.jwt.CustomAccessDeniedHandler;
import com.be_mxh.config.security.jwt.JWTAuthenticationFilter;
import com.be_mxh.config.security.jwt.RestAuthenticationEntryPoint;
import com.be_mxh.repository.RoleRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.UserService;
import com.be_mxh.service.impl.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JWTAuthenticationFilter jwtAuthenticationFilter;

  // Chỉ cần Inject những cái thực sự dùng trong Config này
  @Autowired
  public SecurityConfig(
          @Lazy UserService userService,
          PasswordEncoder passwordEncoder,
          JWTAuthenticationFilter jwtAuthenticationFilter
  ) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean(BeanIds.AUTHENTICATION_MANAGER)
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    // 1. Truyền userService (UserDetailsService) vào ngay khi khởi tạo
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
    // 2. setPasswordEncoder thì vẫn có hàm setter bình thường
    provider.setPasswordEncoder(passwordEncoder);

    return provider;
  }

  // CORS Configuration giữ nguyên của bạn
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://127.0.0.1:5000", "http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            // Cấu hình STATELESS cho JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/favicon.ico", "/static/**", "/css/**", "/js/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/app-visits/record").permitAll()
                    .anyRequest().authenticated()
            )
            // CHỈ dùng 1 dòng này để đăng ký Filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
