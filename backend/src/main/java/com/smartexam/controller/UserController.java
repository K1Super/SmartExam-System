package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.User;
import com.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public Result<List<User>> findAll(
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Integer role) {
        List<User> users = userService.findByConditions(clazzId, role);
        return Result.success("获取用户列表成功", users);
    }
    
    @GetMapping("/role/{role}")
    public Result<List<User>> findByRole(@PathVariable Integer role) {
        List<User> users = userService.findByRole(role);
        return Result.success("获取用户列表成功", users);
    }
    
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        return Result.success("获取用户信息成功", user);
    }
    
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        User createdUser = userService.create(user);
        return Result.success("创建用户成功", createdUser);
    }
    
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.update(user);
        return Result.success("更新用户成功", updatedUser);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除用户成功");
    }
    
    @PostMapping("/reset-student-passwords")
    public Result<Void> resetStudentPasswords() {
        userService.resetAllStudentPasswords();
        return Result.success("重置所有学生密码成功，默认密码为123456");
    }
}
