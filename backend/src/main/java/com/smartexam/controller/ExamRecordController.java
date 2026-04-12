package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.dto.ExamRecordDTO;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.vo.QuestionAnswerVO;
import com.smartexam.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/exam-records")
public class ExamRecordController {
    
    @Autowired
    private ExamRecordService examRecordService;
    
    @GetMapping
    public Result<List<ExamRecordDTO>> findAll() {
        List<ExamRecordDTO> examRecords = examRecordService.findAllWithDetail();
        return Result.success("获取考试记录列表成功", examRecords);
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<ExamRecordDTO>> findByUserId(@PathVariable Long userId) {
        List<ExamRecordDTO> examRecords = examRecordService.findByUserIdWithExamTitle(userId);
        return Result.success("获取用户考试记录成功", examRecords);
    }
    
    @GetMapping("/exam/{examId}")
    public Result<List<ExamRecord>> findByExamId(@PathVariable Long examId) {
        List<ExamRecord> examRecords = examRecordService.findByExamId(examId);
        return Result.success("获取考试记录成功", examRecords);
    }
    
    @PostMapping("/start")
    public Result<ExamRecord> startExam(@RequestBody ExamStartRequest request) {
        ExamRecord examRecord = examRecordService.startExam(request.getExamId(), request.getUserId());
        return Result.success("开始考试成功", examRecord);
    }
    
    static class ExamStartRequest {
        private Long examId;
        private Long userId;
        
        public Long getExamId() {
            return examId;
        }
        
        public void setExamId(Long examId) {
            this.examId = examId;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
    
    @PostMapping("/submit/{examRecordId}")
    public Result<ExamRecord> submitExam(@PathVariable String examRecordId, @RequestBody List<AnswerRecord> answerRecords) {
        try {
            Long id = Long.parseLong(examRecordId);
            if (id == null) {
                return Result.error("考试记录ID不能为空");
            }
            ExamRecord examRecord = examRecordService.submitExam(id, answerRecords);
            return Result.success("提交考试成功", examRecord);
        } catch (NumberFormatException e) {
            return Result.error("考试记录ID格式错误");
        } catch (Exception e) {
            return Result.error("提交考试失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/answers/{examRecordId}")
    public Result<List<QuestionAnswerVO>> getAnswerRecords(@PathVariable Long examRecordId) {
        List<QuestionAnswerVO> answerRecords = examRecordService.getAnswerRecords(examRecordId);
        return Result.success("获取答题记录成功", answerRecords);
    }
    
    @PostMapping("/update-score/{examRecordId}")
    public Result<Void> updateScore(@PathVariable Long examRecordId, @RequestBody ScoreUpdateRequest request) {
        examRecordService.updateScore(examRecordId, request.getScore());
        return Result.success("更新分数成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examRecordService.delete(id);
        return Result.success("删除成功");
    }
    
    static class ScoreUpdateRequest {
        private BigDecimal score;
        
        public BigDecimal getScore() {
            return score;
        }
        
        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }
}
