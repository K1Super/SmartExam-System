package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.ExamPaper;
import com.smartexam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam-papers")
public class ExamPaperController {
    
    @Autowired
    private ExamPaperService examPaperService;
    
    @GetMapping
    public Result<List<ExamPaper>> findAll() {
        List<ExamPaper> examPapers = examPaperService.findAll();
        return Result.success("获取试卷列表成功", examPapers);
    }
    
    @GetMapping("/subject/{subjectId}")
    public Result<List<ExamPaper>> findBySubject(@PathVariable Long subjectId) {
        List<ExamPaper> examPapers = examPaperService.findBySubject(subjectId);
        return Result.success("获取试卷列表成功", examPapers);
    }
    
    @GetMapping("/status/{status}")
    public Result<List<ExamPaper>> findByStatus(@PathVariable Integer status) {
        List<ExamPaper> examPapers = examPaperService.findByStatus(status);
        return Result.success("获取试卷列表成功", examPapers);
    }
    
    @GetMapping("/{id}")
    public Result<ExamPaper> findById(@PathVariable Long id) {
        ExamPaper examPaper = examPaperService.findById(id);
        return Result.success("获取试卷信息成功", examPaper);
    }
    
    @PostMapping
    public Result<ExamPaper> create(@RequestBody ExamPaper examPaper) {
        ExamPaper createdExamPaper = examPaperService.create(examPaper);
        return Result.success("创建试卷成功", createdExamPaper);
    }
    
    @PutMapping("/{id}")
    public Result<ExamPaper> update(@PathVariable Long id, @RequestBody ExamPaper examPaper) {
        examPaper.setId(id);
        ExamPaper updatedExamPaper = examPaperService.update(examPaper);
        return Result.success("更新试卷成功", updatedExamPaper);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperService.delete(id);
        return Result.success("删除试卷成功");
    }
    
    @PostMapping("/{id}/questions")
    public Result<Void> addQuestions(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        List<Long> questionIds = request.get("questionIds");
        examPaperService.addQuestions(id, questionIds);
        return Result.success("添加题目成功");
    }
    
    @GetMapping("/{id}/questions")
    public Result<List<Long>> getQuestions(@PathVariable Long id) {
        List<Long> questionIds = examPaperService.getQuestions(id);
        return Result.success("获取试卷题目成功", questionIds);
    }
    
    @DeleteMapping("/{id}/questions/{questionId}")
    public Result<Void> removeQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        examPaperService.removeQuestion(id, questionId);
        return Result.success("移除题目成功");
    }
}
