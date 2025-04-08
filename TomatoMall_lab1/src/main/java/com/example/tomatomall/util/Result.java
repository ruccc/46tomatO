package com.example.tomatomall.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private T data;
    private String msg;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, data, null);
    }

    public static Result<Void> success() {
        return new Result<>(200, null, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, null, msg);
    }
}