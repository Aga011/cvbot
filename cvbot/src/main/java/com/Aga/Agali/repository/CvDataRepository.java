package com.Aga.Agali.repository;

import com.Aga.Agali.entity.CvData;
import com.Aga.Agali.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CvDataRepository extends JpaRepository<CvData, Long> {
    Optional<CvData> findTopByUserOrderByCreatedAtDesc(User user);
}