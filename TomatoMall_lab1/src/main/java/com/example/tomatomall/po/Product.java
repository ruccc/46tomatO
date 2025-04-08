package com.example.tomatomall.po;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    private String title;
    private BigDecimal price;
    private Double rate;
    private String description;
    private String cover;
    private String detail;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpecification> specifications = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stockpile stockpile;

    public void addSpecification(ProductSpecification spec) {
        specifications.add(spec);
        spec.setProduct(this);
    }

    public void setStockpile(Stockpile stockpile) {
        this.stockpile = stockpile;
        if (stockpile != null) {
            stockpile.setProduct(this);
        }
    }
}