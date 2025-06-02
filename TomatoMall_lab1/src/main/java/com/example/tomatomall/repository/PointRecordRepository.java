package com.example.tomatomall.repository;

import com.example.tomatomall.po.PointRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, Integer> {

    List<PointRecord> findByMemberId(String memberId);

    List<PointRecord> findByMemberIdAndSource(String memberId, String source);

    @Query("SELECT SUM(pr.changePoints) FROM PointRecord pr WHERE pr.memberId = ?1")
    Integer sumPointsByMemberId(String memberId);
}