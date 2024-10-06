package com.shubham.lightbill.lightbill_backend.configuration;

import com.shubham.lightbill.lightbill_backend.custumExceptions.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private Map<String, Object> extractCookies(Cookie[] cookies){
        Map<String, Object> cookiesData = new HashMap<>();
        if(cookies == null) return cookiesData;

        for(Cookie cookie : cookies){
            cookiesData.put(cookie.getName(), cookie.getValue());
        }
        return cookiesData;
    }

    private String  getBearerToken(Map<String, Object> extractedCookies, HttpServletRequest request){
        if(extractedCookies.containsKey("Bearer-token")) return extractedCookies.get("Bearer-token").toString();
        if(request.getHeader("Authorization") != null) return request.getHeader("Authorization");
        log.warn("No token found");
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> extractedCookies = extractCookies(request.getCookies());
        extractedCookies.forEach((key, value) -> System.out.println(key + " " + value));

        String userId = null, token = null, urlEncodedToken = getBearerToken(extractedCookies, request);
        if(urlEncodedToken != null){
            token = URLDecoder.decode(urlEncodedToken).substring(7);
            userId = jwtUtil.extractUserId(token);
        }


        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token)) {
                List<GrantedAuthority> authorities = jwtUtil.getGrantedAuthorityList(token);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
