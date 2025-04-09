package com.example.tomatomall.TomatoException;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException() {
        super("商品不存在");
    }
}
