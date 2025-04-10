package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.AdvertisementNotFoundException;
import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.AdvertisementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @GetMapping
    public Result<List<AdvertisementVO>> getAdvertisements() {
        List<AdvertisementVO> advertisementList = advertisementService.getAdvertisements();
        return Result.success(advertisementList);
    }

    @PutMapping
    public Result<String> updateAdvertisement(@RequestBody AdvertisementVO advertisement) {
        try {
            advertisementService.updateAdvertisement(advertisement);
            return Result.success("更新成功");
        } catch (AdvertisementNotFoundException e) {
            return Result.fail(404, "广告不存在");
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新广告失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping
    public Result<AdvertisementVO> createAdvertisement(@RequestBody AdvertisementVO advertisement) {
        try {
            AdvertisementVO createdAd = advertisementService.createAdvertisement(advertisement);
            return Result.success(createdAd);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建广告失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAdvertisement(@PathVariable String id) {
        try {
            advertisementService.deleteAdvertisement(id);
            return Result.success("删除成功");
        } catch (AdvertisementNotFoundException e) {
            return Result.fail(404, "广告不存在");
        } catch (Exception e) {
            log.error("删除广告失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }
}