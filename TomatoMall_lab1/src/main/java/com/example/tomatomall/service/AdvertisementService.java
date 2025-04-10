package com.example.tomatomall.service;

import com.example.tomatomall.vo.AdvertisementVO;

import java.util.List;

public interface AdvertisementService {
    List<AdvertisementVO> getAdvertisements();
    AdvertisementVO updateAdvertisement(AdvertisementVO advertisement);
    AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO);
    void deleteAdvertisement(String id);
}
