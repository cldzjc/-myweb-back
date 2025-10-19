package org.example.blog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.example.blog.common.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;        // ← 改成 jakarta
import jakarta.servlet.ServletException;  // ← 改成 jakarta
import jakarta.servlet.http.HttpServletRequest;  // ← 改成 jakarta
import jakarta.servlet.http.HttpServletResponse; // ← 改成 jakarta
import java.io.IOException;
import java.util.ArrayList;

public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (JwtUtil.validateToken(token)) {
                    String username = JwtUtil.getUsernameFromToken(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                logger.error("JWT 解析失败", e);
            }
        }
        chain.doFilter(request, response);
    }
}