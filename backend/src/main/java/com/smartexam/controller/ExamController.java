package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.Exam;
import com.smartexam.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exams")
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    @GetMapping
    public Result<List<Exam>> findAll() {
        List<Exam> exams = examService.findAll();
        return Result.success("获取考试列表成功", exams);
    }
    
    @GetMapping("/subject/{subjectId}")
    public Result<List<Exam>> findBySubject(@PathVariable Long subjectId) {
        List<Exam> exams = examService.findBySubject(subjectId);
        return Result.success("获取考试列表成功", exams);
    }
    
    @GetMapping("/status/{status}")
    public Result<List<Exam>> findByStatus(@PathVariable Integer status) {
        List<Exam> exams = examService.findByStatus(status);
        return Result.success("获取考试列表成功", exams);
    }
    
    @GetMapping("/current")
    public Result<List<Exam>> findCurrentExams() {
        List<Exam> exams = examService.findCurrentExams();
        return Result.success("获取当前考试列表成功", exams);
    }
    
    @GetMapping("/{id}")
    public Result<Exam> findById(@PathVariable Long id) {
        Exam exam = examService.findById(id);
        return Result.success("获取考试信息成功", exam);
    }
    
    @PostMapping
    public Result<Exam> create(@RequestBody Exam exam) {
        Exam createdExam = examService.create(exam);
        return Result.success("创建考试成功", createdExam);
    }
    
    @PutMapping("/{id}")
    public Result<Exam> update(@PathVariable Long id, @RequestBody Exam exam) {
        exam.setId(id);
        Exam updatedExam = examService.update(exam);
        return Result.success("更新考试成功", updatedExam);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.success("删除考试成功");
    }
    
    @PutMapping("/update-status")
    public Result<Void> updateExamStatus() {
        examService.updateExamStatus();
        return Result.success("更新考试状态成功");
    }
}
