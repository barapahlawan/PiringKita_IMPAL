package com.piringkita.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.piringkita.demo.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByWarungId(Long warungId);
    List<Review> findByUserId(Long userId);   
    List<Review> findByUserIdOrderByIdDesc(Long userId);
    Review findById(long id);
    
void deleteByWarungId(Long warungId);


}

