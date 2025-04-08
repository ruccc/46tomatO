package com.example.tomatomall.service;

import com.example.tomatomall.dto.ProductCreateDTO;
import com.example.tomatomall.dto.ProductUpdateDTO;
import com.example.tomatomall.vo.ProductDetailVO;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.dto.StockpileAdjustDTO;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductVO> getProducts(Pageable pageable);
    ProductDetailVO getProductDetail(String id);
    ProductVO createProduct(ProductCreateDTO dto);
    ProductVO updateProduct(ProductUpdateDTO dto);
    void deleteProduct(String id);
    StockpileVO adjustStockpile(String productId, StockpileAdjustDTO dto);
    StockpileVO getStockpile(String productId);

    @Transactional(readOnly = true)
    Page<ProductVO> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}