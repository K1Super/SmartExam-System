package com.smartexam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartexam.common.Result;
import com.smartexam.entity.User;
import com.smartexam.mapper.ExamMapper;
import com.smartexam.mapper.ExamPaperMapper;
import com.smartexam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long studentCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("role", 3)
            );
            stats.put("studentCount", studentCount);
        } catch (Exception e) {
            stats.put("studentCount", 0);
        }
        
        try {
            long teacherCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("role", 2)
            );
            stats.put("teacherCount", teacherCount);
        } catch (Exception e) {
            stats.put("teacherCount", 0);
        }
        
        try {
            long examCount = examMapper.selectCount(null);
            stats.put("examCount", examCount);
        } catch (Exception e) {
            stats.put("examCount", 0);
        }
        
        try {
            long paperCount = examPaperMapper.selectCount(null);
            stats.put("paperCount", paperCount);
        } catch (Exception e) {
            stats.put("paperCount", 0);
        }
        
        return Result.success("获取统计数据成功", stats);
    }
}
