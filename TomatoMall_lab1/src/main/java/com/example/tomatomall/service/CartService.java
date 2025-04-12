package com.example.tomatomall.service;

import com.example.tomatomall.dto.CheckoutRequestDTO;
import com.example.tomatomall.dto.CheckoutResponseDTO;
import com.example.tomatomall.vo.CartListVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;

public interface CartService {
    CartVO createCartItem(CartVO cartVO);
    void deleteCartItem(Integer cartItemId);
    void adjustCartQuantity(Integer cartItemId, int quantity);
    CartListVO getCart();
    CheckoutResponseDTO generateOrder(CheckoutRequestDTO checkoutRequestDTO);
}