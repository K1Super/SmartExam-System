package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private Long paperId;
    
    private Long subjectId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer duration;
    
    private Integer status;
    
    private Long creatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
