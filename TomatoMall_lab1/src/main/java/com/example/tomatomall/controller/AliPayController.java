package com.example.tomatomall.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.example.tomatomall.TomatoException.OrderException;
import com.example.tomatomall.TomatoException.TomatoException;
import com.example.tomatomall.dto.PaymentResponseDTO;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.repository.OrdersRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.vo.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/alipay")
@RequiredArgsConstructor
public class AliPayController {
    private final OrderService orderService;
    private final OrdersRepository ordersRepository;

    @Value("${alipay.app-id}")
    private String appId;
    
    @Value("${alipay.private-key}")
    private String privateKey;
    
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    
    @Value("${alipay.server-url}")
    private String serverUrl;
    
    @Value("${alipay.charset}")
    private String charset;
    
    @Value("${alipay.sign-type}")
    private String signType;
    
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    
    @Value("${alipay.return-url}")
    private String returnUrl;
    
    private static final String FORMAT = "JSON";

    /**
     * 发起支付请求
     */
    @PostMapping("/orders/{orderId}/pay")
    public Response<PaymentResponseDTO> initiatePayment(@PathVariable Integer orderId) {
        // 1. 查询订单信息
        Order order = ordersRepository.findByOrderId(orderId);
        if (order == null) {
            throw OrderException.orderNotFound();
        }

        log.info("初始化支付，订单ID: {}, 金额: {}", orderId, order.getTotalAmount());
        log.info("支付宝配置 - APPID: {}, 服务器URL: {}", appId, serverUrl);

        // 2. 创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                serverUrl, appId, privateKey, FORMAT, charset, alipayPublicKey, signType
        );

        // 3. 创建支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        // 4. 设置业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderId()); // 订单号
        bizContent.put("total_amount", order.getTotalAmount()); // 订单金额
        bizContent.put("subject", "番茄书城订单-" + order.getOrderId()); // 订单标题
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 固定配置
        request.setBizContent(bizContent.toString());

        // 5. 生成支付表单
        String paymentForm = "";
        try {
            paymentForm = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
            log.info("支付宝表单生成成功");
        } catch (AlipayApiException e) {
            log.error("生成支付表单失败", e);
            throw OrderException.buildPaymentFormFailure();
        }
        
        // 6. 返回支付表单给前端渲染
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setPaymentForm(paymentForm);
        paymentResponseDTO.setOrderId(order.getOrderId());
        paymentResponseDTO.setTotalAmount(order.getTotalAmount());
        paymentResponseDTO.setPaymentMethod("Alipay");

        return Response.buildSuccess(paymentResponseDTO);
    }

    /**
     * 处理支付宝异步通知
     */
    @PostMapping("/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        log.info("接收到支付宝异步通知");
        
        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
        
        log.info("支付宝通知参数: {}", params);

        // 2. 验证支付宝签名（防止伪造请求）- 使用配置文件中的公钥
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
        
        if (!signVerified) {
            log.error("支付宝签名验证失败");
            response.getWriter().print("fail"); // 签名验证失败，返回 fail
            return;
        }

        // 3. 处理业务逻辑（更新订单、减库存等）
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            String orderId = params.get("out_trade_no"); // 订单号
            String alipayTradeNo = params.get("trade_no"); // 支付宝交易号
            String totalAmount = params.get("total_amount"); // 支付金额
            String paymentTime = params.get("gmt_payment"); // 支付完成时间
            
            log.info("订单 {} 支付成功，支付宝交易号: {}, 支付金额: {}, 支付时间: {}", 
                    orderId, alipayTradeNo, totalAmount, paymentTime);
            
            try {
                // 更新订单状态（注意幂等性，防止重复处理）
                orderService.updateOrderStatus(orderId);
                
                // 扣减库存
                orderService.reduceStock(orderId);
                
                log.info("订单处理完成: {}", orderId);
            } catch (Exception e) {
                log.error("处理订单时出错: {}", orderId, e);
                response.getWriter().print("fail"); // 处理失败，支付宝会重试
                return;
            }
        } else {
            log.info("非成功交易状态: {}, 订单号: {}", tradeStatus, params.get("out_trade_no"));
        }

        // 4. 必须返回纯文本的 "success"（支付宝要求）
        response.getWriter().print("success");
        log.info("支付宝异步通知处理完成");
    }

    /**
     * 支付宝同步回调接口
     */
    @GetMapping("/returnUrl")
    public String returnUrl() {
        return "支付成功，请返回商城查看订单状态";
    }
}