package com.ithistory.repository;

import com.ithistory.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {
    
    Optional<Story> findByDateMonthAndDateDay(Integer month, Integer day);
    
    List<Story> findByDateMonth(Integer month);
    
    boolean existsByDateMonthAndDateDay(Integer month, Integer day);
}
