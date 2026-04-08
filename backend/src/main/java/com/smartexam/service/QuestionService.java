package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Question;
import com.smartexam.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {
    
    public List<Question> findAll() {
        return baseMapper.selectList(null);
    }
    
    public List<Question> findByType(Integer type) {
        return baseMapper.selectList(new QueryWrapper<Question>().eq("type", type));
    }
    
    public List<Question> findBySubject(Long subjectId) {
        return baseMapper.selectList(new QueryWrapper<Question>().eq("subject_id", subjectId));
    }
    
    public List<Question> findByDifficulty(Integer difficulty) {
        return baseMapper.selectList(new QueryWrapper<Question>().eq("difficulty", difficulty));
    }
    
    public Question findById(Long id) {
        return baseMapper.selectById(id);
    }
    
    public Question create(Question question) {
        baseMapper.insert(question);
        return question;
    }
    
    public Question update(Question question) {
        baseMapper.updateById(question);
        return question;
    }
    
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
    
    public List<Question> search(String keyword) {
        return baseMapper.selectList(new QueryWrapper<Question>()
            .like("content", keyword)
            .or()
            .like("knowledge_point", keyword)
        );
    }
}
