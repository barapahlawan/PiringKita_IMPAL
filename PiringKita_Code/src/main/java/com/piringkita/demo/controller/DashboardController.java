package com.piringkita.demo.controller;

import com.piringkita.demo.model.User;
import com.piringkita.demo.model.Warung;
import com.piringkita.demo.repository.UserRepository;
import com.piringkita.demo.service.UserService;
import com.piringkita.demo.service.WarungService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.transaction.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WarungService warungService;

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session,
                                Model model,
                                @RequestParam(required = false) String filter,
                                @RequestParam(required = false) String tag,
                                @RequestParam(required = false) Integer min_rating,
                                @RequestParam(required = false) String sort,
                                @RequestParam(required = false) String cuisine,
                                @RequestParam(required = false) String masakan,
                                @RequestParam(required = false) String features,
                                @RequestParam(required = false) String harga,
                                @RequestParam(required = false) String payment,
                                @RequestParam(required = false) String search) {

        // =============================
        // LOGIN CHECK
        // =============================
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/login";
        }

        // =============================
        // LAST SEARCH FEATURE
        // =============================
        if (search != null && !search.isEmpty()) {
            session.setAttribute("lastSearch", search);
        }
        String lastSearch = (String) session.getAttribute("lastSearch");

        // Ambil semua tags
        List<String> allTags = warungService.getAllTags();
        model.addAttribute("allTags", allTags);

        // =============================
        // FILTERING WARUNG
        // =============================
        List<Warung> approvedWarungs;

        if (tag != null || min_rating != null || sort != null) {
            approvedWarungs = warungService.filterWarungs(tag, min_rating, sort);
        } else if (filter != null) {
            switch (filter) {
                case "rating_tertinggi":
                    approvedWarungs = warungService.filterWarungs(null, 4, "rating_high");
                    break;
                case "terpopuler":
                    approvedWarungs = warungService.filterWarungs(null, null, "popular");
                    break;
                case "murah":
                    approvedWarungs = warungService.filterWarungs("Murah", null, null);
                    break;
                default:
                    approvedWarungs = warungService.getAllApproved();
            }
        } else {
            approvedWarungs = warungService.getAllApproved();
        }

        // =============================
        // FILTER STATUS (hanya approved)
        // =============================
        approvedWarungs = approvedWarungs.stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());

        // =============================
        // SEARCH FILTER (jika ada pencarian)
        // =============================
        if (search != null && !search.isEmpty()) {
            String keyword = search.toLowerCase().trim();
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> {
                    // Cari di nama
                    if (w.getNama() != null && w.getNama().toLowerCase().contains(keyword)) {
                        return true;
                    }
                    
                    // Cari di alamat
                    if (w.getAlamat() != null && w.getAlamat().toLowerCase().contains(keyword)) {
                        return true;
                    }
                    
                    // Cari di tag
                    if (w.getTag() != null && w.getTag().toLowerCase().contains(keyword)) {
                        return true;
                    }
                    
                    // Cari di deskripsi
                    if (w.getDeskripsi() != null && w.getDeskripsi().toLowerCase().contains(keyword)) {
                        return true;
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
            
            // Simpan search query untuk ditampilkan
            model.addAttribute("searchQuery", search);
        }

        // =============================
        // CUISINE FILTER (Asal Masakan)
        // =============================
        if (cuisine != null && !cuisine.isEmpty()) {
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> w.getTag() != null && w.getTag().toLowerCase().contains(cuisine.toLowerCase()))
                .collect(Collectors.toList());
        }

        // =============================
        // JENIS MASAKAN FILTER
        // =============================
        if (masakan != null && !masakan.isEmpty()) {
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> {
                    if (w.getTag() == null) return false;
                    String tagLower = w.getTag().toLowerCase();
                    return tagLower.contains(masakan.toLowerCase());
                })
                .collect(Collectors.toList());
        }

        // =============================
        // FEATURES FILTER (Waktu Operasional)
        // =============================
        if (features != null && !features.isEmpty()) {
            String[] featureArray = features.split(",");
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> {
                    if (w.getTag() == null) return false;
                    String tagLower = w.getTag().toLowerCase();
                    
                    // Cek setiap filter (bisa waktu operasional)
                    for (String feature : featureArray) {
                        if (tagLower.contains(feature.toLowerCase().trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }

        // =============================
        // PAYMENT FILTER (Jenis Pembayaran)
        // =============================
        if (payment != null && !payment.isEmpty()) {
            String[] paymentArray = payment.split(",");
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> {
                    if (w.getTag() == null) return false;
                    String tagLower = w.getTag().toLowerCase();
                    
                    // Cek setiap jenis pembayaran
                    for (String p : paymentArray) {
                        if (tagLower.contains(p.toLowerCase().trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        // =============================
        // HARGA FILTER
        // =============================
        // =============================
        // HARGA FILTER (LENGKAP: Rp, k, rb, angka)
        // =============================
        if (harga != null && !harga.isEmpty()) {
            approvedWarungs = approvedWarungs.stream()
                .filter(w -> {
                    if (w.getTag() == null) return false;
                    String tagLower = w.getTag().toLowerCase(); // Ubah tag jadi huruf kecil semua
                    
                    // Filter berdasarkan range harga
                    switch (harga) {
                        case "10000":
                            return tagLower.contains("murah") || tagLower.contains("sepuluh") ||
                                   // Variasi "k" dan "rb"
                                   tagLower.contains("10k") || tagLower.contains("10 k") || 
                                   tagLower.contains("10rb") || tagLower.contains("10 rb") ||
                                   // Variasi Angka
                                   tagLower.contains("10.000") || tagLower.contains("10000") ||
                                   // Variasi Rupiah
                                   tagLower.contains("rp10.000") || tagLower.contains("rp 10.000") || tagLower.contains("rp10k");

                        case "20000":
                            return tagLower.contains("standar") || tagLower.contains("duapuluh") ||
                                   // Variasi "k" dan "rb"
                                   tagLower.contains("20k") || tagLower.contains("20 k") || 
                                   tagLower.contains("20rb") || tagLower.contains("20 rb") ||
                                   // Variasi Angka
                                   tagLower.contains("20.000") || tagLower.contains("20000") ||
                                   // Variasi Rupiah
                                   tagLower.contains("rp20.000") || tagLower.contains("rp 20.000") || tagLower.contains("rp20k");

                        case "30000":
                            return tagLower.contains("menengah") || tagLower.contains("tigapuluh") ||
                                   // Variasi "k" dan "rb"
                                   tagLower.contains("30k") || tagLower.contains("30 k") || 
                                   tagLower.contains("30rb") || tagLower.contains("30 rb") ||
                                   // Variasi Angka
                                   tagLower.contains("30.000") || tagLower.contains("30000") ||
                                   // Variasi Rupiah
                                   tagLower.contains("rp30.000") || tagLower.contains("rp 30.000") || tagLower.contains("rp30k");

                        case "50000++":
                            return tagLower.contains("mahal") || tagLower.contains("premium") || 
                                   tagLower.contains("restoran") || tagLower.contains("limapuluh") ||
                                   // Variasi "k" dan "rb"
                                   tagLower.contains("50k") || tagLower.contains("50 k") || 
                                   tagLower.contains("50rb") || tagLower.contains("50 rb") ||
                                   // Variasi Angka
                                   tagLower.contains("50.000") || tagLower.contains("50000") ||
                                   // Variasi Rupiah
                                   tagLower.contains("rp50.000") || tagLower.contains("rp 50.000") || tagLower.contains("rp50k");

                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }

        // =============================================
        // RATING TERTINGGI (dengan rating average)
        // =============================================
        List<Warung> warungsRatingTertinggi = approvedWarungs.stream()
            .sorted((w1, w2) -> {
                double rating1 = w1.getRatingAverage() != null ? w1.getRatingAverage() : w1.getRating();
                double rating2 = w2.getRatingAverage() != null ? w2.getRatingAverage() : w2.getRating();
                int ratingCompare = Double.compare(rating2, rating1);

                if (ratingCompare != 0) return ratingCompare;

                // Jika rating sama, bandingkan jumlah ulasan
                int reviewCount1 = w1.getReviewCount() != null ? w1.getReviewCount() : 0;
                int reviewCount2 = w2.getReviewCount() != null ? w2.getReviewCount() : 0;
                return Integer.compare(reviewCount2, reviewCount1);
            })
            .limit(10)
            .collect(Collectors.toList());

        // =============================================
        // TERPOPULER (review terbanyak + rating fallback)
        // =============================================
        List<Warung> warungsTerpopuler = approvedWarungs.stream()
            .sorted((w1, w2) -> {
                int count1 = w1.getReviewCount() != null ? w1.getReviewCount() : 0;
                int count2 = w2.getReviewCount() != null ? w2.getReviewCount() : 0;
                int reviewCompare = Integer.compare(count2, count1);

                if (reviewCompare != 0) return reviewCompare;

                double rating1 = w1.getRatingAverage() != null ? w1.getRatingAverage() : w1.getRating();
                double rating2 = w2.getRatingAverage() != null ? w2.getRatingAverage() : w2.getRating();
                return Double.compare(rating2, rating1);
            })
            .limit(10)
            .collect(Collectors.toList());

        // =============================================
        // REKOMENDASI (TETAP SAMA SEPERTI DI HOME)
        // =============================================
        List<Warung> rekomendasi = warungService.getTopRating(10);
        
        // ++ FILTER REKOMENDASI HANYA YANG APPROVED
        rekomendasi = rekomendasi.stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());

        // SEND DATA TO VIEW
        model.addAttribute("user", loggedUser);
        model.addAttribute("allWarungs", approvedWarungs);
        model.addAttribute("warungsRatingTertinggi", warungsRatingTertinggi);
        model.addAttribute("warungsTerpopuler", warungsTerpopuler);
        model.addAttribute("rekomendasi", rekomendasi);
        model.addAttribute("lastSearch", lastSearch);
        model.addAttribute("searchQuery", search); // Untuk display di UI

        return "dashboard";
    }

    @GetMapping("/search")
    public String searchWarungs(@RequestParam String q, 
                               HttpSession session,
                               Model model) {
        
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/login";
        }
        
        // ++ SIMPAN LAST SEARCH (untuk keperluan lain jika perlu)
        session.setAttribute("lastSearch", q);
        
        // ++ AMBIL HASIL PENCARIAN
        List<Warung> searchResults = warungService.searchWarungs(q);
        
        // ++ FILTER HANYA YANG APPROVED
        List<Warung> approvedWarungs = searchResults.stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());
        
        // =============================================
        // ++ RATING TERTINGGI DARI HASIL PENCARIAN
        // =============================================
        List<Warung> warungsRatingTertinggi = approvedWarungs.stream()
            .sorted((w1, w2) -> {
                double rating1 = w1.getRatingAverage() != null ? w1.getRatingAverage() : w1.getRating();
                double rating2 = w2.getRatingAverage() != null ? w2.getRatingAverage() : w2.getRating();
                int ratingCompare = Double.compare(rating2, rating1);

                if (ratingCompare != 0) return ratingCompare;

                int reviewCount1 = w1.getReviewCount() != null ? w1.getReviewCount() : 0;
                int reviewCount2 = w2.getReviewCount() != null ? w2.getReviewCount() : 0;
                return Integer.compare(reviewCount2, reviewCount1);
            })
            .limit(10)
            .collect(Collectors.toList());

        // =============================================
        // ++ TERPOPULER DARI HASIL PENCARIAN
        // =============================================
        List<Warung> warungsTerpopuler = approvedWarungs.stream()
            .sorted((w1, w2) -> {
                int count1 = w1.getReviewCount() != null ? w1.getReviewCount() : 0;
                int count2 = w2.getReviewCount() != null ? w2.getReviewCount() : 0;
                int reviewCompare = Integer.compare(count2, count1);

                if (reviewCompare != 0) return reviewCompare;

                double rating1 = w1.getRatingAverage() != null ? w1.getRatingAverage() : w1.getRating();
                double rating2 = w2.getRatingAverage() != null ? w2.getRatingAverage() : w2.getRating();
                return Double.compare(rating2, rating1);
            })
            .limit(10)
            .collect(Collectors.toList());

        // =============================================
        // ++ REKOMENDASI (TETAP SAMA SEPERTI DI HOME)
        // =============================================
        List<Warung> rekomendasi = warungService.getTopRating(10);
        
        // ++ FILTER REKOMENDASI HANYA YANG APPROVED
        rekomendasi = rekomendasi.stream()
                .filter(w -> "approved".equals(w.getStatus()))
                .collect(Collectors.toList());
        
        // ++ SEND ALL DATA TO VIEW
        model.addAttribute("user", loggedUser);
        model.addAttribute("allWarungs", approvedWarungs);
        model.addAttribute("warungsRatingTertinggi", warungsRatingTertinggi);
        model.addAttribute("warungsTerpopuler", warungsTerpopuler);
        model.addAttribute("rekomendasi", rekomendasi);
        model.addAttribute("searchQuery", q);
        model.addAttribute("isSearch", true);
        
        return "dashboard";
    }

    @GetMapping("/favorites")
    @Transactional
    public String favorites(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedUser");
        if (sessionUser == null) return "redirect:/login";

        User user = userService.findByEmail(sessionUser.getEmail());

        List<Warung> safeFavorites = user.getFavorites()
            .stream()
            .filter(w -> w != null && w.getId() != null)
            .toList();

        model.addAttribute("favoriteWarungs", safeFavorites);
        model.addAttribute("user", user);

        return "favorites";
    }

    @PostMapping("/favorite/{id}")
    public String toggleFavorite(@PathVariable Long id, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        userService.toggleFavorite(sessionUser.getId(), id);
        return "redirect:/warung/detail/" + id;
    }
}