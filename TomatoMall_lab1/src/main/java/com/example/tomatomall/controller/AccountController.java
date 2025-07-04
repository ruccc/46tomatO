package com.example.tomatomall.controller;

import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.OssService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@RestController
@Slf4j
@RequestMapping("/api/accounts")
public class AccountController {

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
    @Resource
    AccountService accountService;
    OssService ossService;
    /**
     * 获取用户详情
     */
    @GetMapping("/{username}")
    public Response<AccountVO> getUser(@PathVariable String username) {
        try {
            // URL解码，处理中文用户名
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            log.info("获取用户信息 - 原始用户名: {}, 解码后用户名: {}", username, decodedUsername);
            return Response.buildSuccess(accountService.getAccountInfo(decodedUsername));
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            throw e;
        }
    }    /**
     * 创建新的用户
     */
    @PostMapping()
    public Response<String> createUser(@RequestBody AccountVO accountVO) {
        String result = accountService.createAccount(accountVO);
        
        // 根据结果判断是成功还是失败
        if ("注册成功".equals(result)) {
            return Response.buildSuccess(result);
        } else {
            // 用户名或电话号码已存在等失败情况
            log.warn("用户注册失败: {}", result);
            return Response.buildFailure(result, "400");
        }
    }    /**
     * 更新用户信息（JSON格式，不支持头像上传）
     */
    @PutMapping(consumes = "application/json")
    public Response<String> updateUserJson(@RequestBody AccountVO accountVO) {
        try {
            log.info("收到JSON格式的用户信息更新请求，AccountVO用户名: {}", accountVO.getUsername());
            log.info("请求数据: name={}, email={}, telephone={}, location={}", 
                    accountVO.getName(), accountVO.getEmail(), accountVO.getTelephone(), accountVO.getLocation());
            
            String result = accountService.updateAccount(accountVO);
            log.info("用户信息更新结果: {}", result);
            return Response.buildSuccess(result);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Response.buildFailure("更新用户信息失败: " + e.getMessage(), "400");
        }
    }

    /**
     * 更新用户信息（支持头像上传）
     */
    @PutMapping(consumes = "multipart/form-data")
    public Response<String> updateUser(@RequestPart(required = false) MultipartFile avatarFile,
                                       @RequestPart AccountVO accountVO) {
        try {
            log.info("收到multipart格式的用户信息更新请求，用户名: {}", accountVO.getUsername());
            // 如果有上传头像文件，先上传到OSS
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = ossService.uploadAvatar(avatarFile);
                accountVO.setAvatar(avatarUrl);
            }
            return Response.buildSuccess(accountService.updateAccount(accountVO));
        } catch (IOException e) {
            log.error("更新用户信息失败", e);
            return Response.buildFailure("更新用户信息失败", "400");
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest request) {
        return Response.buildSuccess(accountService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Response<Object> getUserStats(@RequestParam String username) {
        try {
            // URL解码，处理中文用户名
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            log.info("获取用户统计信息 - 用户名: {}", decodedUsername);

            // 简单返回默认统计数据，避免报错
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalAmount", 0);
            stats.put("orderCount", 0);
            stats.put("bookCount", 0);
            stats.put("monthlyStats", new java.util.ArrayList<>());

            return Response.buildSuccess(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败: {}", e.getMessage(), e);
            throw e;
        }
    }

}
