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
        System.out.println("创建新题目");
        // 先重排ID，确保所有记录的ID都是连续的
        rearrangeIds();
        // 获取当前最大ID
        List<Question> questions = baseMapper.selectList(new QueryWrapper<Question>().orderByDesc("id"));
        Long maxId = 0L;
        if (!questions.isEmpty()) {
            maxId = questions.get(0).getId();
            System.out.println("当前最大ID: " + maxId);
        } else {
            System.out.println("当前没有题目，ID从1开始");
        }
        // 设置新ID为最大ID+1
        Long newId = maxId + 1;
        System.out.println("新题目ID: " + newId);
        question.setId(newId);
        // 使用insert方法插入，不依赖自增
        baseMapper.insert(question);
        System.out.println("新题目创建成功，ID: " + newId);
        return question;
    }
    
    public Question update(Question question) {
        baseMapper.updateById(question);
        return question;
    }
    
    public void delete(Long id) {
        System.out.println("===== 开始删除题目 ====");
        System.out.println("删除题目ID: " + id);
        baseMapper.deleteById(id);
        System.out.println("删除成功，开始重排ID");
        // 重排ID
        rearrangeIds();
        System.out.println("===== 删除和重排完成 =====");
    }
    
    /**
     * 重排ID，确保ID连续
     */
    private void rearrangeIds() {
        System.out.println("开始重排题目ID...");
        // 获取所有题目并按ID排序
        List<Question> questions = baseMapper.selectList(new QueryWrapper<Question>().orderByAsc("id"));
        System.out.println("当前题目数量: " + questions.size());
        
        // 先删除所有题目
        for (Question question : questions) {
            System.out.println("删除题目ID: " + question.getId());
            baseMapper.deleteById(question.getId());
        }
        
        // 重新插入并分配新ID
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Long newId = (long) (i + 1);
            question.setId(newId);
            System.out.println("重新插入题目，新ID: " + newId);
            baseMapper.insert(question);
        }
        System.out.println("重排题目ID完成");
    }
    
    public List<Question> search(String keyword) {
        return baseMapper.selectList(new QueryWrapper<Question>()
            .like("content", keyword)
            .or()
            .like("knowledge_point", keyword)
        );
    }
}
