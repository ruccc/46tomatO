package com.example.tomatomall.TomatoException;

public class TomatoException extends RuntimeException {
    public TomatoException(String message) {
        super(message);
    }

    public static TomatoException notLogin() {
        return new TomatoException("未登录!");
    }

    public static TomatoException phoneAlreadyExist() {
        return new TomatoException("用户名已存在");
    }

    public static TomatoException notFound() {
        return new TomatoException("用户不存在!");
    }
}
