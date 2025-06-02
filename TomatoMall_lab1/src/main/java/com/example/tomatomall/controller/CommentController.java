package com.example.tomatomall.controller;

import com.example.tomatomall.util.Result;
import com.example.tomatomall.service.CommentService;
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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Result<CommentVO> addComment(
            @RequestParam String bookId,
            @RequestParam Integer userId,
            @RequestParam String content,
            @RequestParam Integer rating) {
        CommentVO comment = commentService.addComment(bookId, userId, content, rating);
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
        stats.put("commentCount", commentCount);
        return Result.success(stats);
    }

    @GetMapping("/{bookId}/check")
    public Result<Boolean> hasUserCommented(
            @PathVariable String bookId,
            @RequestParam Integer userId) {
        boolean hasCommented = commentService.hasUserCommented(bookId, userId);
        return Result.success(hasCommented);
    }
}