package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("paper_id")
    private Long paperId;
    
    @TableField("question_id")
    private Long questionId;
    
    @TableField("question_order")
    private Integer questionOrder;
    
    private BigDecimal score;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
