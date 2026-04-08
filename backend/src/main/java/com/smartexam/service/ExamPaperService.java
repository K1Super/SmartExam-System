package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.ExamPaper;
import com.smartexam.mapper.ExamPaperMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamPaperService extends ServiceImpl<ExamPaperMapper, ExamPaper> {
    
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
        baseMapper.insert(examPaper);
        return examPaper;
    }
    
    public ExamPaper update(ExamPaper examPaper) {
        baseMapper.updateById(examPaper);
        return examPaper;
    }
    
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
    
    public ExamPaper publish(Long id) {
        ExamPaper examPaper = baseMapper.selectById(id);
        examPaper.setStatus(1);
        baseMapper.updateById(examPaper);
        return examPaper;
    }
}
