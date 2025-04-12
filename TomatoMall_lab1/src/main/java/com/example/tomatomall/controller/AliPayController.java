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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/alipay")
public class AliPayController {
    OrderService orderService;
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAruzwcv5FVltvkH3aDhWRU1KudImvwNjVIA+t8J9+4QfQKGi/MvfITGsVLKi6oaYtnkJsqJD5dgm/Q65pZ9amAPVmGQVI4tOxfh04AvjGgTNRSwGw9aYX6dwOw3Mq9pxxFyFe6B/lUGeyVJuD+mwPzcBm2uyvPzkFE+7DQaNNPOl2k1rgseb4oyHGMEuTKHY/sMcB1L2kdB0CNYQHjke0AJRIU9Oy4U30m8yMa6c9qUwg+z8mqBHWjUVoODR1oP5iratkrUhU/+TiK4IafBLc6l9vYKuiX0PqJ5D2rK7VM+GQ7LwRYZacdQLrg+w1Pbz9t5eDNVXVPdqEHO/n0Rhy6wIDAQAB";
    OrdersRepository ordersRepository;
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

    //vvvyyv9548@sandbox.com    支付邮箱
//    @GetMapping("/pay") //subject=xxx&traceNo=xxx&totalAmount=xxx
//    public void pay(AliPay aliPay, HttpServletResponse httpResponse) throws Exception {
//        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
//        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId,
//                privateKey, FORMAT, charset, alipayPublicKey, signType);
//        // 2. 创建 Request并设置Request参数
//        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
//        request.setNotifyUrl(notifyUrl);
//        request.setReturnUrl(returnUrl);
//        JSONObject bizContent = new JSONObject();
//        bizContent.put("out_trade_no", aliPay.getTraceNo());  // 我们自己生成的订单编号
//        bizContent.put("total_amount", aliPay.getTotalAmount()); // 订单的总金额
//        bizContent.put("subject", aliPay.getSubject());   // 支付的名称
//        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
//        request.setBizContent(bizContent.toString());
//        // 执行请求，拿到响应的结果，返回给浏览器
//        String form = "";
//        try {
//            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        httpResponse.setContentType("text/html;charset=" + charset);
//        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();
//    }
    @PostMapping("/orders/{orderId}/pay")
    public Response<PaymentResponseDTO> initiatePayment(@PathVariable Integer orderId) {
        // 1. 查询订单信息（实际中应通过 OrderService 查询数据库）
        Order order = ordersRepository.findByOrderId(orderId);
        if (order == null) {
            throw OrderException.orderNotFound();
        }

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
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 固定配置
        request.setBizContent(bizContent.toString());

        // 5. 生成支付表单
        String paymentForm = "";
        try {
            paymentForm = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw OrderException.buildPaymentFormFailure();
        }
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setPaymentForm(paymentForm);
        paymentResponseDTO.setOrderId(order.getOrderId());
        paymentResponseDTO.setTotalAmount(order.getTotalAmount());
        paymentResponseDTO.setPaymentMethod("Alipay");


        return Response.buildSuccess(paymentResponseDTO);
    }

    @PostMapping("/api/orders/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 2. 验证支付宝签名（防止伪造请求）
        boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");
        if (!signVerified) {
            response.getWriter().print("fail"); // 签名验证失败，返回 fail
            return;
        }

        // 3. 处理业务逻辑（更新订单、减库存等）
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            String orderId = params.get("out_trade_no"); // 您的订单号
            String alipayTradeNo = params.get("trade_no"); // 支付宝交易号
            String totalAmount = params.get("total_amount"); // 支付金额
            String paymentTime = params.get("gmt_payment");//支付完成时间
            // 更新订单状态（注意幂等性，防止重复处理）
            orderService.updateOrderStatus(orderId);
            // 扣减库存（建议加锁或乐观锁）还没加
            orderService.reduceStock(orderId);
        }

        // 4. 必须返回纯文本的 "success"（支付宝要求）
        response.getWriter().print("success");
    }

    @GetMapping("/returnUrl")
    public String returnUrl() {
        return "支付成功了";
    }
}