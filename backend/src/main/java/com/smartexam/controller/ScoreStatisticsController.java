package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.ScoreStatistics;
import com.smartexam.service.ScoreStatisticsService;
import com.smartexam.vo.ClazzStatisticsVO;
import com.smartexam.vo.ScoreDistributionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class ScoreStatisticsController {
    
    @Autowired
    private ScoreStatisticsService scoreStatisticsService;
    
    @GetMapping
    public Result<List<ScoreStatistics>> findAll() {
        List<ScoreStatistics> statistics = scoreStatisticsService.findAll();
        return Result.success("获取成绩统计列表成功", statistics);
    }
    
    @GetMapping("/exam/{examId}")
    public Result<ScoreStatistics> getStatistics(@PathVariable Long examId) {
        ScoreStatistics statistics = scoreStatisticsService.getStatistics(examId);
        return Result.success("获取考试成绩统计成功", statistics);
    }
    
    @PostMapping("/generate/{examId}")
    public Result<ScoreStatistics> generateStatistics(@PathVariable Long examId) {
        ScoreStatistics statistics = scoreStatisticsService.generateStatistics(examId);
        return Result.success("生成成绩统计成功", statistics);
    }
    
    @GetMapping("/distribution/{examId}")
    public Result<ScoreDistributionVO> getScoreDistribution(@PathVariable Long examId) {
        ScoreDistributionVO distribution = scoreStatisticsService.getScoreDistribution(examId);
        return Result.success("获取成绩分布成功", distribution);
    }
    
    @GetMapping("/clazz/{examId}")
    public Result<List<ClazzStatisticsVO>> getClazzStatistics(@PathVariable Long examId) {
        List<ClazzStatisticsVO> statistics = scoreStatisticsService.getClazzStatistics(examId);
        return Result.success("获取班级成绩统计成功", statistics);
    }
}
