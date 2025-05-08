package com.example.tomatomall.controller;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


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

    /**
     * 获取用户详情
     */
    @GetMapping("/{username}")
    public Response<AccountVO> getUser(@PathVariable String username) {

        return Response.buildSuccess(accountService.getAccountInfo(username));
    }

    /**
     * 创建新的用户
     */
    @PostMapping()
    public Response<String> createUser(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.createAccount(accountVO));
    }

    /**
     * 更新用户信息
     */
    @PutMapping()
    public Response<String> updateUser(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));

    }


    /**
     * 登录
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest request) {
        return Response.buildSuccess(accountService.login(request.getUsername(), request.getPassword()));
    }
}
