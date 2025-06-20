package com.example.tomatomall.po;

import com.example.tomatomall.vo.StockpileVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "stockpiles")
@Getter
@Setter
@NoArgsConstructor
public class Stockpile {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    private Integer amount;
    private Integer frozen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    public StockpileVO toVO() {
        StockpileVO vo = new StockpileVO();
        vo.setId(id);
        vo.setAmount(amount);
        vo.setFrozen(frozen);
        vo.setProductId(product != null ? product.getId() : null);
        return vo;
    }
}