package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Exam;
import com.smartexam.mapper.ExamMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamService extends ServiceImpl<ExamMapper, Exam> {
    
    public List<Exam> findAll() {
        return baseMapper.selectList(null);
    }
    
    public List<Exam> findBySubject(Long subjectId) {
        return baseMapper.selectList(new QueryWrapper<Exam>().eq("subject_id", subjectId));
    }
    
    public List<Exam> findByStatus(Integer status) {
        return baseMapper.selectList(new QueryWrapper<Exam>().eq("status", status));
    }
    
    public List<Exam> findCurrentExams() {
        LocalDateTime now = LocalDateTime.now();
        return baseMapper.selectList(new QueryWrapper<Exam>()
            .eq("status", 1)
            .le("start_time", now)
            .ge("end_time", now)
        );
    }
    
    public Exam findById(Long id) {
        return baseMapper.selectById(id);
    }
    
    public Exam create(Exam exam) {
        baseMapper.insert(exam);
        return exam;
    }
    
    public Exam update(Exam exam) {
        baseMapper.updateById(exam);
        return exam;
    }
    
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
    
    public void updateExamStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新未开始的考试为进行中
        baseMapper.update(null, new QueryWrapper<Exam>()
            .eq("status", 0)
            .le("start_time", now)
            .ge("end_time", now)
            .set("status", 1)
        );
        
        // 更新进行中的考试为已结束
        baseMapper.update(null, new QueryWrapper<Exam>()
            .eq("status", 1)
            .lt("end_time", now)
            .set("status", 2)
        );
    }
}
