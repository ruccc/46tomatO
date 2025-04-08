package com.example.tomatomall.repository;

import com.example.tomatomall.po.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findById(String id);

    @EntityGraph(attributePaths = {"specifications"})
    Optional<Product> findWithSpecificationsById(String id);

    @EntityGraph(attributePaths = {"specifications", "stockpile"})
    Optional<Product> findDetailById(String id);

    Page<Product> findByTitleContainingAndPriceBetween(String title, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}