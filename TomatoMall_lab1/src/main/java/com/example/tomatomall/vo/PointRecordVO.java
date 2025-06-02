package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class PointRecordVO {
    private Integer id;
    private String memberId;
    private Integer changePoints;
    private String source;
    private Integer remainingPoints;
    private String createTime;
    private String remark;

    // 变动类型: 增加/减少
    private String changeType;
}