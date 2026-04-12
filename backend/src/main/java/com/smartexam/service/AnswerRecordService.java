package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.mapper.AnswerRecordMapper;
import com.smartexam.mapper.ExamRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnswerRecordService extends ServiceImpl<AnswerRecordMapper, AnswerRecord> {
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Transactional
    public void updateScore(Long answerRecordId, BigDecimal score, Integer isCorrect) {
        AnswerRecord answerRecord = answerRecordMapper.selectById(answerRecordId);
        if (answerRecord == null) {
            throw new RuntimeException("答题记录不存在");
        }
        
        answerRecord.setScore(score);
        answerRecord.setIsCorrect(isCorrect);
        answerRecordMapper.updateById(answerRecord);
        
        // 更新对应考试记录的总分
        updateTotalScore(answerRecord.getExamRecordId());
    }
    
    private void updateTotalScore(Long examRecordId) {
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
        
        BigDecimal totalScore = BigDecimal.ZERO;
        for (AnswerRecord record : answerRecords) {
            if (record.getScore() != null) {
                totalScore = totalScore.add(record.getScore());
            }
        }
        
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord != null) {
            examRecord.setScore(totalScore);
            examRecordMapper.updateById(examRecord);
        }
    }
}
