package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.dto.*;
import com.example.tomatomall.TomatoException.ProductNotFoundException;
import com.example.tomatomall.mapper.ProductMapper;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.mapper.StockpileMapper;
import com.example.tomatomall.vo.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ProductRepository productRepository;
    private StockpileRepository stockpileRepository;
    private ProductMapper productMapper;
    private EntityManager entityManager;
    private StockpileMapper stockpileMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, StockpileRepository stockpileRepository, ProductMapper productMapper, EntityManager entityManager, StockpileMapper stockpileMapper) {
        this.productRepository = productRepository;
        this.stockpileRepository = stockpileRepository;
        this.productMapper = productMapper;
        this.entityManager = entityManager;
        this.stockpileMapper = stockpileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductVO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductVO vo = productMapper.toVO(product);

                    if (vo.getSpecifications() == null && !Hibernate.isInitialized(product.getSpecifications())) {
                        entityManager.refresh(product);
                    }
                    return vo;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailVO getProductDetail(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDetailVO(product);
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

        Stockpile stockpile = new Stockpile();
        stockpile.setAmount(dto.getStockAmount() != null ? dto.getStockAmount() : 0);
        stockpile.setFrozen(0);
        stockpile.setProduct(product);
        product.setStockpile(stockpile);

        Product savedProduct = productRepository.save(product);
        return productMapper.toVO(savedProduct);
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

        product.getSpecifications().clear();
        if (dto.getSpecifications() != null) {
            dto.getSpecifications().forEach(specDto -> {
                ProductSpecification spec = new ProductSpecification();
                spec.setItem(specDto.getItem());
                spec.setValue(specDto.getValue());
                spec.setProduct(product);
                product.addSpecification(spec);
            });
        }

        Product updatedProduct = productRepository.save(product);
        productMapper.toVO(updatedProduct);
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
        stockpile.setFrozen(newFrozen);

        Stockpile updated = stockpileRepository.save(stockpile);
        stockpileMapper.toVO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StockpileVO getStockpile(String productId) {
        Stockpile stockpile = stockpileRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return stockpileMapper.toVO(stockpile);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductVO> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByTitleContainingAndPriceBetween(keyword, minPrice, maxPrice, pageable)
                    .map(productMapper::toVO);
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(productMapper::toVO);
    }

}