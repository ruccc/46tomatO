package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.dto.*;
import com.example.tomatomall.TomatoException.ProductNotFoundException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockpileRepository stockpileRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductVO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductVO vo = product.toVO(); // 直接调用 PO 的 toVO() 方法
                    // 处理延迟加载的字段
                    if (vo.getSpecifications() == null && !Hibernate.isInitialized(product.getSpecifications())) {
                        entityManager.refresh(product);
                        vo.setSpecifications(
                                product.getSpecifications().stream()
                                        .map(ProductSpecification::toVO)
                                        .collect(Collectors.toSet())
                        );
                    }
                    return vo;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailVO getProductDetail(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductDetailVO detailVO = new ProductDetailVO();
        // 手动设置字段
        detailVO.setId(product.getId());
        detailVO.setTitle(product.getTitle());
        detailVO.setPrice(product.getPrice());
        detailVO.setRate(product.getRate());
        detailVO.setDescription(product.getDescription());
        detailVO.setCover(product.getCover());
        detailVO.setDetail(product.getDetail());
        // 转换规格和库存
        detailVO.setSpecifications(
                product.getSpecifications().stream()
                        .map(ProductSpecification::toVO)
                        .collect(Collectors.toList())
        );
        detailVO.setStockpile(
                product.getStockpile() != null ? product.getStockpile().toVO() : null
        );
        return detailVO;
    }

    @Override
    @Transactional
    public ProductVO createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setRate(dto.getRate());
        product.setDescription(dto.getDescription());
        product.setCover(dto.getCover());
        product.setDetail(dto.getDetail());

        // 设置规格
        Set<ProductSpecification> specs = dto.getSpecifications().stream()
                .map(specDto -> {
                    ProductSpecification spec = new ProductSpecification();
                    spec.setItem(specDto.getItem());
                    spec.setValue(specDto.getValue());
                    spec.setProduct(product);
                    return spec;
                })
                .collect(Collectors.toSet());
        product.setSpecifications(specs);

        // 设置库存
        Stockpile stockpile = new Stockpile();
        stockpile.setAmount(0);
        stockpile.setFrozen(0);
        stockpile.setProduct(product);
        product.setStockpile(stockpile);

        Product savedProduct = productRepository.save(product);
        return savedProduct.toVO(); // 直接调用 PO 的 toVO()
    }

    @Override
    @Transactional
    public void updateProduct(ProductUpdateDTO dto) {
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new ProductNotFoundException(dto.getId()));

        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setRate(dto.getRate());
        product.setDescription(dto.getDescription());
        product.setCover(dto.getCover());
        product.setDetail(dto.getDetail());

        updateProductSpecifications(product, dto.getSpecifications());
        productRepository.save(product);
    }

    private void updateProductSpecifications(Product product, Set<ProductSpecificationDTO> specDTOs) {
        product.getSpecifications().clear();
        if (specDTOs != null && !specDTOs.isEmpty()) {
            specDTOs.forEach(specDto -> {
                ProductSpecification spec = new ProductSpecification();
                spec.setItem(specDto.getItem());
                spec.setValue(specDto.getValue());
                product.addSpecification(spec);
            });
        }
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void adjustStockpile(String productId, StockpileAdjustDTO dto) {
        Stockpile stockpile = stockpileRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int newAmount = stockpile.getAmount() + dto.getAmount();
        if (newAmount < 0) {
            throw new IllegalArgumentException("调整后的库存不能小于0");
        }
        stockpile.setAmount(newAmount);

        int newFrozen = stockpile.getFrozen() + dto.getFrozen();
        if (newFrozen < 0) {
            throw new IllegalArgumentException("调整后的冻结库存不能小于0");
        }
        if (newFrozen > stockpile.getAmount()) {
            throw new IllegalArgumentException("冻结库存不能超过总库存");
        }
        stockpile.setFrozen(newFrozen);
        stockpileRepository.save(stockpile);
    }

    @Override
    @Transactional(readOnly = true)
    public StockpileVO getStockpile(String productId) {
        Stockpile stockpile = stockpileRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return stockpile.toVO(); // 直接调用 PO 的 toVO()
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductVO> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByTitleContainingAndPriceBetween(keyword, minPrice, maxPrice, pageable)
                    .map(Product::toVO); // 使用方法引用
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(Product::toVO);
    }

    @Override
    public ProductVO getProductById(String id) {
        return productRepository.findById(id)
                .map(product -> {
                    ProductVO vo = product.toVO();
                    // 确保规格数据已加载
                    if (vo.getSpecifications() == null) {
                        vo.setSpecifications(
                                product.getSpecifications().stream()
                                        .map(ProductSpecification::toVO)
                                        .collect(Collectors.toSet())
                        );
                    }
                    return vo;
                })
                .orElse(new ProductVO()); // 或抛出异常
    }
}