package com.example.tomatomall.configure;


import com.example.tomatomall.TomatoException.TomatoException;
import com.example.tomatomall.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    final
    TokenUtil tokenUtil;

    public LoginInterceptor(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 排除注册接口和支付宝回调接口
        if ("/api/accounts".equals(uri) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        
        // 排除支付宝回调接口
        if (uri.startsWith("/alipay/notify") || uri.startsWith("/alipay/returnUrl")) {
            return true;
        }
        
        // 排除静态资源请求
        if (uri.contains(".") && (
            uri.endsWith(".ico") || 
            uri.endsWith(".js") || 
            uri.endsWith(".css") || 
            uri.endsWith(".png") || 
            uri.endsWith(".jpg") || 
            uri.endsWith(".jpeg") || 
            uri.endsWith(".gif") || 
            uri.endsWith(".svg"))) {
            return true;
        }
        
        String token = request.getHeader("token");
        if (token != null && tokenUtil.verifyToken(token)) {
            request.getSession().setAttribute("currentAccount", tokenUtil.getAccount(token));
            return true;
        } else {
            throw TomatoException.notLogin();
        }
    }

}
