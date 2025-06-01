package com.example.tomatomall.vo;

import com.example.tomatomall.po.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountVO {
    private Integer id;

    private String username;

    private String password;

    private String name;

    private String avatar;

    private String role;

    private String telephone;

    private String email;

    private String location;

    public String memberLevel;

    public Account toPO(){
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setName(name);
        account.setAvatar(avatar);
        account.setRole(role);

        account.setTelephone(telephone);
        account.setEmail(email);
        account.setLocation(location);

        account.setMemberLevel(memberLevel);

        return account;
    }
}
