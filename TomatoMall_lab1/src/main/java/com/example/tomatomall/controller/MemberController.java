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

    // 原有方法保持不变...

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

    @PostMapping("/{memberId}/points/add")
    public Result<String> addPoints(@PathVariable String memberId,
                                    @RequestParam Integer points,
                                    @RequestParam String source) {
        try {
            memberService.addPoints(memberId, points, source);
            return Result.success("积分添加成功");
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (Exception e) {
            log.error("添加积分失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping("/{memberId}/points/deduct")
    public Result<String> deductPoints(@PathVariable String memberId,
                                       @RequestParam Integer points,
                                       @RequestParam String remark) {
        try {
            memberService.deductPoints(memberId, points, remark);
            return Result.success("积分扣除成功");
        } catch (MemberNotFoundException e) {
            return Result.fail(404, "会员不存在");
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("扣除积分失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @GetMapping("/{memberId}/points/records")
    public Result<List<PointRecordVO>> getPointRecords(@PathVariable String memberId) {
        try {
            List<PointRecordVO> records = memberService.getPointRecords(memberId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取积分记录失败", e);
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