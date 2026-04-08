package com.smartexam.controller;

import com.smartexam.common.Result;
import com.smartexam.entity.ScoreStatistics;
import com.smartexam.service.ScoreStatisticsService;
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
}
