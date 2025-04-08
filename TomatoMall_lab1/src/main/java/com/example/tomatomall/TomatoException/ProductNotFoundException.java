package com.example.tomatomall.TomatoException;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("商品ID " + productId + " 不存在");
    }
}