package com.example.tomatomall.vo;

import com.example.tomatomall.dto.CartResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartListVO {
    private List<CartResponseDTO> items;
    private Integer total;
    private BigDecimal totalAmount;
}