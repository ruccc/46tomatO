package com.example.tomatomall.service;

import com.example.tomatomall.dto.CheckoutRequestDTO;
import com.example.tomatomall.dto.CheckoutResponseDTO;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;

public interface CartService {
    CartVO createCartItem(CartVO cartVO);
    void deleteCartItem(String cartItemId);
    void adjustCartQuantity(String cartItemId, int quantity);
    CartListVO getCart();
    CheckoutResponseDTO generateOrder(CheckoutRequestDTO checkoutRequestDTO);
}