package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.OrderException;
import com.example.tomatomall.dto.PaymentResponseDTO;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.repository.OrdersRepository;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersRepository ordersRepository;
    private final AliPayController aliPayController;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping
    public Result<List<OrderVO>> getOrders() {
        // 获取当前用户ID
        Integer userId = securityUtil.getCurrentAccount().getId();

        // 查询当前用户的所有订单
        List<Order> orders = ordersRepository.findAll();
        List<Order> userOrders = new ArrayList<>();

        // 过滤出当前用户的订单
        for (Order order : orders) {
            if (order.getUserId().equals(userId)) {
                userOrders.add(order);
            }
        }

        // 转换为VO对象列表
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Order order : userOrders) {
            OrderVO orderVO = new OrderVO();
            orderVO.setOrderId(order.getOrderId());
            orderVO.setUserId(order.getUserId());
            orderVO.setTotalAmount(order.getTotalAmount());
            orderVO.setPaymentMethod(order.getPaymentMethod());
            orderVO.setStatus(order.getStatus());
            orderVO.setCreateTime(order.getCreateTime());
            orderVOList.add(orderVO);
        }

        return Result.success(orderVOList);
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Integer orderId) {
        // 获取当前用户ID
        Integer userId = securityUtil.getCurrentAccount().getId();

        // 查询订单
        Order order = ordersRepository.findByOrderId(orderId);

        // 检查订单是否存在
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }

        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            return Result.fail(403, "您无权查看此订单");
        }

        // 转换为VO对象
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(order.getOrderId());
        orderVO.setUserId(order.getUserId());
        orderVO.setTotalAmount(order.getTotalAmount());
        orderVO.setPaymentMethod(order.getPaymentMethod());
        orderVO.setStatus(order.getStatus());
        orderVO.setCreateTime(order.getCreateTime());

        return Result.success(orderVO);
    }

    @PostMapping("/{orderId}/pay")
    public Result<PaymentResponseDTO> payOrder(@PathVariable Integer orderId) {
        // 先验证订单是否存在且属于当前用户
        Integer userId = securityUtil.getCurrentAccount().getId();
        Order order = ordersRepository.findByOrderId(orderId);
        
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作此订单");
        }
        
        try {
            // 调用AliPayController的支付方法
            com.example.tomatomall.vo.Response<PaymentResponseDTO> response = aliPayController.initiatePayment(orderId);
            
            // 检查支付是否成功初始化
            if ("200".equals(response.getCode())) {
                return Result.success(response.getData());
            } else {
                return Result.fail(Integer.parseInt(response.getCode()), response.getMsg());
            }
        } catch (OrderException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return Result.fail(500, "支付初始化失败: " + e.getMessage());
        }
    }
}