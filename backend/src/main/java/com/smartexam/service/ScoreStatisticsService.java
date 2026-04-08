package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.ScoreStatistics;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.ScoreStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ScoreStatisticsService extends ServiceImpl<ScoreStatisticsMapper, ScoreStatistics> {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private ScoreStatisticsMapper scoreStatisticsMapper;
    
    public ScoreStatistics generateStatistics(Long examId) {
        // 检查是否已存在统计记录
        ScoreStatistics existing = scoreStatisticsMapper.selectOne(
            new QueryWrapper<ScoreStatistics>().eq("exam_id", examId)
        );
        
        if (existing != null) {
            return existing;
        }
        
        // 获取所有考试记录
        List<ExamRecord> examRecords = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId)
        );
        
        if (examRecords.isEmpty()) {
            throw new RuntimeException("暂无考试记录");
        }
        
        int totalCount = examRecords.size();
        int submitCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("100");
        int passCount = 0;
        int excellentCount = 0;
        
        for (ExamRecord record : examRecords) {
            if (record.getStatus() >= 2) { // 已提交或已阅卷
                submitCount++;
                if (record.getScore() != null) {
                    totalScore = totalScore.add(record.getScore());
                    if (record.getScore().compareTo(maxScore) > 0) {
                        maxScore = record.getScore();
                    }
                    if (record.getScore().compareTo(minScore) < 0) {
                        minScore = record.getScore();
                    }
                    if (record.getScore().compareTo(new BigDecimal("60")) >= 0) {
                        passCount++;
                    }
                    if (record.getScore().compareTo(new BigDecimal("90")) >= 0) {
                        excellentCount++;
                    }
                }
            }
        }
        
        ScoreStatistics statistics = new ScoreStatistics();
        statistics.setExamId(examId);
        statistics.setTotalCount(totalCount);
        statistics.setSubmitCount(submitCount);
        
        if (submitCount > 0) {
            statistics.setAvgScore(totalScore.divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP));
            statistics.setMaxScore(maxScore);
            statistics.setMinScore(minScore);
            statistics.setPassRate(new BigDecimal(passCount).divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
            statistics.setExcellentRate(new BigDecimal(excellentCount).divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
        }
        
        scoreStatisticsMapper.insert(statistics);
        return statistics;
    }
    
    public ScoreStatistics getStatistics(Long examId) {
        return scoreStatisticsMapper.selectOne(
            new QueryWrapper<ScoreStatistics>().eq("exam_id", examId)
        );
    }
    
    public List<ScoreStatistics> findAll() {
        return scoreStatisticsMapper.selectList(null);
    }
}
