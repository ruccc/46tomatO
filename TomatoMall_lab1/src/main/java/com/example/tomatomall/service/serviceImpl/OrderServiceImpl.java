package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.CartsOrdersRelation;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersRepository ordersRepository;
    
    @Autowired
    private CORRepository corRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockpileRepository stockpileRepository;
    
    @Override
    @Transactional
    public void updateOrderStatus(String orderId) {
        try {
            log.info("开始更新订单状态，订单ID: {}", orderId);
            Order order = ordersRepository.findByOrderId(Integer.parseInt(orderId));
            
            if (order == null) {
                log.error("找不到订单, 订单ID: {}", orderId);
                return;
            }
            
            // 仅当订单状态为PENDING时更新，避免重复更新
            if ("PENDING".equals(order.getStatus())) {
                log.info("订单状态从PENDING更新为SUCCESS，订单ID: {}", orderId);
                order.setStatus("SUCCESS");
                ordersRepository.save(order);
            } else {
                log.info("订单已经处理过，当前状态: {}，订单ID: {}", order.getStatus(), orderId);
            }
        } catch (Exception e) {
            log.error("更新订单状态失败，订单ID: {}", orderId, e);
            throw e; // 重新抛出异常，确保事务回滚
        }
    }

    @Override
    @Transactional
    public void reduceStock(String orderId) {
        try {
            log.info("开始减少库存，订单ID: {}", orderId);
            List<CartsOrdersRelation> relations = corRepository.findByOrderId(Integer.parseInt(orderId));
            
            if (relations == null || relations.isEmpty()) {
                log.warn("订单没有关联的购物车项，订单ID: {}", orderId);
                return;
            }
            
            for (CartsOrdersRelation relation : relations) {
                String cartItemId = relation.getCartItemId();
                if (cartItemId == null) {
                    log.warn("关联中的购物车项ID为空");
                    continue;
                }
                
                com.example.tomatomall.po.Cart cartItem = cartRepository.findByCartItemId(cartItemId);
                if (cartItem == null) {
                    log.warn("找不到购物车项，购物车项ID: {}", cartItemId);
                    continue;
                }
                
                String productId = cartItem.getProductId();
                int quantity = cartItem.getQuantity();
                
                if (productId == null) {
                    log.warn("购物车项没有关联商品ID，购物车项ID: {}", cartItemId);
                    continue;
                }
                
                Stockpile stockpile = stockpileRepository.findById(Integer.parseInt(productId));
                if (stockpile == null) {
                    log.warn("找不到商品库存信息，商品ID: {}", productId);
                    continue;
                }
                
                int newAmount = stockpile.getAmount() - quantity;
                if (newAmount < 0) {
                    log.error("库存不足，商品ID: {}, 当前库存: {}, 请求数量: {}", 
                            productId, stockpile.getAmount(), quantity);
                    newAmount = 0; // 防止库存为负数
                }
                
                log.info("更新商品库存，商品ID: {}, 原库存: {}, 减少数量: {}, 新库存: {}", 
                        productId, stockpile.getAmount(), quantity, newAmount);
                stockpile.setAmount(newAmount);
                stockpileRepository.save(stockpile);
            }
            
            log.info("库存减少完成，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("减少库存失败，订单ID: {}", orderId, e);
            throw e; // 重新抛出异常，确保事务回滚
        }
    }
}
