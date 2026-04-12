package com.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("clazz")
public class Clazz {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String className;
    
    private String grade;
    
    private Long teacherId;
    
    private Integer studentCount;
    
    private String major;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
