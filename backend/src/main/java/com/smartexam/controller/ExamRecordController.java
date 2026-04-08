package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-records")
public class ExamRecordController {
    
    @Autowired
    private ExamRecordService examRecordService;
    
    @GetMapping("/user/{userId}")
    public Result<List<ExamRecord>> findByUserId(@PathVariable Long userId) {
        List<ExamRecord> examRecords = examRecordService.findByUserId(userId);
        return Result.success("获取用户考试记录成功", examRecords);
    }
    
    @GetMapping("/exam/{examId}")
    public Result<List<ExamRecord>> findByExamId(@PathVariable Long examId) {
        List<ExamRecord> examRecords = examRecordService.findByExamId(examId);
        return Result.success("获取考试记录成功", examRecords);
    }
    
    @PostMapping("/start")
    public Result<ExamRecord> startExam(@RequestParam Long examId, @RequestParam Long userId) {
        ExamRecord examRecord = examRecordService.startExam(examId, userId);
        return Result.success("开始考试成功", examRecord);
    }
    
    @PostMapping("/submit/{examRecordId}")
    public Result<ExamRecord> submitExam(@PathVariable Long examRecordId, @RequestBody List<AnswerRecord> answerRecords) {
        ExamRecord examRecord = examRecordService.submitExam(examRecordId, answerRecords);
        return Result.success("提交考试成功", examRecord);
    }
    
    @GetMapping("/answers/{examRecordId}")
    public Result<List<AnswerRecord>> getAnswerRecords(@PathVariable Long examRecordId) {
        List<AnswerRecord> answerRecords = examRecordService.getAnswerRecords(examRecordId);
        return Result.success("获取答题记录成功", answerRecords);
    }
}
