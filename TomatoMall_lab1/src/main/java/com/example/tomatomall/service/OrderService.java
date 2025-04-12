package com.example.tomatomall.service;

public interface OrderService {
    void updateOrderStatus(String orderId);
    void reduceStock(String orderId);

}
