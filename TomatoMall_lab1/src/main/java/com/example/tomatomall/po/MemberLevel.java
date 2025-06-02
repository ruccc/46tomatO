package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "member_level")
public class MemberLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @Column(name = "growth_point", nullable = false)
    private Integer growthPoint;

    @Column(name = "discount", nullable = false)
    private Double discount;

    @Column(name = "description")
    private String description;
}
