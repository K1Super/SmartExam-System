package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long examId;
    
    private Long paperId;
    
    private Long userId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime submitTime;
    
    private Integer status;
    
    private BigDecimal score;
    
    private BigDecimal objectiveScore;
    
    private BigDecimal subjectiveScore;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
