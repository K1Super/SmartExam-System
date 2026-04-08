package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_statistics")
public class ScoreStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long examId;
    
    private Long subjectId;
    
    private Integer totalCount;
    
    private Integer submitCount;
    
    private BigDecimal avgScore;
    
    private BigDecimal maxScore;
    
    private BigDecimal minScore;
    
    private BigDecimal passRate;
    
    private BigDecimal excellentRate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
