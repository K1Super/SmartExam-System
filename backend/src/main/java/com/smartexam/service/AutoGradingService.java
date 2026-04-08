package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.Question;
import com.smartexam.mapper.AnswerRecordMapper;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutoGradingService {
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Transactional
    public ExamRecord autoGrade(Long examRecordId) {
        // 获取考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord == null || examRecord.getStatus() != 2) {
            throw new RuntimeException("考试记录不存在或未提交");
        }
        
        // 获取所有答题记录
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
        
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        
        for (AnswerRecord answerRecord : answerRecords) {
            Question question = questionMapper.selectById(answerRecord.getQuestionId());
            if (question == null) {
                continue;
            }
            
            // 客观题自动判分
            if (question.getType() <= 3) { // 1-单选题，2-多选题，3-判断题
                boolean isCorrect = question.getAnswer().equals(answerRecord.getUserAnswer());
                answerRecord.setIsCorrect(isCorrect ? 1 : 0);
                answerRecord.setScore(isCorrect ? question.getScore() : BigDecimal.ZERO);
                objectiveScore = objectiveScore.add(answerRecord.getScore());
            } else {
                // 主观题暂时标记为待阅卷
                answerRecord.setIsCorrect(2);
                answerRecord.setScore(BigDecimal.ZERO);
            }
            
            totalScore = totalScore.add(answerRecord.getScore());
            answerRecordMapper.updateById(answerRecord);
        }
        
        // 更新考试记录
        examRecord.setScore(totalScore);
        examRecord.setObjectiveScore(objectiveScore);
        examRecord.setSubjectiveScore(subjectiveScore);
        examRecord.setStatus(3); // 已阅卷
        examRecordMapper.updateById(examRecord);
        
        return examRecord;
    }
    
    @Transactional
    public AnswerRecord manualGrade(Long answerRecordId, BigDecimal score) {
        AnswerRecord answerRecord = answerRecordMapper.selectById(answerRecordId);
        if (answerRecord == null) {
            throw new RuntimeException("答题记录不存在");
        }
        
        answerRecord.setIsCorrect(score.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        answerRecord.setScore(score);
        answerRecordMapper.updateById(answerRecord);
        
        // 更新对应考试记录的主观题得分
        updateSubjectiveScore(answerRecord.getExamRecordId());
        
        return answerRecord;
    }
    
    private void updateSubjectiveScore(Long examRecordId) {
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
        
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (AnswerRecord answerRecord : answerRecords) {
            if (answerRecord.getIsCorrect() == 2) { // 主观题
                subjectiveScore = subjectiveScore.add(answerRecord.getScore());
            }
            totalScore = totalScore.add(answerRecord.getScore());
        }
        
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        examRecord.setSubjectiveScore(subjectiveScore);
        examRecord.setScore(totalScore);
        examRecordMapper.updateById(examRecord);
    }
}
