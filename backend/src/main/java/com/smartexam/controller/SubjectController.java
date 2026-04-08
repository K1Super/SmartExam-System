package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.Subject;
import com.smartexam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SubjectController {
    
    @Autowired
    private SubjectService subjectService;
    
    @GetMapping
    public Result<List<Subject>> findAll() {
        List<Subject> subjects = subjectService.findAll();
        return Result.success("获取科目列表成功", subjects);
    }
    
    @GetMapping("/{id}")
    public Result<Subject> findById(@PathVariable Long id) {
        Subject subject = subjectService.findById(id);
        return Result.success("获取科目信息成功", subject);
    }
    
    @PostMapping
    public Result<Subject> create(@RequestBody Subject subject) {
        Subject createdSubject = subjectService.create(subject);
        return Result.success("创建科目成功", createdSubject);
    }
    
    @PutMapping("/{id}")
    public Result<Subject> update(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        Subject updatedSubject = subjectService.update(subject);
        return Result.success("更新科目成功", updatedSubject);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return Result.success("删除科目成功");
    }
}
