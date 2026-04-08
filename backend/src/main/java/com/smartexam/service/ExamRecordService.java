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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamRecordService extends ServiceImpl<ExamRecordMapper, ExamRecord> {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    public List<ExamRecord> findByUserId(Long userId) {
        return examRecordMapper.selectList(new QueryWrapper<ExamRecord>().eq("user_id", userId));
    }
    
    public List<ExamRecord> findByExamId(Long examId) {
        return examRecordMapper.selectList(new QueryWrapper<ExamRecord>().eq("exam_id", examId));
    }
    
    @Transactional
    public ExamRecord startExam(Long examId, Long userId) {
        // 检查是否已经参加过该考试
        ExamRecord existing = examRecordMapper.selectOne(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId).eq("user_id", userId)
        );
        
        if (existing != null) {
            return existing;
        }
        
        ExamRecord examRecord = new ExamRecord();
        examRecord.setExamId(examId);
        examRecord.setUserId(userId);
        examRecord.setStartTime(LocalDateTime.now());
        examRecord.setStatus(1); // 答题中
        
        examRecordMapper.insert(examRecord);
        return examRecord;
    }
    
    @Transactional
    public ExamRecord submitExam(Long examRecordId, List<AnswerRecord> answerRecords) {
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord == null || examRecord.getStatus() != 1) {
            throw new RuntimeException("考试记录不存在或状态不正确");
        }
        
        // 保存答题记录
        for (AnswerRecord answerRecord : answerRecords) {
            answerRecord.setExamRecordId(examRecordId);
            answerRecordMapper.insert(answerRecord);
        }
        
        // 更新考试记录状态
        examRecord.setSubmitTime(LocalDateTime.now());
        examRecord.setStatus(2); // 已提交
        examRecordMapper.updateById(examRecord);
        
        return examRecord;
    }
    
    public List<AnswerRecord> getAnswerRecords(Long examRecordId) {
        return answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
    }
}
