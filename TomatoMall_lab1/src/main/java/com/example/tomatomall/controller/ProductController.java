package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.ProductNotFoundException;
import com.example.tomatomall.dto.*;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<ProductVO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        return Result.success(productService.getProducts(pageable));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable String id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping
    public Result<ProductVO> createProduct(@Validated @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.createProduct(dto));
    }

    @PutMapping
    public Result<String> updateProduct(@Validated @RequestBody ProductUpdateDTO dto) {
        try {
            productService.updateProduct(dto);
            return Result.success("更新成功");
        } catch (ProductNotFoundException e) {
            return Result.fail(400, "商品不存在");
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.fail(500, "更新商品失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        if (id != null) {
            return Result.success("删除成功");
        }else {
            return Result.fail(400,"商品不存在");
        }
    }


    @PatchMapping("/stockpile/{productId}")
    public Result<String> adjustStockpile(
            @PathVariable String productId,
            @Validated @RequestBody StockpileAdjustDTO dto) {
        try {
            productService.adjustStockpile(productId, dto);
            return Result.success("调整库存成功");
        } catch (ProductNotFoundException e) {
            return Result.fail(400, "商品不存在");
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @GetMapping("/stockpile/{productId}")
    public Result<StockpileVO> getStockpile(@PathVariable String productId) {
        return Result.success(productService.getStockpile(productId));
    }

    @GetMapping("/search")
    public Result<Page<ProductVO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return Result.success(productService.searchProducts(keyword, minPrice, maxPrice, pageable));
    }
}