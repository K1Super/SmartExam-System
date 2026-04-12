package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Exam;
import com.smartexam.entity.ExamPaper;
import com.smartexam.entity.ExamRecord;
import com.smartexam.entity.ScoreStatistics;
import com.smartexam.mapper.ExamMapper;
import com.smartexam.mapper.ExamPaperMapper;
import com.smartexam.mapper.ExamRecordMapper;
import com.smartexam.mapper.ScoreStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamService extends ServiceImpl<ExamMapper, Exam> {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ExamRecordMapper examRecordMapper;
    
    @Autowired
    private ScoreStatisticsMapper scoreStatisticsMapper;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    public List<Exam> findAll() {
        List<Exam> exams = baseMapper.selectList(null);
        // 兼容前端字段：把title填充到examTitle
        for (Exam exam : exams) {
            if (exam.getExamTitle() == null || exam.getExamTitle().isEmpty()) {
                exam.setExamTitle(exam.getTitle());
            }
        }
        return exams;
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
        Exam exam = baseMapper.selectById(id);
        if (exam != null && (exam.getExamTitle() == null || exam.getExamTitle().isEmpty())) {
            exam.setExamTitle(exam.getTitle());
        }
        return exam;
    }
    
    public Exam create(Exam exam) {
        // 兼容前端字段 examTitle -> title
        if (exam.getExamTitle() != null && !exam.getExamTitle().isEmpty()) {
            exam.setTitle(exam.getExamTitle());
        }
        if (exam.getTitle() == null || exam.getTitle().isEmpty()) {
            if (exam.getPaperId() != null) {
                ExamPaper paper = examPaperMapper.selectById(exam.getPaperId());
                if (paper != null) {
                    exam.setTitle(paper.getTitle());
                }
            }
        }
        if (exam.getDuration() == null || exam.getDuration() == 0) {
            if (exam.getPaperId() != null) {
                ExamPaper paper = examPaperMapper.selectById(exam.getPaperId());
                if (paper != null && paper.getDuration() != null) {
                    exam.setDuration(paper.getDuration());
                }
            }
        }
        
        List<Exam> exams = baseMapper.selectList(new QueryWrapper<Exam>().orderByDesc("id"));
        Long maxId = 0L;
        if (!exams.isEmpty()) {
            maxId = exams.get(0).getId();
        }
        exam.setId(maxId + 1);
        baseMapper.insert(exam);
        return exam;
    }
    
    public Exam update(Exam exam) {
        // 兼容前端字段 examTitle -> title
        if (exam.getExamTitle() != null && !exam.getExamTitle().isEmpty()) {
            exam.setTitle(exam.getExamTitle());
        }
        
        // 先查数据库原有数据，避免null字段覆盖
        Exam existingExam = baseMapper.selectById(exam.getId());
        if (existingExam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 只更新非null字段
        if (exam.getTitle() != null && !exam.getTitle().isEmpty()) {
            existingExam.setTitle(exam.getTitle());
        }
        if (exam.getPaperId() != null) {
            existingExam.setPaperId(exam.getPaperId());
        }
        if (exam.getClazzIds() != null) {
            existingExam.setClazzIds(exam.getClazzIds());
        }
        if (exam.getStartTime() != null) {
            existingExam.setStartTime(exam.getStartTime());
        }
        if (exam.getEndTime() != null) {
            existingExam.setEndTime(exam.getEndTime());
        }
        if (exam.getStatus() != null) {
            existingExam.setStatus(exam.getStatus());
        }
        if (exam.getDescription() != null) {
            existingExam.setDescription(exam.getDescription());
        }
        if (exam.getDuration() != null && exam.getDuration() > 0) {
            existingExam.setDuration(exam.getDuration());
        }
        
        baseMapper.updateById(existingExam);
        return existingExam;
    }
    
    @Transactional
    public void delete(Long id) {
        examRecordMapper.delete(new QueryWrapper<ExamRecord>().eq("exam_id", id));
        scoreStatisticsMapper.delete(new QueryWrapper<ScoreStatistics>().eq("exam_id", id));
        baseMapper.deleteById(id);
        rearrangeIds();
    }
    
    private void rearrangeIds() {
        List<Exam> exams = baseMapper.selectList(new QueryWrapper<Exam>().orderByAsc("id"));
        
        if (exams.isEmpty()) {
            return;
        }
        
        Map<Long, Long> idMapping = new HashMap<>();
        long newId = 1;
        for (Exam exam : exams) {
            idMapping.put(exam.getId(), newId++);
        }
        
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        try {
            List<ExamRecord> examRecords = examRecordMapper.selectList(null);
            for (ExamRecord record : examRecords) {
                record.setExamId(-record.getExamId());
                examRecordMapper.updateById(record);
            }
            
            for (ExamRecord record : examRecords) {
                Long oldExamId = -record.getExamId();
                if (idMapping.containsKey(oldExamId)) {
                    record.setExamId(idMapping.get(oldExamId));
                    examRecordMapper.updateById(record);
                }
            }
            
            List<ScoreStatistics> statistics = scoreStatisticsMapper.selectList(null);
            for (ScoreStatistics stat : statistics) {
                Long oldExamId = stat.getExamId();
                if (idMapping.containsKey(oldExamId)) {
                    stat.setExamId(idMapping.get(oldExamId));
                    scoreStatisticsMapper.updateById(stat);
                }
            }
            
            jdbcTemplate.execute("TRUNCATE TABLE exam");
            
            for (Exam exam : exams) {
                exam.setId(idMapping.get(exam.getId()));
                baseMapper.insert(exam);
            }
            
            jdbcTemplate.execute("ALTER TABLE exam AUTO_INCREMENT = " + (exams.size() + 1));
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
    
    public void updateExamStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        Exam update1 = new Exam();
        update1.setStatus(1);
        baseMapper.update(update1, new QueryWrapper<Exam>()
            .eq("status", 0)
            .le("start_time", now)
            .ge("end_time", now)
        );
        
        Exam update2 = new Exam();
        update2.setStatus(2);
        baseMapper.update(update2, new QueryWrapper<Exam>()
            .eq("status", 1)
            .lt("end_time", now)
        );
    }
    
    public void publishExam(Long id) {
        Exam exam = baseMapper.selectById(id);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        if (exam.getStatus() != 0) {
            throw new RuntimeException("只能发布未开始的考试");
        }
        
        exam.setStatus(1);
        exam.setStartTime(LocalDateTime.now());
        if (exam.getEndTime() == null && exam.getDuration() != null) {
            exam.setEndTime(LocalDateTime.now().plusMinutes(exam.getDuration()));
        }
        
        baseMapper.updateById(exam);
    }
}
