package com.example.tomatomall.TomatoException;

public class AdvertisementNotFoundException extends RuntimeException {
    public AdvertisementNotFoundException() {
        super("广告不存在");
    }
}
