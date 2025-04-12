package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUserId(Integer userId);

    Cart findByCartItemId(String cartItemId);
}