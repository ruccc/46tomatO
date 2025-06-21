package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.MemberNotFoundException;
import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.MemberService;
import com.example.tomatomall.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberLevelRepository memberLevelRepository;

    private final AccountRepository accountRepository;

    public MemberServiceImpl(MemberRepository memberRepository,
                             MemberLevelRepository memberLevelRepository,

                             AccountRepository accountRepository) {
        this.memberRepository = memberRepository;
        this.memberLevelRepository = memberLevelRepository;

        this.accountRepository = accountRepository;
    }

    @Override
    public List<MemberVO> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        List<MemberVO> result = new ArrayList<>();
        for (Member member : members) {
            result.add(convertToVO(member));
        }
        return result;
    }

    @Override
    public MemberVO getMemberById(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
        return convertToVO(member);
    }

    @Override
    public MemberVO getMemberByAccountId(Integer accountId) {
        Member member = memberRepository.findByAccountId(String.valueOf(accountId))
                .orElseThrow(MemberNotFoundException::new);
        return convertToVO(member);
    }

    @Override
    public MemberVO createMember(MemberVO memberVO) {
        // 检查关联账户是否存在
        Account account = accountRepository.findById(memberVO.getAccountId())
                .orElseThrow(() -> new BusinessException("关联账户不存在"));

        // 检查会员是否已存在
        if (memberRepository.existsByAccountId(String.valueOf(memberVO.getAccountId()))) {
            throw new BusinessException("该账户已是会员");
        }

        Member member = new Member();
        member.setAccountId(memberVO.getAccountId());
        member.setMemberNo(generateMemberNo());
        member.setName(memberVO.getName());
        member.setEmail(memberVO.getEmail());
        member.setPhone(memberVO.getPhone());
        member.setAddress(memberVO.getAddress());
        member.setPoints(0);
        member.setLevel(1);
        member.setGrowthValue(0);
        member.setBirthday(memberVO.getBirthday());
        member.setGender(memberVO.getGender());
        member.setStatus(1);
        member.setRegisterTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Member savedMember = memberRepository.save(member);
        return convertToVO(savedMember);
    }

    @Override
    public MemberVO updateMember(MemberVO memberVO) {
        Member member = memberRepository.findById(memberVO.getId())
                .orElseThrow(MemberNotFoundException::new);

        member.setName(memberVO.getName());
        member.setEmail(memberVO.getEmail());
        member.setPhone(memberVO.getPhone());
        member.setAddress(memberVO.getAddress());
        member.setBirthday(memberVO.getBirthday());
        member.setGender(memberVO.getGender());
        member.setStatus(memberVO.getStatus());

        Member updatedMember = memberRepository.save(member);
        return convertToVO(updatedMember);
    }

    @Override
    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new MemberNotFoundException();
        }
        memberRepository.deleteById(id);
    }

    @Override
    public MemberVO upgradeMemberLevel(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 获取下一等级
        MemberLevel nextLevel = memberLevelRepository.findByGrowthPointGreaterThan(member.getGrowthValue())
                .stream()
                .findFirst()
                .orElse(null);

        if (nextLevel != null && member.getGrowthValue() >= nextLevel.getGrowthPoint()) {
            member.setLevel(nextLevel.getId());
            memberRepository.save(member);
        }

        return convertToVO(member);
    }

    @Override
    public MemberLevelVO getMemberLevelInfo(Integer level) {
        MemberLevel memberLevel = memberLevelRepository.findById(level)
                .orElseThrow(() -> new BusinessException("会员等级不存在"));

        MemberLevelVO vo = new MemberLevelVO();
        vo.setId(memberLevel.getId());
        vo.setLevelName(memberLevel.getLevelName());
        vo.setGrowthPoint(memberLevel.getGrowthPoint());
        vo.setDiscount(memberLevel.getDiscount());
        vo.setDescription(memberLevel.getDescription());

        return vo;
    }

    @Override
    public void recordLogin(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 更新最后登录时间
        member.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        memberRepository.save(member);


    }

    private MemberVO convertToVO(Member member) {
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setAccountId(member.getAccountId());
        vo.setMemberNo(member.getMemberNo());
        vo.setName(member.getName());
        vo.setEmail(member.getEmail());
        vo.setPhone(member.getPhone());
        vo.setAddress(member.getAddress());
        vo.setPoints(member.getPoints());
        vo.setLevel(member.getLevel());
        vo.setGrowthValue(member.getGrowthValue());
        vo.setBirthday(member.getBirthday());
        vo.setGender(member.getGender());
        vo.setStatus(member.getStatus());
        vo.setRegisterTime(member.getRegisterTime());
        vo.setLastLoginTime(member.getLastLoginTime());

        // 设置会员等级信息
        vo.setLevelInfo(getMemberLevelInfo(member.getLevel()));

        return vo;
    }

    private String generateMemberNo() {
        return "M" + UUID.randomUUID().toString().replace("-", "").substring(0, 11).toUpperCase();
    }
}