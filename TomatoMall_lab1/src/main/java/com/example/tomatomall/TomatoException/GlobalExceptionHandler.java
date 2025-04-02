package com.example.tomatomall.TomatoException;


import com.example.tomatomall.vo.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: DingXiaoyu
 * @Date: 0:26 2023/11/26
 * 这个类能够接住项目中所有抛出的异常，
 * 使用了RestControllerAdvice切面完成，
 * 表示所有异常出现后都会通过这里。
 * 这个类将异常信息封装到ResultVO中进行返回。
*/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = TomatoException.class)
    public Response<String> handleAIExternalException(TomatoException e) {
        e.printStackTrace();
        String statusCode = "401";
        if (e.getMessage().equals("用户不存在!")) {
            statusCode = "401";
        } else if (e.getMessage().equals("用户名已存在")) {
            statusCode = "400";
        } else if (e.getMessage().equals("未登录")) {
            statusCode = "400";
        }
        return Response.buildFailure(e.getMessage(), statusCode);
    }

}
