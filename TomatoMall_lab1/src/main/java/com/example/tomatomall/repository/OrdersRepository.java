package com.example.tomatomall.repository;

import com.example.tomatomall.po.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Order, Integer> {
    Order findByOrderId(Integer orderId);

}
