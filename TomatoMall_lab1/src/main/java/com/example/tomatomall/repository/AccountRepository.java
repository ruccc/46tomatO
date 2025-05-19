package com.example.tomatomall.repository;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.vo.AccountVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);
    Account findByUsernameAndPassword(String username, String password);
    Account findByTelephone(String telephone);

    List<Account> findByRole(String service);
}
