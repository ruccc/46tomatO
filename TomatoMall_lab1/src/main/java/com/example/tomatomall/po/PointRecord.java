package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "point_record")
public class PointRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "change_points", nullable = false)
    private Integer changePoints;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "remaining_points", nullable = false)
    private Integer remainingPoints;

    @Column(name = "create_time", nullable = false)
    private String createTime;

    @Column(name = "remark")
    private String remark;
}