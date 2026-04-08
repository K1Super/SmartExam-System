package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.service.AutoGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/grading")
public class GradingController {
    
    @Autowired
    private AutoGradingService autoGradingService;
    
    @PostMapping("/auto/{examRecordId}")
    public Result<ExamRecord> autoGrade(@PathVariable Long examRecordId) {
        ExamRecord examRecord = autoGradingService.autoGrade(examRecordId);
        return Result.success("自动阅卷成功", examRecord);
    }
    
    @PostMapping("/manual/{answerRecordId}")
    public Result<AnswerRecord> manualGrade(
            @PathVariable Long answerRecordId, 
            @RequestParam BigDecimal score) {
        AnswerRecord answerRecord = autoGradingService.manualGrade(answerRecordId, score);
        return Result.success("手动阅卷成功", answerRecord);
    }
}
