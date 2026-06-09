package com.example.housetrading.controller;

import com.example.housetrading.common.Result;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.UserService;
import com.example.housetrading.service.MailService;
import com.example.housetrading.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private com.example.housetrading.mapper.PasswordResetTokenMapper tokenMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userService.getByUsername(loginUser.getUsername());
        if (user == null || !passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userService.getByEmail(email);
        if (user == null) {
            // we don't want to reveal if email exists for security reasons, 
            // but in this simple system we can just return success
            return Result.success();
        }

        String token = java.util.UUID.randomUUID().toString();
        com.example.housetrading.entity.PasswordResetToken resetToken = new com.example.housetrading.entity.PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setExpiryTime(java.time.LocalDateTime.now().plusMinutes(15));
        tokenMapper.insert(resetToken);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        mailService.sendResetPasswordMail(user.getEmail(), resetLink);

        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");

        com.example.housetrading.entity.PasswordResetToken resetToken = tokenMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.housetrading.entity.PasswordResetToken>()
                        .eq(com.example.housetrading.entity.PasswordResetToken::getToken, token)
        );

        if (resetToken == null || resetToken.getExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            return Result.error("链接已失效或不存在");
        }

        userService.updatePassword(resetToken.getUserId(), newPassword);
        tokenMapper.deleteById(resetToken.getId());

        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
}
