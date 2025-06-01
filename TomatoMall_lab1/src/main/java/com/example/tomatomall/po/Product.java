package com.example.tomatomall.po;

import com.example.tomatomall.vo.ProductSpecificationVO;
import com.example.tomatomall.vo.ProductVO;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

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
    @JsonManagedReference
    private Set<ProductSpecification> specifications = new HashSet<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
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

    public ProductVO toVO(){
        ProductVO vo = new ProductVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setPrice(price);
        vo.setRate(rate);
        vo.setDescription(description);
        vo.setCover(cover);
        vo.setDetail(detail);
        Set<ProductSpecificationVO> specificationsVOs = new HashSet<>();
        for (ProductSpecification spec : specifications) {
            specificationsVOs.add(spec.toVO());
        }
        vo.setSpecifications(specificationsVOs);
        return vo;
    }
}