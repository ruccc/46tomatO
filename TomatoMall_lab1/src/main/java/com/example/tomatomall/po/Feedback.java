package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "type", nullable = false)
    private String type; // suggestion/complaint/bug

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "contact")
    private String contact;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-未处理,1-已处理

    @Column(name = "create_time", nullable = false)
    private String createTime;
}