package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Subject;
import com.smartexam.mapper.SubjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService extends ServiceImpl<SubjectMapper, Subject> {
    
    public List<Subject> findAll() {
        return baseMapper.selectList(null);
    }
    
    public Subject findById(Long id) {
        return baseMapper.selectById(id);
    }
    
    public Subject create(Subject subject) {
        baseMapper.insert(subject);
        return subject;
    }
    
    public Subject update(Subject subject) {
        baseMapper.updateById(subject);
        return subject;
    }
    
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
}
