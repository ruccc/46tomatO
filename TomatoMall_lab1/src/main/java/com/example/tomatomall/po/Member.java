package com.example.tomatomall.po;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Data
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId; // 关联Account表

    @Column(name = "member_no", unique = true, nullable = false)
    private String memberNo; // 会员编号

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "points")
    private Integer points = 0;

    @Column(name = "level")
    private Integer level = 1; // 会员等级，默认1级

    @Column(name = "growth_value")
    private Integer growthValue = 0; // 成长值

    @Column(name = "birthday")
    private String birthday; // 生日，格式yyyy-MM-dd

    @Column(name = "gender")
    private String gender; // 性别: M-男, F-女, U-未知

    @Column(name = "status")
    private Integer status = 1; // 状态: 1-正常, 0-禁用

    @Column(name = "register_time")
    private String registerTime; // 注册时间

    @Column(name = "last_login_time")
    private String lastLoginTime; // 最后登录时间
}