package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Subject;
import com.smartexam.mapper.SubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService extends ServiceImpl<SubjectMapper, Subject> {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
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
    
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
        rearrangeIds();
    }
    
    private void rearrangeIds() {
        List<Subject> subjects = baseMapper.selectList(new QueryWrapper<Subject>().orderByAsc("id"));
        
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        try {
            jdbcTemplate.execute("TRUNCATE TABLE subject");
            
            long newId = 1;
            for (Subject subject : subjects) {
                subject.setId(newId++);
                baseMapper.insert(subject);
            }
            
            jdbcTemplate.execute("ALTER TABLE subject AUTO_INCREMENT = " + (subjects.size() + 1));
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
