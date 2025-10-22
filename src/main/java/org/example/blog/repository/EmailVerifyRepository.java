package org.example.blog.repository;

import org.example.blog.entity.EmailVerify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerifyRepository extends JpaRepository<EmailVerify, Long> {
    boolean existsByEmailAndCreatedAtAfter(String email, LocalDateTime after);
    Optional<EmailVerify> findByEmailAndCodeAndExpireAtAfter(String email, String code, LocalDateTime now);
    void deleteByEmail(String email);   // 清理历史验证码
}