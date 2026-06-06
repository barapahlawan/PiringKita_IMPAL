package com.piringkita.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.piringkita.demo.model.Warung;

import java.util.List;

public interface WarungRepository extends JpaRepository<Warung, Long> {

    // Untuk admin filter berdasarkan status
    List<Warung> findByStatus(String status);
    // NEW: Filter by tag
    List<Warung> findByTagContainingIgnoreCase(String tag);
    
    // NEW: Filter by rating
    List<Warung> findByRatingGreaterThanEqual(int minRating);
    
    // NEW: Filter by tag and status
    List<Warung> findByTagContainingIgnoreCaseAndStatus(String tag, String status);
    
    // NEW: Filter by multiple criteria
    @Query("SELECT w FROM Warung w WHERE " +
           "(:tag IS NULL OR LOWER(w.tag) LIKE LOWER(CONCAT('%', :tag, '%'))) AND " +
           "(:minRating IS NULL OR w.rating >= :minRating) AND " +
           "w.status = 'approved'")
    List<Warung> findByAdvancedFilters(
        @Param("tag") String tag,
        @Param("minRating") Integer minRating
    );
    
    // NEW: Search by name or address
    List<Warung> findByNamaContainingIgnoreCaseOrAlamatContainingIgnoreCase(String nama, String alamat);

    List<Warung> findByNamaContainingIgnoreCaseOrTagContainingIgnoreCase(String nama, String tag);

    List<Warung> findTop10ByOrderByRatingDesc();

    List<Warung> findByUser_IdOrderByCreatedAtDesc(Long userId);




}
