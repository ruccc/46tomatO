package com.example.tomatomall.po;

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
    private Set<ProductSpecification> specifications = new Set<ProductSpecification>() {

        @Override
        public int size() {
            return specifications.size();
        }

        @Override
        public boolean isEmpty() {
            return specifications.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return specifications.contains(o);
        }

        @Override
        public Iterator<ProductSpecification> iterator() {
            return specifications.iterator();
        }

        @Override
        public Object[] toArray() {
            return specifications.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return specifications.toArray(a);
        }

        @Override
        public boolean add(ProductSpecification productSpecification) {
            return specifications.add(productSpecification);
        }

        @Override
        public boolean remove(Object o) {
            return specifications.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return specifications.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends ProductSpecification> c) {
            return specifications.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return specifications.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return specifications.removeAll(c);
        }

        @Override
        public void clear() {
            specifications.clear();
        }
    };

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
}