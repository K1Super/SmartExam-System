package com.smartexam.service;

import com.smartexam.entity.Question;
import com.smartexam.mapper.QuestionMapper;
import com.smartexam.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    private static final String HOT_QUESTIONS_KEY = "hot_questions";
    private static final String EXAM_CONFIG_KEY = "exam_config_%s";
    private static final String USER_SESSION_KEY = "user_session_%s";
    
    public List<Question> getHotQuestions() {
        List<Question> questions = (List<Question>) redisUtil.get(HOT_QUESTIONS_KEY);
        if (questions == null) {
            // 从数据库获取热门题目（这里简单返回所有题目，实际应该根据使用频率排序）
            questions = questionMapper.selectList(null);
            redisUtil.set(HOT_QUESTIONS_KEY, questions, 1, TimeUnit.HOURS);
        }
        return questions;
    }
    
    public void clearHotQuestionsCache() {
        redisUtil.delete(HOT_QUESTIONS_KEY);
    }
    
    public void setExamConfig(Long examId, Object config) {
        String key = String.format(EXAM_CONFIG_KEY, examId);
        redisUtil.set(key, config, 24, TimeUnit.HOURS);
    }
    
    public Object getExamConfig(Long examId) {
        String key = String.format(EXAM_CONFIG_KEY, examId);
        return redisUtil.get(key);
    }
    
    public void setUserSession(Long userId, Object session) {
        String key = String.format(USER_SESSION_KEY, userId);
        redisUtil.set(key, session, 24, TimeUnit.HOURS);
    }
    
    public Object getUserSession(Long userId) {
        String key = String.format(USER_SESSION_KEY, userId);
        return redisUtil.get(key);
    }
    
    public void clearUserSession(Long userId) {
        String key = String.format(USER_SESSION_KEY, userId);
        redisUtil.delete(key);
    }
}
