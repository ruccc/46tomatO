package com.example.tomatomall.repository;

import com.example.tomatomall.po.Stockpile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockpileRepository extends JpaRepository<Stockpile, String> {
    Optional<Stockpile> findByProductId(String productId);
    Stockpile findById(int id);
}