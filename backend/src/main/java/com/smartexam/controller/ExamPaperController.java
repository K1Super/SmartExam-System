package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.ExamPaper;
import com.smartexam.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    @PutMapping("/{id}/publish")
    public Result<ExamPaper> publish(@PathVariable Long id) {
        ExamPaper publishedExamPaper = examPaperService.publish(id);
        return Result.success("发布试卷成功", publishedExamPaper);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperService.delete(id);
        return Result.success("删除试卷成功");
    }
}
