package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.dto.ExamRecordDTO;
import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.Clazz;
import com.smartexam.entity.Exam;
import com.smartexam.entity.ExamPaper;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.User;
import com.smartexam.vo.QuestionAnswerVO;
import com.smartexam.entity.Question;
import com.smartexam.mapper.AnswerRecordMapper;
import com.smartexam.mapper.ClazzMapper;
import com.smartexam.mapper.ExamMapper;
import com.smartexam.mapper.ExamPaperMapper;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.QuestionMapper;
import com.smartexam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExamRecordService extends ServiceImpl<ExamRecordMapper, ExamRecord> {
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ClazzMapper clazzMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    public List<ExamRecord> findAll() {
        return examRecordMapper.selectList(null);
    }
    
    public List<ExamRecordDTO> findAllWithDetail() {
        List<ExamRecord> records = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().orderByDesc("create_time")
        );
        
        List<ExamRecordDTO> dtoList = new ArrayList<>();
        for (ExamRecord record : records) {
            ExamRecordDTO dto = new ExamRecordDTO();
            dto.setId(record.getId());
            dto.setRecordId(record.getId()); // 兼容前端字段
            dto.setExamId(record.getExamId());
            dto.setPaperId(record.getPaperId());
            dto.setUserId(record.getUserId());
            dto.setStartTime(record.getStartTime());
            dto.setSubmitTime(record.getSubmitTime());
            dto.setStatus(record.getStatus());
            dto.setScore(record.getScore());
            dto.setObjectiveScore(record.getObjectiveScore());
            dto.setSubjectiveScore(record.getSubjectiveScore());
            dto.setCreateTime(record.getCreateTime());
            dto.setUpdateTime(record.getUpdateTime());
            
            // 考试标题
            if (record.getExamId() != null) {
                Exam exam = examMapper.selectById(record.getExamId());
                if (exam != null) {
                    dto.setExamTitle(exam.getTitle());
                }
            }
            
            // 学生信息
            if (record.getUserId() != null) {
                User user = userMapper.selectById(record.getUserId());
                if (user != null) {
                    dto.setUserName(user.getRealName());
                    dto.setClazzId(user.getClazzId());
                }
            }
            
            // 班级信息
            if (dto.getClazzId() != null) {
                Clazz clazz = clazzMapper.selectById(dto.getClazzId());
                if (clazz != null) {
                    dto.setClazzName(clazz.getClassName());
                }
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    public List<ExamRecord> findByUserId(Long userId) {
        return examRecordMapper.selectList(new QueryWrapper<ExamRecord>().eq("user_id", userId));
    }
    
    public List<ExamRecord> findByExamId(Long examId) {
        return examRecordMapper.selectList(new QueryWrapper<ExamRecord>().eq("exam_id", examId));
    }
    
    @Transactional
    public ExamRecord startExam(Long examId, Long userId) {
        // 检查是否已经参加过该考试
        ExamRecord existing = examRecordMapper.selectOne(
            new QueryWrapper<ExamRecord>().eq("exam_id", examId).eq("user_id", userId)
        );
        
        if (existing != null) {
            if (existing.getStatus() >= 2) {
                throw new RuntimeException("您已经交卷，不能重新参加考试！");
            }
            // status=1 答题中，继续考试
            return existing;
        }
        
        // 获取考试信息，包括试卷ID
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        ExamRecord examRecord = new ExamRecord();
        examRecord.setExamId(examId);
        examRecord.setPaperId(exam.getPaperId());
        examRecord.setUserId(userId);
        examRecord.setStartTime(LocalDateTime.now());
        examRecord.setStatus(1); // 答题中
        
        examRecordMapper.insert(examRecord);
        return examRecord;
    }
    
    @Transactional
    public ExamRecord submitExam(Long examRecordId, List<AnswerRecord> answerRecords) {
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord == null || examRecord.getStatus() != 1) {
            throw new RuntimeException("考试记录不存在或状态不正确");
        }
        
        // 保存答题记录
        for (AnswerRecord answerRecord : answerRecords) {
            answerRecord.setExamRecordId(examRecordId);
            answerRecordMapper.insert(answerRecord);
        }
        
        // 更新考试记录状态
        examRecord.setSubmitTime(LocalDateTime.now());
        examRecord.setStatus(2); // 已提交
        examRecordMapper.updateById(examRecord);
        
        return examRecord;
    }
    
    @Autowired
    private AutoGradingService autoGradingService;
    
    public List<QuestionAnswerVO> getAnswerRecords(Long examRecordId) {
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        
        // 只有状态是2（已提交）才需要自动判分
        // 状态是3（已批改）直接返回数据，避免学生查成绩时报错
        if (examRecord != null && examRecord.getStatus() == 2) {
            System.out.println("\n>>> 考试已提交，执行自动判分，ID: " + examRecordId);
            try {
                autoGradingService.autoGrade(examRecordId);
                System.out.println(">>> 自动判分完成");
            } catch (Exception e) {
                System.out.println(">>> 判分跳过: " + e.getMessage());
            }
        } else {
            System.out.println("\n>>> 考试状态=" + (examRecord != null ? examRecord.getStatus() : "null") + "，无需判分，直接返回数据");
        }
        
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
            new QueryWrapper<AnswerRecord>().eq("exam_record_id", examRecordId).orderByAsc("id")
        );
        
        List<QuestionAnswerVO> voList = new ArrayList<>();
        for (AnswerRecord answer : answerRecords) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            voList.add(new QuestionAnswerVO(question, answer));
        }
        return voList;
    }
    
    public List<ExamRecordDTO> findByUserIdWithExamTitle(Long userId) {
        List<ExamRecord> records = examRecordMapper.selectList(
            new QueryWrapper<ExamRecord>().eq("user_id", userId).orderByDesc("create_time")
        );
        
        List<ExamRecordDTO> dtoList = new ArrayList<>();
        for (ExamRecord record : records) {
            ExamRecordDTO dto = new ExamRecordDTO();
            dto.setId(record.getId());
            dto.setRecordId(record.getId());
            dto.setExamId(record.getExamId());
            dto.setPaperId(record.getPaperId());
            dto.setUserId(record.getUserId());
            dto.setStartTime(record.getStartTime());
            dto.setSubmitTime(record.getSubmitTime());
            dto.setStatus(record.getStatus());
            dto.setScore(record.getScore());
            dto.setObjectiveScore(record.getObjectiveScore());
            dto.setSubjectiveScore(record.getSubjectiveScore());
            dto.setCreateTime(record.getCreateTime());
            dto.setUpdateTime(record.getUpdateTime());
            
            // 考试标题 - 从exam表拿更准确
            if (record.getExamId() != null) {
                Exam exam = examMapper.selectById(record.getExamId());
                if (exam != null) {
                    dto.setExamTitle(exam.getTitle());
                }
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    @Transactional
    public void updateScore(Long examRecordId, BigDecimal score) {
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        if (examRecord == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        examRecord.setScore(score);
        examRecord.setStatus(3); // 已批改
        examRecordMapper.updateById(examRecord);
    }
    
    @Transactional
    public void delete(Long id) {
        // 先删除答题记录
        answerRecordMapper.delete(new QueryWrapper<AnswerRecord>().eq("exam_record_id", id));
        // 删除考试记录
        examRecordMapper.deleteById(id);
        // ID重排
        rearrangeIds();
    }
    
    private void rearrangeIds() {
        // ID重排逻辑
    }
}
