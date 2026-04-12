package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.ExamPaper;
import com.smartexam.entity.ExamPaperQuestion;
import com.smartexam.mapper.ExamPaperMapper;
import com.smartexam.mapper.ExamPaperQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamPaperService extends ServiceImpl<ExamPaperMapper, ExamPaper> {
    
    @Autowired
    private ExamPaperQuestionMapper examPaperQuestionMapper;
    
    public List<ExamPaper> findAll() {
        return baseMapper.selectList(null);
    }
    
    public List<ExamPaper> findBySubject(Long subjectId) {
        return baseMapper.selectList(new QueryWrapper<ExamPaper>().eq("subject_id", subjectId));
    }
    
    public List<ExamPaper> findByStatus(Integer status) {
        return baseMapper.selectList(new QueryWrapper<ExamPaper>().eq("status", status));
    }
    
    public ExamPaper findById(Long id) {
        return baseMapper.selectById(id);
    }
    
    public ExamPaper create(ExamPaper examPaper) {
        // 获取当前最大ID
        List<ExamPaper> examPapers = baseMapper.selectList(new QueryWrapper<ExamPaper>().orderByDesc("id"));
        Long maxId = 0L;
        if (!examPapers.isEmpty()) {
            maxId = examPapers.get(0).getId();
        }
        // 设置新ID为最大ID+1
        examPaper.setId(maxId + 1);
        baseMapper.insert(examPaper);
        return examPaper;
    }
    
    public ExamPaper update(ExamPaper examPaper) {
        baseMapper.updateById(examPaper);
        return examPaper;
    }
    
    public void delete(Long id) {
        baseMapper.deleteById(id);
        // 重排ID
        rearrangeIds();
    }
    
    /**
     * 重排ID，确保ID连续
     */
    private void rearrangeIds() {
        List<ExamPaper> examPapers = baseMapper.selectList(new QueryWrapper<ExamPaper>().orderByAsc("id"));
        for (int i = 0; i < examPapers.size(); i++) {
            ExamPaper examPaper = examPapers.get(i);
            if (examPaper.getId() != (i + 1L)) {
                examPaper.setId(i + 1L);
                baseMapper.updateById(examPaper);
            }
        }
    }
    
    public void addQuestions(Long examPaperId, List<Long> questionIds) {
        ExamPaper examPaper = baseMapper.selectById(examPaperId);
        if (examPaper == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        if (questionIds == null || questionIds.isEmpty()) {
            throw new RuntimeException("题目ID列表不能为空");
        }
        
        List<ExamPaperQuestion> existingQuestions = examPaperQuestionMapper.selectList(
            new QueryWrapper<ExamPaperQuestion>().eq("paper_id", examPaperId)
        );
        int maxOrder = existingQuestions.size();
        
        java.util.Set<Long> existingQuestionIds = existingQuestions.stream()
            .map(ExamPaperQuestion::getQuestionId)
            .collect(java.util.stream.Collectors.toSet());
        
        int addedCount = 0;
        for (int i = 0; i < questionIds.size(); i++) {
            Long questionId = questionIds.get(i);
            if (existingQuestionIds.contains(questionId)) {
                continue;
            }
            
            ExamPaperQuestion examPaperQuestion = new ExamPaperQuestion();
            examPaperQuestion.setPaperId(examPaperId);
            examPaperQuestion.setQuestionId(questionId);
            examPaperQuestion.setQuestionOrder(maxOrder + addedCount + 1);
            examPaperQuestion.setScore(BigDecimal.ZERO);
            examPaperQuestionMapper.insert(examPaperQuestion);
            addedCount++;
        }
        
        if (addedCount > 0) {
            int currentCount = examPaper.getQuestionCount() != null ? examPaper.getQuestionCount() : 0;
            examPaper.setQuestionCount(currentCount + addedCount);
            baseMapper.updateById(examPaper);
        }
    }
    
    public List<Long> getQuestions(Long examPaperId) {
        List<ExamPaperQuestion> examPaperQuestions = examPaperQuestionMapper.selectList(
            new QueryWrapper<ExamPaperQuestion>().eq("paper_id", examPaperId)
        );
        return examPaperQuestions.stream()
            .map(ExamPaperQuestion::getQuestionId)
            .collect(Collectors.toList());
    }
    
    public void removeQuestion(Long examPaperId, Long questionId) {
        examPaperQuestionMapper.delete(
            new QueryWrapper<ExamPaperQuestion>()
                .eq("paper_id", examPaperId)
                .eq("question_id", questionId)
        );
        
        ExamPaper examPaper = baseMapper.selectById(examPaperId);
        if (examPaper != null && examPaper.getQuestionCount() != null && examPaper.getQuestionCount() > 0) {
            examPaper.setQuestionCount(examPaper.getQuestionCount() - 1);
            baseMapper.updateById(examPaper);
        }
    }
}
