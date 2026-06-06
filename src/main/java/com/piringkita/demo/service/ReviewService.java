package com.piringkita.demo.service;

import com.piringkita.demo.model.Review;
import com.piringkita.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByWarungId(Long warungId) {
        return reviewRepository.findByWarungId(warungId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdOrderByIdDesc(userId);
    }

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public Review getById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

}



