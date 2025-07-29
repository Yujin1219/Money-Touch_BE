package com.server.money_touch.global.config.jwt;


import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.domain.user.entity.CustomUserDetails;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.properties.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;


@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    /**
     * Access Token과 Refresh Token을 함께 생성
     */
    public TokenResponse generateTokens(Authentication authentication) {
        // ✅ CustomUserDetails로 다운캐스팅
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String email = userDetails.getUsername(); // 또는 userDetails.getEmail()
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        Long userId = userDetails.getId(); // ✅ userId 추출

        Date now = new Date();

        // ✅ Access Token 생성 (userId 포함)
        String accessToken = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId) // ✅ userId 클레임 추가
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getExpiration().getAccess()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getExpiration().getRefresh()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration().getAccess() / 1000) // 초 단위
                .tokenType("Bearer")
                .build();
    }
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature is invalid: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인 (만료된 토큰도 파싱 가능)
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Request에서 토큰 추출
     */
    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰에서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();

        // 1. DB에서 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. CustomUserDetails 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(user, "");

        // 3. Authentication 객체 반환
        return new UsernamePasswordAuthenticationToken(customUserDetails, token, customUserDetails.getAuthorities());
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 토큰에서 역할 추출
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    /*
    * 토큰에서 claims 객체 추출
    * */
    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*
    *  토큰에서 userId 추출
    * */
    public Long extractUserId(String token){
        Claims claims = extractAllClaims(token); // claims 객체 추출
        return claims.get("userId", Long.class); // "userID" 클레임에서 값을 추출
    }

    /**
     * Request에서 Authentication 추출
     */
    public Authentication extractAuthentication(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        if (accessToken == null || !validateToken(accessToken)) {
            return null; // null 반환으로 변경
        }
        return getAuthentication(accessToken);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            return null; // null 반환으로 변경
        }

        String email = getEmailFromToken(refreshToken);
        String role = getRoleFromToken(refreshToken);
        Date now = new Date();

        // 새로운 Access Token 생성
        String newAccessToken = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getExpiration().getAccess()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 유지
                .expiresIn(jwtProperties.getExpiration().getAccess() / 1000)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 토큰 만료 시간 조회
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }
}