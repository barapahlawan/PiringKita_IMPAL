package com.piringkita.demo.controller;

import com.piringkita.demo.model.User;
import com.piringkita.demo.model.Warung;
import com.piringkita.demo.service.WarungService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    private WarungService warungService;

    @GetMapping("/history")
    public String showHistoryPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Ambil semua warung milik user
        List<Warung> warungs = warungService.getWarungByUser(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("warungs", warungs);

        return "history";
    }

    @GetMapping("/warung/edit/{id}")
    public String editWarung(@PathVariable Long id, Model model) {
    Warung w = warungService.getById(id);
    model.addAttribute("warung", w);
    return "editwarung";  // ← Nama file HTML
}

@PostMapping("/warung/update/{id}")
public String updateWarung(
        @PathVariable Long id,
        @ModelAttribute Warung warung,
        @RequestParam(value = "image", required = false) MultipartFile image,
        HttpSession session,
        Model model
) {
    // Cek user login
    User user = (User) session.getAttribute("loggedUser");
    if (user == null) {
        return "redirect:/login";
    }

    // Ambil warung lama
    Warung existing = warungService.getById(id);
    if (existing == null) {
        model.addAttribute("error", "Warung tidak ditemukan!");
        return "editwarung";
    }

    try {
        // Update field dasar
        existing.setNama(warung.getNama());
        existing.setAlamat(warung.getAlamat());
        existing.setTelepon(warung.getTelepon());
        existing.setDeskripsi(warung.getDeskripsi());
        existing.setTag(warung.getTag());
        existing.setRating(warung.getRating());
        existing.setLatitude(warung.getLatitude());
        existing.setLongitude(warung.getLongitude());

        // Jika gambar baru diunggah → replace
        if (image != null && !image.isEmpty()) {
            warungService.updateWarungWithImage(existing, image);
        } else {
            // Jika tidak upload → save tanpa ubah imageName
            warungService.updateWarung(existing);
        }

        return "redirect:/warung/detail/" + id + "?updated=true";

    } catch (Exception e) {
        model.addAttribute("error", "Gagal mengupdate warung: " + e.getMessage());
        model.addAttribute("warung", existing);
        return "editwarung";
    }
}


}
