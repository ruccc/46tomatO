package com.example.tomatomall.po;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "carts")
@Setter
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private String cartItemId;

    @Column(name = "user_id",nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Transient
    private String title;
    @Transient
    private BigDecimal price;
    @Transient
    private String description;
    @Transient
    private String cover;
    @Transient
    private String detail;
}