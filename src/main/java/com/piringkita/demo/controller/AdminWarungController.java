package com.piringkita.demo.controller;

import com.piringkita.demo.model.Warung;
import com.piringkita.demo.service.WarungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin")
public class AdminWarungController {

    @Autowired
    private WarungService warungService;

    // ========== LIST + FILTER ==========
    @GetMapping("/verifikasi")
    public String pageVerifikasi(@RequestParam(required = false) String status,
                                 @RequestParam(required = false) String sort,
                                 Model model) {

        model.addAttribute("warungList", warungService.filterAndSort(status, sort));
        return "admin-warung-list";
    }

    // ========== DETAIL ==========
    @GetMapping("/detail/{id}")
    public String detailWarung(@PathVariable Long id, Model model) {

        Warung warung = warungService.getById(id);
        model.addAttribute("warung", warung);

        return "admin-warung-detail"; // buat halaman ini
    }

    // ========== APPROVE ==========
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {

        warungService.updateStatus(id, "approved", null);

        return "redirect:/admin/verifikasi?msg=Warung+diapprove";
    }

    // ========== REJECT ==========
    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id,
                         @RequestParam(defaultValue = "Data tidak valid") String reason) {

        warungService.updateStatus(id, "rejected", reason);

        return "redirect:/admin/verifikasi?msg=Warung+direject";
    }
}
