package com.example.tomatomall.po;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NoArgsConstructor
@Table(name = "carts")
@Setter
@Getter
@Data
public class Cart {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "cart_item_id", columnDefinition = "VARCHAR(255)")
    private String cartItemId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false, length = 36)
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