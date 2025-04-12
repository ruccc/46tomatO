package com.example.tomatomall.controller;

import com.example.tomatomall.dto.CheckoutRequestDTO;
import com.example.tomatomall.dto.CheckoutResponseDTO;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public Result<CartVO> createCartItem(@RequestBody CartVO cartVO) {
        CartVO createdItem = cartService.createCartItem(cartVO);
        return Result.success(createdItem);
    }
    @PostMapping("/checkout")
    public Response<CheckoutResponseDTO> createCartItem(@RequestBody CheckoutRequestDTO checkoutRequestDTO) {

        return Response.buildSuccess(cartService.generateOrder(checkoutRequestDTO));
    }
    @DeleteMapping("/{cartItemId}")
    public Result<String> deleteCartItem(@PathVariable Integer cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return Result.success("删除成功");
    }

    @PatchMapping("/{cartItemId}")
    public Result<String> adjustCartQuantity(
            @PathVariable Integer cartItemId,
            @RequestBody QuantityRequest request) {
        cartService.adjustCartQuantity(cartItemId, request.getQuantity());
        return Result.success("修改数量成功");
    }

    @Setter
    @Getter
    public static class QuantityRequest {
        private Integer quantity;
    }

    @GetMapping
    public Result<CartListVO> getCart() {
        CartListVO cartList = cartService.getCart();
        return Result.success(cartList);
    }
}
