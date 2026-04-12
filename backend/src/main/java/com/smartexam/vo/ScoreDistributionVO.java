package com.smartexam.vo;

import lombok.Data;

@Data
public class ScoreDistributionVO {
    private Integer range0_60;
    private Integer range60_70;
    private Integer range70_80;
    private Integer range80_90;
    private Integer range90_100;
    private Integer total;
}
