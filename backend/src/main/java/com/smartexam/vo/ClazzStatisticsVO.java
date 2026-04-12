package com.smartexam.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClazzStatisticsVO {
    private Long clazzId;
    private String clazzName;
    private Integer totalCount;
    private Integer submitCount;
    private BigDecimal avgScore;
    private BigDecimal passRate;
    private BigDecimal excellentRate;
    private Integer passCount;
    private Integer excellentCount;
}
