package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.MemberNotFoundException;
import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.service.MemberService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.MemberVO;
import com.example.tomatomall.vo.PointRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;


    @GetMapping
    public Result<List<MemberVO>> getAllMembers() {
        List<MemberVO> memberList = memberService.getAllMembers();
        return Result.success(memberList);
    }

    @GetMapping("/{id}")
    public Result<MemberVO> getMemberById(@PathVariable String id) {
        try {
            MemberVO member = memberService.getMemberById(id);
            return Result.success(member);
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("获取会员信息失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping
    public Result<MemberVO> createMember(@RequestBody MemberVO member) {
        try {
            MemberVO createdMember = memberService.createMember(member);
            return Result.success(createdMember);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建会员失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PutMapping
    public Result<MemberVO> updateMember(@RequestBody MemberVO member) {
        try {
            MemberVO updatedMember = memberService.updateMember(member);
            return Result.success(updatedMember);
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新会员信息失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMember(@PathVariable String id) {
        try {
            memberService.deleteMember(id);
            return Result.success("删除成功");
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("删除会员失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @GetMapping("/account/{accountId}")
    public Result<MemberVO> getMemberByAccountId(@PathVariable Integer accountId) {
        try {
            MemberVO member = memberService.getMemberByAccountId(accountId);
            return Result.success(member);
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("获取会员信息失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }







    @PostMapping("/{memberId}/upgrade")
    public Result<MemberVO> upgradeMemberLevel(@PathVariable String memberId) {
        try {
            MemberVO member = memberService.upgradeMemberLevel(memberId);
            return Result.success(member);
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("升级会员等级失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping("/{memberId}/record-login")
    public Result<String> recordLogin(@PathVariable String memberId) {
        try {
            memberService.recordLogin(memberId);
            return Result.success("登录记录成功");
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("记录登录失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }
}