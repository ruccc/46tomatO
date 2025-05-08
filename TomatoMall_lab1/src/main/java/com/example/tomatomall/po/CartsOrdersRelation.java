package com.example.tomatomall.po;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Column;

@Entity
@NoArgsConstructor
@Table(name = "carts_orders_relation")
@Setter
@Getter
@Data
public class CartsOrdersRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "cart_item_id", columnDefinition = "VARCHAR(255)")
    private String cartItemId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    // 外键关联到 carts 表
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", referencedColumnName = "cart_item_id", insertable = false, updatable = false)
    private Cart cart;

    // 外键关联到 orders 表
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "orderId", insertable = false, updatable = false)
    private Order order;
}