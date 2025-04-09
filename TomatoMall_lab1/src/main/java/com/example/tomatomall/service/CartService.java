package com.example.tomatomall.service;

import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;

public interface CartService {
    CartVO createCartItem(CartVO cartVO);
    void deleteCartItem(Integer cartItemId);
    void adjustCartQuantity(Integer cartItemId, int quantity);
    CartListVO getCart();
}