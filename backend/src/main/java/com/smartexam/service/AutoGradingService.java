package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.Question;
import com.smartexam.mapper.AnswerRecordMapper;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutoGradingService {
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Transactional
    public ExamRecord autoGrade(Long examRecordId) {
        // 获取考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        // 状态2（已提交）或3（已批改）都允许判分
        // 老师打开批改时需要重新判分确保最新
        if (examRecord.getStatus() != 2 && examRecord.getStatus() != 3) {
            throw new RuntimeException("考试状态不允许判分");
        }
        
        // 获取所有答题记录
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
        
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        
        for (AnswerRecord answerRecord : answerRecords) {
            Question question = questionMapper.selectById(answerRecord.getQuestionId());
            if (question == null) {
                continue;
            }
            
            // 客观题自动判分
            if (question.getType() <= 3) { // 1-单选题，2-多选题，3-判断题
                String userAnswer = answerRecord.getUserAnswer();
                boolean isCorrect = false;
                
                System.out.println("\n========== 判分DEBUG ==========");
                System.out.println("题目ID: " + question.getId() + " 类型: " + question.getType());
                System.out.println("数据库学生答案: [" + userAnswer + "]");
                System.out.println("数据库正确答案: [" + question.getAnswer() + "]");
                System.out.println("题目分值: " + question.getScore());
                
                if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                    String correctAnswer = question.getAnswer();
                    String userAns = userAnswer.trim();
                    
                    if (question.getType() == 2) {
                        java.util.Set<String> correctSet = splitAnswer(correctAnswer.toUpperCase());
                        java.util.Set<String> userSet = splitAnswer(userAns.toUpperCase());
                        System.out.println("多选题解析: 标准答案=" + correctSet + " 学生答案=" + userSet);
                        isCorrect = correctSet.equals(userSet);
                        System.out.println("匹配结果: " + isCorrect);
                    } else if (question.getType() == 3) {
                        String correctNorm = normalizeBoolean(correctAnswer);
                        String userNorm = normalizeBoolean(userAns);
                        System.out.println("判断题解析: 标准答案=" + correctNorm + " 学生答案=" + userNorm);
                        isCorrect = correctNorm.equals(userNorm);
                        System.out.println("匹配结果: " + isCorrect);
                    } else {
                        System.out.println("单选题解析: equalsIgnoreCase比较");
                        isCorrect = correctAnswer.equalsIgnoreCase(userAns);
                        System.out.println("匹配结果: " + isCorrect + " (" + correctAnswer + " vs " + userAns + ")");
                    }
                } else {
                    System.out.println("学生答案为空，直接判错");
                }
                
                answerRecord.setIsCorrect(isCorrect ? 1 : 0);
                answerRecord.setScore(isCorrect ? question.getScore() : BigDecimal.ZERO);
                System.out.println("最终判分: " + answerRecord.getScore() + "分");
                System.out.println("==============================\n");
                
                objectiveScore = objectiveScore.add(answerRecord.getScore());
            } else {
                // 主观题暂时标记为待阅卷
                answerRecord.setIsCorrect(2);
                answerRecord.setScore(BigDecimal.ZERO);
            }
            
            totalScore = totalScore.add(answerRecord.getScore());
            answerRecordMapper.updateById(answerRecord);
        }
        
        // 更新考试记录
        examRecord.setScore(totalScore);
        examRecord.setObjectiveScore(objectiveScore);
        examRecord.setSubjectiveScore(subjectiveScore);
        examRecordMapper.updateById(examRecord);
        
        System.out.println("=== 自动判分完成 ===");
        System.out.println("考试记录ID: " + examRecordId + " 总分: " + totalScore);
        
        return examRecord;
    }
    
    @Transactional
    public AnswerRecord manualGrade(Long answerRecordId, BigDecimal score) {
        AnswerRecord answerRecord = answerRecordMapper.selectById(answerRecordId);
        if (answerRecord == null) {
            throw new RuntimeException("答题记录不存在");
        }
        
        answerRecord.setIsCorrect(score.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        answerRecord.setScore(score);
        answerRecordMapper.updateById(answerRecord);
        
        // 更新对应考试记录的主观题得分
        updateSubjectiveScore(answerRecord.getExamRecordId());
        
        return answerRecord;
    }
    
    private String normalizeBoolean(String val) {
        if (val == null) return "";
        String v = val.trim().toLowerCase();
        if (v.equals("true") || v.equals("1") || v.equals("正确") || v.equals("对") || v.equals("yes")) {
            return "true";
        }
        return "false";
    }
    
    private java.util.Set<String> splitAnswer(String str) {
        java.util.Set<String> result = new java.util.HashSet<>();
        if (str == null || str.trim().isEmpty()) {
            return result;
        }
        String s = str.trim();
        if (s.contains(",") || s.contains("，")) {
            // 有逗号按逗号分割
            String[] parts = s.split("[,，]");
            for (String part : parts) {
                String p = part.trim();
                if (!p.isEmpty()) {
                    result.add(p);
                }
            }
        } else {
            // 没有逗号就是ABCD连写，按单个字符分割
            for (char c : s.toCharArray()) {
                if (c >= 'A' && c <= 'D') {
                    result.add(String.valueOf(c));
                }
            }
        }
        return result;
    }
    
    private void updateSubjectiveScore(Long examRecordId) {
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId)
        );
        
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (AnswerRecord answerRecord : answerRecords) {
            if (answerRecord.getIsCorrect() == 2) { // 主观题
                subjectiveScore = subjectiveScore.add(answerRecord.getScore());
            }
            totalScore = totalScore.add(answerRecord.getScore());
        }
        
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        examRecord.setSubjectiveScore(subjectiveScore);
        examRecord.setScore(totalScore);
        examRecordMapper.updateById(examRecord);
    }
}
