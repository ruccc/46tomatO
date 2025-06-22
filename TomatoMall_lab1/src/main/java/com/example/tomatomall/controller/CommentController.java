package com.example.tomatomall.controller;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CommentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;  // 正确的Map导入

@RestController
@RequestMapping("/api/books/comments")
public class CommentController {
    private final CommentService commentService;
    private final SecurityUtil securityUtil;    public CommentController(CommentService commentService, SecurityUtil securityUtil) {
        this.commentService = commentService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public Result<CommentVO> addComment(
            @RequestParam String bookId,
            @RequestParam String content,
            @RequestParam Integer rating) {
        // 从当前登录用户获取用户ID，提高安全性
        Account currentAccount = securityUtil.getCurrentAccount();
        if (currentAccount == null) {
            return Result.fail(401, "请先登录");
        }
        
        CommentVO comment = commentService.addComment(bookId, currentAccount.getId(), content, rating);
        return Result.success(comment);
    }

    @GetMapping("/{bookId}")
    public Result<Page<CommentVO>> getCommentsByBookId(
            @PathVariable String bookId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentVO> comments = commentService.getCommentsByBookId(bookId, pageable);
        return Result.success(comments);
    }

    @GetMapping("/{bookId}/stats")
    public Result<Object> getCommentStats(@PathVariable String bookId) {
        Double averageRating = commentService.getAverageRating(bookId);
        Long commentCount = commentService.getCommentCount(bookId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", averageRating != null ? averageRating : 0);
        stats.put("commentCount", commentCount);        return Result.success(stats);
    }

    @GetMapping("/{bookId}/check")
    public Result<Boolean> hasUserCommented(@PathVariable String bookId) {
        // 从当前登录用户获取用户ID
        Account currentAccount = securityUtil.getCurrentAccount();
        if (currentAccount == null) {
            return Result.success(false); // 未登录用户当然没有评论过
        }
        
        boolean hasCommented = commentService.hasUserCommented(bookId, currentAccount.getId());
        return Result.success(hasCommented);
    }
}