package com.example.tomatomall.po;

import com.example.tomatomall.vo.AccountVO;
import lombok.*;
import javax.persistence.*;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "user_name")
    private String username;

    @Basic
    @Column(name = "password", length = 100)
    private String password;

    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "avatar")
    private String avatar;

    @Basic
    @Column(name = "role")
    private String role;

    @Basic
    @Column(name = "telephone")
    private String telephone;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "member_level")
    private String memberLevel;

    @Basic
    @Column(name = "location")
    private String location;
    public AccountVO toVO(){
        AccountVO accountVO = new AccountVO();
        accountVO.setId(id);
        accountVO.setUsername(username);
        accountVO.setPassword(password);
        accountVO.setName(name);
        accountVO.setAvatar(avatar);

        accountVO.setRole(role);
        accountVO.setTelephone(telephone);
        accountVO.setEmail(email);
        accountVO.setLocation(location);

        accountVO.setMemberLevel(memberLevel);

        return accountVO;
    }

}
