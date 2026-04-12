package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Clazz;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.ScoreStatistics;
import com.smartexam.entity.User;
import com.smartexam.mapper.ClazzMapper;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.ScoreStatisticsMapper;
import com.smartexam.mapper.UserMapper;
import com.smartexam.vo.ClazzStatisticsVO;
import com.smartexam.vo.ScoreDistributionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreStatisticsService extends ServiceImpl<ScoreStatisticsMapper, ScoreStatistics> {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private ScoreStatisticsMapper scoreStatisticsMapper;
    
    @Autowired
    private ClazzMapper clazzMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    public ScoreStatistics generateStatistics(Long examId) {
        ScoreStatistics existing = scoreStatisticsMapper.selectOne(
            new QueryWrapper<ScoreStatistics>().eq("exam_id", examId)
        );
        
        if (existing != null) {
            return existing;
        }
        
        List<ExamRecord> examRecords = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId)
        );
        
        ScoreStatistics statistics = new ScoreStatistics();
        statistics.setExamId(examId);
        statistics.setTotalCount(examRecords.size());
        statistics.setSubmitCount(0);
        statistics.setAvgScore(BigDecimal.ZERO);
        statistics.setMaxScore(BigDecimal.ZERO);
        statistics.setMinScore(BigDecimal.ZERO);
        statistics.setPassRate(BigDecimal.ZERO);
        statistics.setExcellentRate(BigDecimal.ZERO);
        
        if (examRecords.isEmpty()) {
            scoreStatisticsMapper.insert(statistics);
            return statistics;
        }
        
        int totalCount = examRecords.size();
        int submitCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("100");
        int passCount = 0;
        int excellentCount = 0;
        
        for (ExamRecord record : examRecords) {
            if (record.getStatus() >= 2) {
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
        
        statistics.setTotalCount(totalCount);
        statistics.setSubmitCount(submitCount);
        
        if (submitCount > 0) {
            statistics.setAvgScore(totalScore.divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP));
            statistics.setMaxScore(maxScore);
            statistics.setMinScore(minScore);
            statistics.setPassRate(new BigDecimal(passCount).divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
            statistics.setExcellentRate(new BigDecimal(excellentCount).divide(new BigDecimal(submitCount), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
        } else {
            statistics.setAvgScore(BigDecimal.ZERO);
            statistics.setMaxScore(BigDecimal.ZERO);
            statistics.setMinScore(BigDecimal.ZERO);
            statistics.setPassRate(BigDecimal.ZERO);
            statistics.setExcellentRate(BigDecimal.ZERO);
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
    
    public ScoreDistributionVO getScoreDistribution(Long examId) {
        List<ExamRecord> examRecords = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId).ge("status", 2)
        );
        
        ScoreDistributionVO vo = new ScoreDistributionVO();
        vo.setRange0_60(0);
        vo.setRange60_70(0);
        vo.setRange70_80(0);
        vo.setRange80_90(0);
        vo.setRange90_100(0);
        vo.setTotal(examRecords.size());
        
        for (ExamRecord record : examRecords) {
            if (record.getScore() != null) {
                BigDecimal score = record.getScore();
                if (score.compareTo(new BigDecimal("60")) < 0) {
                    vo.setRange0_60(vo.getRange0_60() + 1);
                } else if (score.compareTo(new BigDecimal("70")) < 0) {
                    vo.setRange60_70(vo.getRange60_70() + 1);
                } else if (score.compareTo(new BigDecimal("80")) < 0) {
                    vo.setRange70_80(vo.getRange70_80() + 1);
                } else if (score.compareTo(new BigDecimal("90")) < 0) {
                    vo.setRange80_90(vo.getRange80_90() + 1);
                } else {
                    vo.setRange90_100(vo.getRange90_100() + 1);
                }
            }
        }
        
        return vo;
    }
    
    public List<ClazzStatisticsVO> getClazzStatistics(Long examId) {
        List<ExamRecord> examRecords = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId).ge("status", 2)
        );
        
        if (examRecords.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<Long, Clazz> clazzMap = clazzMapper.selectList(null).stream()
            .collect(Collectors.toMap(Clazz::getId, c -> c));
        
        Map<Long, User> userMap = userMapper.selectList(null).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        
        Map<Long, List<ExamRecord>> clazzRecords = new HashMap<>();
        
        for (ExamRecord record : examRecords) {
            User user = userMap.get(record.getUserId());
            if (user != null && user.getClazzId() != null) {
                clazzRecords.computeIfAbsent(user.getClazzId(), k -> new ArrayList<>()).add(record);
            }
        }
        
        List<ClazzStatisticsVO> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<ExamRecord>> entry : clazzRecords.entrySet()) {
            Long clazzId = entry.getKey();
            List<ExamRecord> records = entry.getValue();
            
            ClazzStatisticsVO vo = new ClazzStatisticsVO();
            vo.setClazzId(clazzId);
            vo.setClazzName(clazzMap.containsKey(clazzId) ? clazzMap.get(clazzId).getClassName() : "未知班级");
            vo.setTotalCount(records.size());
            vo.setSubmitCount(records.size());
            
            BigDecimal totalScore = BigDecimal.ZERO;
            int passCount = 0;
            int excellentCount = 0;
            
            for (ExamRecord record : records) {
                if (record.getScore() != null) {
                    totalScore = totalScore.add(record.getScore());
                    if (record.getScore().compareTo(new BigDecimal("60")) >= 0) {
                        passCount++;
                    }
                    if (record.getScore().compareTo(new BigDecimal("90")) >= 0) {
                        excellentCount++;
                    }
                }
            }
            
            int count = records.size();
            if (count > 0) {
                vo.setAvgScore(totalScore.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP));
                vo.setPassCount(passCount);
                vo.setExcellentCount(excellentCount);
                vo.setPassRate(new BigDecimal(passCount).divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
                vo.setExcellentRate(new BigDecimal(excellentCount).divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
            }
            
            result.add(vo);
        }
        
        return result;
    }
}
