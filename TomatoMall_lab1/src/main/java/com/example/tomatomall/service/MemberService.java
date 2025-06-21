package com.example.tomatomall.service;

import com.example.tomatomall.vo.MemberVO;
import com.example.tomatomall.vo.MemberLevelVO;
import com.example.tomatomall.vo.PointRecordVO;

import java.util.List;

public interface MemberService {
    List<MemberVO> getAllMembers();
    MemberVO getMemberById(String id);
    MemberVO getMemberByAccountId(Integer accountId);
    MemberVO createMember(MemberVO memberVO);
    MemberVO updateMember(MemberVO memberVO);
    void deleteMember(String id);


    // 会员等级相关
    MemberVO upgradeMemberLevel(String memberId);
    MemberLevelVO getMemberLevelInfo(Integer level);

    // 会员登录相关
    void recordLogin(String memberId);
}