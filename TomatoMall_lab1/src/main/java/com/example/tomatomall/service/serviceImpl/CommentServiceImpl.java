// CommentServiceImpl.java
package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.Comment;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.CommentRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.CommentService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ProductService productService;
    private final AccountService accountService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              ProductService productService,
                              AccountService accountService) {
        this.commentRepository = commentRepository;
        this.productService = productService;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public CommentVO addComment(String bookId, Integer userId, String content, Integer rating) {
        // 检查用户是否已评论过
        if (commentRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new IllegalArgumentException("您已经评论过本书籍");
        }

        // 验证评分范围
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }

        Product book = productService.getProductById(bookId).toPO();
        Account user = accountService.getAccountById(userId).toPO();

        Comment comment = new Comment();
        comment.setBook(book);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setCreateTime(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return convertToVO(savedComment);
    }

    @Override
    public Page<CommentVO> getCommentsByBookId(String bookId, Pageable pageable) {
        return commentRepository.findByBookId(bookId, pageable)
                .map(this::convertToVO);
    }

    @Override
    public Double getAverageRating(String bookId) {
        return commentRepository.findAverageRatingByBookId(bookId);
    }

    @Override
    public Long getCommentCount(String bookId) {
        return commentRepository.countByBookId(bookId);
    }

    @Override
    public boolean hasUserCommented(String bookId, Integer userId) {
        return commentRepository.existsByBookIdAndUserId(bookId, userId);
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        // 设置书籍信息
        vo.setBookId(comment.getBook().getId());
        vo.setBookTitle(comment.getBook().getTitle());
        vo.setBookCover(comment.getBook().getCover());

        // 设置用户信息
        vo.setUserId(comment.getUser().getId());
        vo.setUserName(comment.getUser().getName());
        vo.setUserAvatar(comment.getUser().getAvatar());

        return vo;
    }
}