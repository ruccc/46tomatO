package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Account;
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
    private AccountRepository accountRepository;

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

                // 检查订单中是否包含会员卡商品
                checkAndUpdateMemberLevel(order);
            } else {
                log.info("订单已经处理过，当前状态: {}，订单ID: {}", order.getStatus(), orderId);
            }
        } catch (Exception e) {
            log.error("更新订单状态失败，订单ID: {}", orderId, e);
            throw e;
        }
    }

    private void checkAndUpdateMemberLevel(Order order) {
        // 获取订单关联的所有商品
        List<CartsOrdersRelation> relations = corRepository.findByOrderId(order.getOrderId());

        if (relations == null || relations.isEmpty()) {
            return;
        }

        // 检查是否有会员卡商品
        for (CartsOrdersRelation relation : relations) {
            String cartItemId = relation.getCartItemId();
            if (cartItemId == null) {
                continue;
            }

            com.example.tomatomall.po.Cart cartItem = cartRepository.findByCartItemId(cartItemId);
            if (cartItem == null) {
                continue;
            }

            String productId = cartItem.getProductId();


            // 检查是否是会员卡商品(1/2/3)
            if (isMembershipCard(productId)) {
                // 更新用户会员等级

                if (productId.equals("dcaf11e7-c1a8-42c7-a51e-bc6ddb1f2628")) {
                    productId = "1";
                } else if (productId.equals("60300a88-8def-4dad-ba5d-6bbb668e6f27")) {
                    productId = "2";
                } else if (productId.equals("fc3eb463-913d-4645-bff5-a3607a901e43")) {
                    productId="3";
                }
                updateUserMemberLevel(order.getUserId(), productId);
                break; // 只需要处理一次，因为一个订单中只能有一种会员卡
            }
        }
    }

    private boolean isMembershipCard(String productId) {
        return "dcaf11e7-c1a8-42c7-a51e-bc6ddb1f2628".equals(productId) || "60300a88-8def-4dad-ba5d-6bbb668e6f27".equals(productId) || "fc3eb463-913d-4645-bff5-a3607a901e43".equals(productId);
    }

    private void updateUserMemberLevel(Integer userId, String memberLevel) {
        Account account = accountRepository.findById(userId).orElse(null);
        if (account != null) {
            account.setMemberLevel(memberLevel);
            accountRepository.save(account);
            log.info("用户 {} 会员等级已更新为 {}", userId, memberLevel);
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
