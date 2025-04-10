package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.AdvertisementNotFoundException;
import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdvertisementServiceImpl implements AdvertisementService {
    private final AdvertisementRepository advertisementRepository;

    public AdvertisementServiceImpl(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    @Override
    public List<AdvertisementVO> getAdvertisements() {
        List<Advertisement> advertisements = advertisementRepository.findAll();
        List<AdvertisementVO> result = new ArrayList<>();
        for (Advertisement advertisement : advertisements) {
            result.add(convertToVO(advertisement));
        }
        return result;
    }

    @Override
    public AdvertisementVO updateAdvertisement(AdvertisementVO advertisementVO) {
        if (!advertisementRepository.existsById(advertisementVO.getId())) {
            throw new AdvertisementNotFoundException();
        }

        Advertisement advertisement = new Advertisement();
        advertisement.setId(advertisementVO.getId());
        advertisement.setTitle(advertisementVO.getTitle());
        advertisement.setContent(advertisementVO.getContent());
        advertisement.setImgUrl(advertisementVO.getImgUrl());
        advertisement.setProductId(advertisementVO.getProductId());

        Advertisement updatedAd = advertisementRepository.save(advertisement);
        return convertToVO(updatedAd);
    }

    @Override
    public AdvertisementVO createAdvertisement(AdvertisementVO advertisementVO) {
        if (advertisementVO.getProductId() == null || advertisementVO.getProductId().trim().isEmpty()) {
            throw new BusinessException("商品ID不能为空");
        }

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementVO.getTitle());
        advertisement.setContent(advertisementVO.getContent());
        advertisement.setImgUrl(advertisementVO.getImgUrl());
        advertisement.setProductId(advertisementVO.getProductId());

        Advertisement savedAd = advertisementRepository.save(advertisement);
        return convertToVO(savedAd);
    }

    @Override
    public void deleteAdvertisement(String id) {
        if (!advertisementRepository.existsById(id)) {
            throw new AdvertisementNotFoundException();
        }
        advertisementRepository.deleteById(id);
    }

    private AdvertisementVO convertToVO(Advertisement advertisement) {
        AdvertisementVO vo = new AdvertisementVO();
        vo.setId(advertisement.getId());
        vo.setTitle(advertisement.getTitle());
        vo.setContent(advertisement.getContent());
        vo.setImgUrl(advertisement.getImgUrl());
        vo.setProductId(advertisement.getProductId());
        return vo;
    }
}