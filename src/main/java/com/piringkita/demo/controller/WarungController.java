package com.piringkita.demo.controller;

import com.piringkita.demo.model.Warung;
import com.piringkita.demo.service.ReviewService;
import com.piringkita.demo.service.WarungService;
import com.piringkita.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import com.piringkita.demo.model.User;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WarungController {

    @Autowired
    private WarungService warungService;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;

    // ============================
    // PAGE FORM TAMBAH WARUNG
    // ============================
    @GetMapping("/tambahwarung")
    public String formWarung(HttpSession session, Model model) {
        Object draft = session.getAttribute("warungDraft");
        model.addAttribute("warungDraft", draft);

        model.addAttribute("latitude", session.getAttribute("latitude"));
        model.addAttribute("longitude", session.getAttribute("longitude"));

        return "tambahwarung";
    }

    // ============================
    // PROSES SIMPAN WARUNG
    // ============================
    @PostMapping("/tambahwarung")
    public String saveWarung(
            @ModelAttribute Warung warung,
            @RequestParam("image") MultipartFile image,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        warung.setUser(user);

        Double lat = (Double) session.getAttribute("latitude");
        Double lng = (Double) session.getAttribute("longitude");

        if (lat != null) warung.setLatitude(lat);
        if (lng != null) warung.setLongitude(lng);

        if (image == null || image.isEmpty()) {
            model.addAttribute("error", "Gambar wajib diupload");
            return "tambahwarung";
        }

        try {
            warungService.saveWarung(warung, image);

            session.removeAttribute("warungDraft");
            session.removeAttribute("latitude");
            session.removeAttribute("longitude");

            return "redirect:/dashboard?success=true";

        } catch (Exception e) {
            model.addAttribute("error", "Gagal upload warung: " + e.getMessage());
            return "tambahwarung";
        }
    }

    // ============================
    // PIN MAP PAGE
    // ============================
    @GetMapping("/pin-map")
    public String pinMapPage() {
        return "pin-map";
    }

    // ============================
    // SIMPAN LOKASI PIN MAP
    // ============================
    @PostMapping("/simpan-pinmap")
    public String simpanLokasi(
            @RequestParam double latitude,
            @RequestParam double longitude,
            HttpSession session
    ) {
        session.setAttribute("latitude", latitude);
        session.setAttribute("longitude", longitude);

        return "redirect:/tambahwarung";
    }

    // ============================
    // SAVE DRAFT KE SESSION (AJAX)
    // ============================
    @PostMapping("/save-draft")
    @ResponseBody
    public void saveDraft(@RequestBody Map<String, Object> data, HttpSession session) {
        Map<String, Object> draft = (Map<String, Object>) session.getAttribute("warungDraft");

        if (draft == null)
            draft = new HashMap<>();

        draft.putAll(data);
        session.setAttribute("warungDraft", draft);
    }

    // ============================
    // DETAIL WARUNG
    // ============================
    @GetMapping("/warung/detail/{id}")
    public String detailWarung(@PathVariable Long id, HttpSession session, Model model) {
        Warung warung = warungService.getById(id);
        model.addAttribute("warung", warung);

        model.addAttribute("ulasan", reviewService.getReviewsByWarungId(id));

        User user = (User) session.getAttribute("loggedUser");
        boolean isFavorite = false;

        if (user != null) {
            // Gunakan UserService untuk mendapatkan user dengan favorites
            User fullUser = userService.findByEmail(user.getEmail());
            isFavorite = fullUser.getFavorites().stream()
                    .anyMatch(fav -> fav.getId().equals(id));
        }

        model.addAttribute("isFavorite", isFavorite);
        return "detail-warung";
    }

    // ============================
    // DELETE WARUNG
    // ============================
    @PostMapping("/warung/delete/{id}")
    public String deleteWarung(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/login";

        warungService.deleteWarung(id);
        return "redirect:/history";
    }
}