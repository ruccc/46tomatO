package com.example.tomatomall.service;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.vo.AccountVO;

public interface AccountService {
    AccountVO getAccountInfo(String username);
    String createAccount(AccountVO accountVO);
String login(String username, String password);
String updateAccount(AccountVO accountVO);

    AccountVO getAccountById(Integer id);
    Account getAccountEntityById(Integer id);
}
