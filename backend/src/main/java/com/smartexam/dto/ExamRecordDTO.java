package com.smartexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamRecordDTO {
    private Long id;
    private Long recordId;
    private Long examId;
    private String examTitle;
    private Long paperId;
    private Long userId;
    private String userName;
    private Long clazzId;
    private String clazzName;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer status;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
