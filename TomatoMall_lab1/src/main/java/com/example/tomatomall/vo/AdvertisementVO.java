package com.example.tomatomall.vo;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdvertisementVO {
    private String id;
    private String title;
    private String content;
    private String imgUrl;
    private String productId;
}
