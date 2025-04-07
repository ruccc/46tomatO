package com.example.tomatomall.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


import java.util.Date;

@Component
public class TokenUtil {
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    @Autowired
    AccountRepository accountRepository;

    public String getToken(Account account) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 不再使用密码作为密钥，改为使用固定密钥或从配置读取
        String secret = "your-secret-key"; // 应该从配置文件中读取
        return JWT.create()
                .withAudience(String.valueOf(account.getId()))
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean verifyToken(String token) {
        try {
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            Account account = accountRepository.findById(userId).orElse(null);
            if (account == null) {
                return false;
            }
            String secret = "your-secret-key"; // 应该与getToken中的一致
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Account getAccount(String token) {
        Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
        return accountRepository.findById(userId).orElse(null);
    }
}