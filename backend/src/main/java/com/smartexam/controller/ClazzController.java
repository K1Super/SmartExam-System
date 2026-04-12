package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.Clazz;
import com.smartexam.entity.User;
import com.smartexam.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clazz")
public class ClazzController {
    
    @Autowired
    private ClazzService clazzService;
    
    @GetMapping
    public Result<List<Clazz>> findAll() {
        List<Clazz> clazzList = clazzService.findAll();
        return Result.success("获取班级列表成功", clazzList);
    }
    
    @GetMapping("/{id}")
    public Result<Clazz> findById(@PathVariable Long id) {
        Clazz clazz = clazzService.findById(id);
        return Result.success("获取班级信息成功", clazz);
    }
    
    @PostMapping
    public Result<Clazz> create(@RequestBody Clazz clazz) {
        Clazz createdClazz = clazzService.create(clazz);
        return Result.success("创建班级成功", createdClazz);
    }
    
    @PutMapping("/{id}")
    public Result<Clazz> update(@PathVariable Long id, @RequestBody Clazz clazz) {
        clazz.setId(id);
        Clazz updatedClazz = clazzService.update(clazz);
        return Result.success("更新班级成功", updatedClazz);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        clazzService.delete(id);
        return Result.success("删除班级成功");
    }
    
    @GetMapping("/{id}/students")
    public Result<List<User>> getStudents(@PathVariable Long id) {
        List<User> students = clazzService.getStudentsByClazzId(id);
        return Result.success("获取学生列表成功", students);
    }
    
    @PostMapping("/{id}/students")
    public Result<User> addStudent(@PathVariable Long id, @RequestBody User student) {
        User createdStudent = clazzService.addStudentToClazz(id, student);
        return Result.success("添加学生成功", createdStudent);
    }
    
    @PutMapping("/{clazzId}/students/{studentId}")
    public Result<User> updateStudent(@PathVariable Long clazzId, @PathVariable Long studentId, @RequestBody User student) {
        student.setId(studentId);
        User updatedStudent = clazzService.updateStudent(student);
        return Result.success("更新学生成功", updatedStudent);
    }
    
    @DeleteMapping("/{clazzId}/students/{studentId}")
    public Result<Void> removeStudent(@PathVariable Long clazzId, @PathVariable Long studentId) {
        clazzService.removeStudentFromClazz(clazzId, studentId);
        return Result.success("移除学生成功");
    }
}
