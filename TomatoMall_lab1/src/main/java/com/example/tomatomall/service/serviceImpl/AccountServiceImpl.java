package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.TomatoException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TokenUtil tokenUtil;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public AccountVO getAccountInfo(String username) throws TomatoException {
        Account account = accountRepository.findByUsername(username);
        if (account==null) {
            throw TomatoException.notFound(); // 需要先添加这个异常方法
        }
        return account.toVO();
    }

    @Override
    public String createAccount(AccountVO accountVO) {
        Account account =accountRepository.findByTelephone((accountVO.getTelephone()));
        if (account != null) {
            return "用户名已存在";
        }
       Account newAccount = accountVO.toPO();
        newAccount.setPassword(passwordEncoder.encode(accountVO.getPassword()));
        accountRepository.save(newAccount);
        return "注册成功";
    }

    @Override
    public String login(String username, String password) throws TomatoException {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw TomatoException.notLogin();
        }
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw TomatoException.notLogin(); // 或者创建一个密码错误的异常
        }
        return tokenUtil.getToken(account);
    }

    @Override
    public String updateAccount(AccountVO accountVO) {
        Account account = securityUtil.getCurrentAccount();


        if(accountVO.getName()!=null){
            account.setName(accountVO.getName());
        }

        if(accountVO.getLocation()!=null){
            account.setLocation(accountVO.getLocation());
        }
        if(accountVO.getTelephone()!=null){
            account.setTelephone(accountVO.getTelephone());
        }
        if(accountVO.getEmail()!=null){
            account.setEmail(accountVO.getEmail());
        }
        if(accountVO.getAvatar()!=null){
            account.setAvatar(accountVO.getAvatar());
        }
        if (accountVO.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(accountVO.getPassword()));
        }
        if (accountVO.getRole() != null) {
            account.setRole(accountVO.getRole());
        }

        accountRepository.save(account);
        return "更新成功";
    }
}
