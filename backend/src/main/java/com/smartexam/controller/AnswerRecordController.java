package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.service.AnswerRecordService;
import com.smartexam.service.ExamRecordService;
import com.smartexam.vo.QuestionAnswerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/answer-records")
public class AnswerRecordController {
    
    @Autowired
    private ExamRecordService examRecordService;
    
    @Autowired
    private AnswerRecordService answerRecordService;
    
    @GetMapping
    public Result<List<AnswerRecord>> findAll() {
        // 这里可以根据需要实现查询所有答题记录的逻辑
        // 暂时返回空列表，因为通常我们是通过考试记录ID查询答题记录
        return Result.success("获取答题记录列表成功", List.of());
    }
    
    @GetMapping("/exam/{examRecordId}")
    public Result<List<QuestionAnswerVO>> getAnswerRecords(@PathVariable Long examRecordId) {
        List<QuestionAnswerVO> answerRecords = examRecordService.getAnswerRecords(examRecordId);
        return Result.success("获取答题记录成功", answerRecords);
    }
    
    @PostMapping("/update-score/{answerRecordId}")
    public Result<Void> updateScore(@PathVariable Long answerRecordId, @RequestBody ScoreUpdateRequest request) {
        answerRecordService.updateScore(answerRecordId, request.getScore(), request.getIsCorrect());
        return Result.success("更新分数成功", null);
    }
    
    static class ScoreUpdateRequest {
        private BigDecimal score;
        private Integer isCorrect;
        
        public BigDecimal getScore() {
            return score;
        }
        
        public void setScore(BigDecimal score) {
            this.score = score;
        }
        
        public Integer getIsCorrect() {
            return isCorrect;
        }
        
        public void setIsCorrect(Integer isCorrect) {
            this.isCorrect = isCorrect;
        }
    }
}
