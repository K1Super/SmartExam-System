package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartexam.entity.User;
import com.smartexam.mapper.UserMapper;
import com.smartexam.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> login(String username, String password) {
        try {
            // 获取用户信息
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if (user == null) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 验证用户状态，只有 status=0 表示禁用，null或1都表示正常
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new RuntimeException("账户已被禁用，请联系管理员");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }
    
    public User register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        
        userMapper.insert(user);
        return user;
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        String username = authentication.getName();
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }
    
    @PostConstruct
    public void initAdminUser() {
        System.out.println("\n>>> 初始化管理员账户...");
        User existingAdmin = userMapper.selectOne(new QueryWrapper<User>().eq("username", "admin"));
        
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setRole(1);
            admin.setStatus(1);
            userMapper.insert(admin);
            System.out.println(">>> 管理员账户创建成功: admin / 123456");
        } else {
            // 直接重置密码为123456
            existingAdmin.setPassword(passwordEncoder.encode("123456"));
            existingAdmin.setStatus(1);
            userMapper.updateById(existingAdmin);
            System.out.println(">>> 管理员密码已重置为: 123456");
            System.out.println(">>> 加密后密码: " + existingAdmin.getPassword());
        }
    }
}
