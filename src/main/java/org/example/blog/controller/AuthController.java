package org.example.blog.controller;

import org.example.blog.common.Result;
import org.example.blog.common.JwtUtil;
import org.example.blog.dto.EmailDTO;
import org.example.blog.entity.User;
import org.example.blog.repository.UserRepository;
import org.example.blog.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody EmailDTO dto) {
        try {
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
                return new Result<>(400, "邮箱不能为空", null);
            }

            // 调用邮件服务发送验证码
            emailService.sendVerificationCode(dto.getEmail());
            return new Result<>(200, "验证码已发送", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(500, "发送失败：" + e.getMessage(), null);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        try {
            if (user.getEmail() == null || user.getPassword() == null || user.getUsername() == null) {
                return new Result<>(400, "注册信息不完整", null);
            }
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return new Result<>(400, "邮箱已被注册", null);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getRole() == null) {
                user.setRole("ROLE_USER");
            }

            userRepository.save(user);
            return new Result<>(200, "注册成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(500, "注册异常：" + e.getMessage(), null);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User loginUser) {
        try {
            if (loginUser.getEmail() == null || loginUser.getPassword() == null) {
                return new Result<>(400, "邮箱和密码不能为空", null);
            }

            User user = userRepository.findByEmail(loginUser.getEmail());
            if (user == null) {
                return new Result<>(400, "用户不存在", null);
            }

            if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
                return new Result<>(400, "密码错误", null);
            }

            // 使用 email 生成 Token（更稳定）
            String token = JwtUtil.generateToken(user.getEmail());

            // 返回 token 和用户基本信息
            return new Result<>(200, "登录成功",
                    new LoginResponse(token, user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(500, "登录异常：" + e.getMessage(), null);
        }
    }

    /**
     * 内部类：登录返回数据结构
     */
    static class LoginResponse {
        public String token;
        public String username;
        public String email;

        public LoginResponse(String token, String username, String email) {
            this.token = token;
            this.username = username;
            this.email = email;
        }
    }
}
