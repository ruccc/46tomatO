package com.example.tomatomall.repository;

import com.example.tomatomall.po.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, String> {
    List<Advertisement> findAll();
    Optional<Advertisement> findById(String id);
}