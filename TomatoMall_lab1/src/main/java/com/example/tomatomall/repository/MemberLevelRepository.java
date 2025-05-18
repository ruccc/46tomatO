package com.example.tomatomall.repository;

import com.example.tomatomall.po.MemberLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevel, Integer> {

    @Query("SELECT ml FROM MemberLevel ml WHERE ml.growthPoint > ?1 ORDER BY ml.growthPoint ASC")
    List<MemberLevel> findByGrowthPointGreaterThan(Integer growthValue);

    MemberLevel findByLevelName(String levelName);
}