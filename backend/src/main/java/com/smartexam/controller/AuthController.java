package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.User;
import com.smartexam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        System.out.println("\n>>> 登录请求: username=" + username);
        
        try {
            Map<String, Object> result = authService.login(username, password);
            System.out.println(">>> 登录成功");
            return Result.success("登录成功", result);
        } catch (Exception e) {
            System.out.println(">>> 登录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User registeredUser = authService.register(user);
        return Result.success("注册成功", registeredUser);
    }
    
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return Result.success("获取用户信息成功", user);
    }
}
