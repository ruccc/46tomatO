package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_service_session")
public class CustomerServiceSession {
    @Id
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "question_type", nullable = false)
    private String questionType; // order/payment/delivery/other

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "status", nullable = false)
    private Integer status; // 0-未处理,1-处理中,2-已解决,3-已关闭

    @Column(name = "rating")
    private Integer rating; // 1-5

    @Column(name = "comment")
    private String comment;

    @Column(name = "create_time", nullable = false)
    private String createTime;

    @Column(name = "update_time", nullable = false)
    private String updateTime;
}