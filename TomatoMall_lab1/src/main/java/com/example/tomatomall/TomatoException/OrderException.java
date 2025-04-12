package com.example.tomatomall.TomatoException;

public class OrderException extends RuntimeException {
  public OrderException(String message) {
    super(message);
  }
  public static TomatoException orderNotFound() {
    return new TomatoException("订单不存在");
  }
  public static TomatoException buildPaymentFormFailure() {
    return new TomatoException("生成支付表单失败");
  }
}
