package com.piringkita.demo.controller;

import com.piringkita.demo.service.WarungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @Autowired
    private WarungService warungService;

    // ➤ MAPPING UNTUK DASHBOARD ADMIN
    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin(HttpSession session, Model model) {

        // Pastikan admin sudah login
        Object user = session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        return "dashboardAdmin";  // tampilkan dashboardAdmin.html
    }

    // ➤ MAPPING UNTUK LIST WARUNG
    @GetMapping("/admin-warung-list")
    public String adminWarungList(Model model) {

        model.addAttribute("warungList", warungService.getAll());

        return "admin-warung-list"; // menuju admin-warung-list.html
    }
}
