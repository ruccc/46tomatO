package com.example.tomatomall.po;

import javax.persistence.*;

import com.example.tomatomall.vo.ProductSpecificationVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
@NoArgsConstructor
public class ProductSpecification {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    private String item;
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    public ProductSpecificationVO toVO() {
        ProductSpecificationVO productSpecificationVO = new ProductSpecificationVO();
        productSpecificationVO.setId(id);
        productSpecificationVO.setItem(item);
        productSpecificationVO.setValue(value);
        productSpecificationVO.setProduct(product);
        return productSpecificationVO;
    }
}