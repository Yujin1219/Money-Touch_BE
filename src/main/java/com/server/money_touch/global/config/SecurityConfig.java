package com.server.money_touch.global.config;

import com.server.money_touch.global.config.jwt.JwtAuthenticationFilter;
import com.server.money_touch.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // ✅ WebMvcConfigurer(WebConfig)의 CORS 설정을 반영하도록 허용
                .csrf((auth) -> auth.disable()) // 필요 시 CSRF 보호 비활성화
                .formLogin((auth) -> auth.disable()) // form 로그인 방식 disable
                .httpBasic((auth) -> auth.disable()) // http Basic 인증 방식 disable
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 STATELESS 상태로 설정
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/api/user/signup/local", "/api/user/login/local","/auth/login/kakao/**", // 로그인, 회원가입
                                "/", "/index.html", "/css/**", "/js/**", "/images/**", "/test", // Spring Boot의 정적 리소스 기본 경로
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", // Swaggger 문서
                                "/api/test/s3/upload") // 프로필 이미지 업로드
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 페이지 접근 제어
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//                .formLogin((form) -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/home", true)
//                        .permitAll()
//                )
//                .logout((logout) -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }
}
