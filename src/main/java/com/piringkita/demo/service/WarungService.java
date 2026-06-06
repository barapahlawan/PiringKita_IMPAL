package com.piringkita.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.piringkita.demo.model.Review;
import com.piringkita.demo.model.Warung;
import com.piringkita.demo.repository.ReviewRepository;
import com.piringkita.demo.repository.WarungRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarungService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WarungRepository warungRepository;

    private final String uploadDir = "uploads/";

    // ===========================
    //  CREATE WARUNG
    // ===========================
    public void saveWarung(Warung warung, MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new Exception("Image tidak boleh kosong");
        }

        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        warung.setImageName(fileName);
        warung.setStatus("pending");

        warungRepository.save(warung);
    }

    // ===========================
    //  GET BY ID dengan rating stats
    // ===========================
    public Warung getById(Long id) {
        Warung warung = warungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warung tidak ditemukan"));
        
        calculateRatingStats(warung);
        return warung;
    }

    // ===========================
    //  FILTER BERDASARKAN STATUS
    // ===========================
    public List<Warung> filterByStatus(String status) {
        List<Warung> warungs;
        if (status == null || status.equalsIgnoreCase("all")) {
            warungs = warungRepository.findAll();
        } else {
            warungs = warungRepository.findByStatus(status);
        }
        
        // Hitung rating stats untuk semua
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        
        return warungs;
    }

    // ===========================
    //  UPDATE STATUS (APPROVE/REJECT)
    // ===========================
    public void updateStatus(Long id, String status, String reason) {
        Warung w = warungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warung tidak ditemukan"));
        w.setStatus(status);
        w.setRejectReason(reason);
        warungRepository.save(w);
    }

    // ===========================
    //  UPDATE WARUNG + FOTO
    // ===========================
    public void updateWarung(Warung input, MultipartFile image) throws IOException {
        Warung w = warungRepository.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Warung tidak ditemukan"));

        w.setNama(input.getNama());
        w.setAlamat(input.getAlamat());
        w.setTelepon(input.getTelepon());
        w.setDeskripsi(input.getDeskripsi());
        w.setTag(input.getTag());
        w.setLatitude(input.getLatitude());
        w.setLongitude(input.getLongitude());
        w.setRating(input.getRating());

        // update image jika ada
        if (image != null && !image.isEmpty()) {
            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);

            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            w.setImageName(fileName);
        }

        warungRepository.save(w);
    }

    public List<Warung> getAll() {
        List<Warung> warungs = warungRepository.findAll();
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        return warungs;
    }

    public List<Warung> filterAndSort(String status, String sort) {
        List<Warung> list;

        if (status == null || status.isEmpty()) {
            list = warungRepository.findAll();
        } else {
            list = warungRepository.findByStatus(status);
        }

        // Hitung rating stats
        for (Warung w : list) {
            calculateRatingStats(w);
        }

        // sort by name
        if ("nama".equals(sort)) {
            list.sort(Comparator.comparing(Warung::getNama));
        }

        return list;
    }

    public List<Warung> getAllApproved() {
        List<Warung> warungs = warungRepository.findByStatus("approved");
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        return warungs;
    }
    
    // NEW: Filter with multiple criteria
    public List<Warung> filterWarungs(String tag, Integer minRating, String sortBy) {
        List<Warung> warungs;
        
        if (tag != null && !tag.isEmpty() && minRating != null) {
            warungs = warungRepository.findByAdvancedFilters(tag, minRating);
        } else if (tag != null && !tag.isEmpty()) {
            warungs = warungRepository.findByTagContainingIgnoreCaseAndStatus(tag, "approved");
        } else if (minRating != null) {
            warungs = warungRepository.findByRatingGreaterThanEqual(minRating)
                .stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());
        } else {
            warungs = warungRepository.findByStatus("approved");
        }
        
        // Hitung rating stats untuk semua warung
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        
        // Apply sorting
        if (sortBy != null) {
            switch (sortBy) {
                case "rating_high":
                    warungs.sort((w1, w2) -> {
                        double r1 = w1.getRatingAverage() != null ? w1.getRatingAverage() : w1.getRating();
                        double r2 = w2.getRatingAverage() != null ? w2.getRatingAverage() : w2.getRating();
                        return Double.compare(r2, r1);
                    });
                    break;
                case "popular":
                    warungs.sort((w1, w2) -> {
                        int c1 = w1.getReviewCount() != null ? w1.getReviewCount() : 0;
                        int c2 = w2.getReviewCount() != null ? w2.getReviewCount() : 0;
                        return Integer.compare(c2, c1);
                    });
                    break;
                case "distance":
                    // Need user location - implement later
                    break;
                default:
                    // Sort by ID (newest first)
                    warungs.sort(Comparator.comparing(Warung::getId).reversed());
            }
        }
        
        return warungs;
    }
    
    // NEW: Search functionality
    public List<Warung> searchWarungs(String keyword) {
        List<Warung> warungs = warungRepository.findByNamaContainingIgnoreCaseOrAlamatContainingIgnoreCase(keyword, keyword)
            .stream()
            .filter(w -> "approved".equals(w.getStatus()))
            .collect(Collectors.toList());
            
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        
        return warungs;
    }
    
    // NEW: Get unique tags for filter options
    public List<String> getAllTags() {
    return warungRepository.findByStatus("approved")
        .stream()
        .map(Warung::getTag)
        .filter(tag -> tag != null && !tag.isEmpty())
        .flatMap(tag -> Arrays.stream(tag.split(",")))
        .map(String::trim)
        .distinct()
        .collect(Collectors.toList());
    }

    public List<Warung> getRekomendasi(String keyword) {
        List<Warung> result;
        
        if (keyword == null || keyword.isEmpty()) {
            result = warungRepository.findTop10ByOrderByRatingDesc();
        } else {
            result = warungRepository.findByNamaContainingIgnoreCaseOrTagContainingIgnoreCase(keyword, keyword);
            
            if (result.isEmpty()) {
                result = warungRepository.findTop10ByOrderByRatingDesc();
            }
        }
        
        // Filter hanya yang approved
        result = result.stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());
                
        // Hitung rating stats
        for (Warung w : result) {
            calculateRatingStats(w);
        }
        
        return result;
    }

    // ===============================
    // TOP RATING (REKOMENDASI)
    // ===============================
    public List<Warung> getTopRating(int limit) {
        List<Warung> warungs = warungRepository.findAll().stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .sorted(Comparator.comparing(Warung::getRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
                
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        
        return warungs;
    }

    // ===============================
    // SEARCH UNTUK REKOMENDASI
    // ===============================
    public List<Warung> searchByKeyword(String keyword) {
        List<Warung> warungs = warungRepository
                .findByNamaContainingIgnoreCaseOrTagContainingIgnoreCase(keyword, keyword)
                .stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());
                
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        
        return warungs;
    }

    public List<Warung> getHistory(Long userId) {
        List<Warung> warungs = warungRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        return warungs;
    }

    public List<Warung> getWarungByUser(Long userId) {
        List<Warung> warungs = warungRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        for (Warung w : warungs) {
            calculateRatingStats(w);
        }
        return warungs;
    }

    public void deleteWarung(Long id) {
        reviewRepository.deleteByWarungId(id);
        warungRepository.deleteById(id);
    }

    public void updateWarungWithImage(Warung warung, MultipartFile image) throws IOException {
        String fileName = saveImage(image);
        warung.setImageName(fileName);
        warungRepository.save(warung);
    }
    
    // ===========================
    //  SAVE IMAGE (UTILITY)
    // ===========================
    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IOException("File gambar kosong");
        }

        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public void updateWarung(Warung warung) {
        warungRepository.save(warung);
    }
    
    // ===========================
    //  CALCULATE RATING STATS
    // ===========================
    public void calculateRatingStats(Warung warung) {
        // Ambil semua review untuk warung ini
        List<Review> reviews = reviewRepository.findByWarungId(warung.getId());
        
        int totalReviews = reviews.size();
        double totalRating = 0;
        
        // Jika ada review dari user
        if (totalReviews > 0) {
            for (Review r : reviews) {
                totalRating += r.getRating();
            }
            
            // TAMBAHKAN rating dari form tambah warung sebagai review pertama
            totalRating += warung.getRating(); // Rating dari form (1-5)
            totalReviews += 1; // Tambah 1 untuk rating dari form
            
            // Hitung average
            double average = totalRating / totalReviews;
            warung.setRatingAverage(Math.round(average * 10.0) / 10.0); // Bulatkan 1 desimal
            warung.setReviewCount(totalReviews);
        } else {
            // Jika tidak ada review user, gunakan rating dari form sebagai satu-satunya rating
            warung.setRatingAverage((double) warung.getRating());
            warung.setReviewCount(1); // Rating dari form dianggap 1 review
        }
    }
}