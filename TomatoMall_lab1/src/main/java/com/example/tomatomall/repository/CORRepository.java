package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.CartsOrdersRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CORRepository extends JpaRepository<CartsOrdersRelation, Integer> {
    List<CartsOrdersRelation> findByOrderId(int orderId);
}
