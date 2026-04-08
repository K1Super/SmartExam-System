package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.Question;
import com.smartexam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping
    public Result<List<Question>> findAll() {
        List<Question> questions = questionService.findAll();
        return Result.success("获取题目列表成功", questions);
    }
    
    @GetMapping("/type/{type}")
    public Result<List<Question>> findByType(@PathVariable Integer type) {
        List<Question> questions = questionService.findByType(type);
        return Result.success("获取题目列表成功", questions);
    }
    
    @GetMapping("/subject/{subjectId}")
    public Result<List<Question>> findBySubject(@PathVariable Long subjectId) {
        List<Question> questions = questionService.findBySubject(subjectId);
        return Result.success("获取题目列表成功", questions);
    }
    
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Question>> findByDifficulty(@PathVariable Integer difficulty) {
        List<Question> questions = questionService.findByDifficulty(difficulty);
        return Result.success("获取题目列表成功", questions);
    }
    
    @GetMapping("/search")
    public Result<List<Question>> search(@RequestParam String keyword) {
        List<Question> questions = questionService.search(keyword);
        return Result.success("搜索题目成功", questions);
    }
    
    @GetMapping("/{id}")
    public Result<Question> findById(@PathVariable Long id) {
        Question question = questionService.findById(id);
        return Result.success("获取题目信息成功", question);
    }
    
    @PostMapping
    public Result<Question> create(@RequestBody Question question) {
        Question createdQuestion = questionService.create(question);
        return Result.success("创建题目成功", createdQuestion);
    }
    
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        Question updatedQuestion = questionService.update(question);
        return Result.success("更新题目成功", updatedQuestion);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除题目成功");
    }
}
