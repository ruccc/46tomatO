package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.CartsOrdersRelation;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrdersRepository ordersRepository;
    CORRepository corRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;
    StockpileRepository stockpileRepository;
    @Override
    public void updateOrderStatus(String orderId) {
        Order order = ordersRepository.findByOrderId(Integer.parseInt(orderId));//
        order.setStatus("SUCCESS");
        ordersRepository.save(order);
    }

    @Override
    public void reduceStock(String orderId) {
//        Order order = ordersRepository.findByOrderId(Integer.parseInt(orderId));
        List<CartsOrdersRelation> l_cartsOrdersRelation = corRepository.findByOrderId(Integer.parseInt(orderId));
        for (CartsOrdersRelation c : l_cartsOrdersRelation) {
            String cart_item_id = c.getCartItemId();
            String product_id=cartRepository.findByCartItemId(cart_item_id).getProductId();
            Stockpile stockpile=stockpileRepository.findById(Integer.parseInt(product_id));
            int newAmount = stockpile.getAmount() -1;
            if (newAmount < 0) {
                throw new IllegalArgumentException("调整后的库存不能小于0");
            }
            stockpile.setAmount(newAmount);
            stockpileRepository.save(stockpile);
        }
    }
}
