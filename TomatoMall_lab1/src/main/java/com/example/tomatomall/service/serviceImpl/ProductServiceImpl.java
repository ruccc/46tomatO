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
import java.util.HashSet;
import java.util.Optional;
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
        stockpile.setAmount( 0);
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


    public ProductVO getProductById(String id) {
        // 1. 从仓库获取 Optional<Product>
        Optional<Product> optionalProduct = productRepository.findById(id);

        // 2. 使用 map 将 Product 转换为 ProductVO
        //    如果 optionalProduct 为空，则使用 orElse 提供一个默认的 ProductVO
        return optionalProduct.map(product -> {
            // 3. 创建并返回新的 ProductVO 实例
            //    这里假设 ProductVO 有一个接受 Product 的构造函数或 setter 方法
            //    你需要根据你的 ProductVO 实际结构来填充数据
            ProductVO productVO = new ProductVO();
            productVO.setId(product.getId());
            productVO.setTitle(product.getTitle());
            productVO.setPrice(product.getPrice());
            productVO.setRate(product.getRate());
            productVO.setDescription(product.getDescription());
            productVO.setCover(product.getCover());
            productVO.setDetail(product.getDetail());
            Set<ProductSpecification> specs = product.getSpecifications();
            Set<ProductSpecificationVO> specVOs = new HashSet<>();
            for(ProductSpecification a:specs){
                specVOs.add(a.toVO());
            }
            productVO.setSpecifications(specVOs);
            return productVO;
        }).orElse(new ProductVO()); // 或者返回 null，或者抛出异常，取决于你的业务需求
        // 如果返回 null，可以写成 .orElse(null)
        // 如果想找不到时抛出异常，可以写成 .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
}