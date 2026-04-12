package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.INPUT)
    private Long id;
    
    private String content;
    
    private Integer type;
    
    private Integer difficulty;
    
    private Long subjectId;
    
    private Long chapterId;
    
    private String knowledgePoint;
    
    private String options;
    
    private String answer;
    
    private String analysis;
    
    private BigDecimal score;
    
    private Long creatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
