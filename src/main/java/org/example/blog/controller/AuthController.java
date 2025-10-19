package org.example.blog.controller;

import org.example.blog.common.Result;
import org.example.blog.entity.User;
import org.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.example.blog.common.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return new Result<>(400, "邮箱已被注册", null);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return new Result<>(200, "注册成功", null);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody User loginUser) {
        User user = userRepository.findByEmail(loginUser.getEmail());
        if (user == null) {
            return new Result<>(400, "用户不存在", null);
        }
        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return new Result<>(400, "密码错误", null);
        }
        String token = JwtUtil.generateToken(user.getUsername());
        return new Result<>(200, "登录成功", token);   // data 就是 JWT 字符串
    }
}

