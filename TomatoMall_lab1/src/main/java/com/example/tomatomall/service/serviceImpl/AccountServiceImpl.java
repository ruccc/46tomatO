package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.TomatoException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    final
    AccountRepository accountRepository;
    final
    TokenUtil tokenUtil;
    final
    SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(TokenUtil tokenUtil, SecurityUtil securityUtil, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.tokenUtil = tokenUtil;
        this.securityUtil = securityUtil;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }    @Override
    public AccountVO getAccountInfo(String username) throws TomatoException {
        log.info("查找用户: {}", username);
        Account account = accountRepository.findByUsername(username);
        log.info("查找结果: {}", account != null ? "找到用户" : "未找到用户");
        if (account == null) {
            log.warn("用户不存在: {}", username);
            throw TomatoException.notFound();
        }
        log.info("返回用户信息: id={}, username={}, name={}", account.getId(), account.getUsername(), account.getName());
        return account.toVO();
    }    @Override
    public String createAccount(AccountVO accountVO) {
        // 检查用户名是否已存在
        Account existingUserByUsername = accountRepository.findByUsername(accountVO.getUsername());
        if (existingUserByUsername != null) {
            log.warn("注册失败 - 用户名已存在: {}", accountVO.getUsername());
            return "用户名已存在";
        }
        
        // 检查电话号码是否已存在
        Account existingUserByTelephone = accountRepository.findByTelephone(accountVO.getTelephone());
        if (existingUserByTelephone != null) {
            log.warn("注册失败 - 电话号码已存在: {}", accountVO.getTelephone());
            return "电话号码已存在";
        }
        
        // 创建新用户
        Account newAccount = accountVO.toPO();
        newAccount.setPassword(passwordEncoder.encode(accountVO.getPassword()));
        Account savedAccount = accountRepository.save(newAccount);
        
        log.info("用户注册成功 - ID: {}, 用户名: {}, 姓名: {}", 
                savedAccount.getId(), savedAccount.getUsername(), savedAccount.getName());
        return "注册成功";
    }    @Override
    public String login(String username, String password) throws TomatoException {
        log.info("用户尝试登录 - 用户名: {}", username);
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            log.warn("登录失败 - 用户不存在: {}", username);
            throw TomatoException.notFound();
        }
        log.info("找到用户 - ID: {}, 用户名: {}, 姓名: {}", account.getId(), account.getUsername(), account.getName());
        
        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.warn("登录失败 - 密码错误: {}", username);
            throw TomatoException.wrongPassword();
        }
        
        log.info("用户登录成功: {}", username);
        return tokenUtil.getToken(account);
    }

    @Override
    public String updateAccount(AccountVO accountVO) {
        Account account = securityUtil.getCurrentAccount();

        // 只更新非空字段
        if (accountVO.getName() != null) {
            account.setName(accountVO.getName());
        }
        if (accountVO.getLocation() != null) {
            account.setLocation(accountVO.getLocation());
        }
        if (accountVO.getTelephone() != null) {
            account.setTelephone(accountVO.getTelephone());
        }
        if (accountVO.getEmail() != null) {
            account.setEmail(accountVO.getEmail());
        }
        if (accountVO.getAvatar() != null) {
            account.setAvatar(accountVO.getAvatar());
        }
        if (accountVO.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(accountVO.getPassword()));
        }
        if (accountVO.getRole() != null) {
            account.setRole(accountVO.getRole());
        }
        if (accountVO.getMemberLevel() != null) {
            account.setMemberLevel(accountVO.getMemberLevel());
        }

        accountRepository.save(account);
        return "更新成功";
    }

    public AccountVO getAccountById(Integer id){
        return accountRepository.findById(id).get().toVO();
    }

    @Override
    public Account getAccountEntityById(Integer id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("用户不存在，ID: " + id)
        );
    }
}
