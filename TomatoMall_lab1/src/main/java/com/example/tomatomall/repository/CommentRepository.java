package com.example.tomatomall.repository;

import com.example.tomatomall.po.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByBookId(String bookId, Pageable pageable);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") String bookId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.book.id = :bookId")
    Long countByBookId(@Param("bookId") String bookId);

    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.book.id = :bookId AND c.user.id = :userId")
    boolean existsByBookIdAndUserId(@Param("bookId") String bookId, @Param("userId") Integer userId);
}