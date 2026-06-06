package com.piringkita.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.piringkita.demo.model.User;
import com.piringkita.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
public String register(@ModelAttribute User user,
                       @RequestParam("confirmPassword") String confirmPassword,
                       Model model) {

    // ✅ Validasi password cocok
    if (!user.getPassword().equals(confirmPassword)) {
        model.addAttribute("error", "Password dan konfirmasi tidak cocok!");
        return "register";
    }

    // ✅ Validasi nomor telepon harus angka 10–13 digit
    if (!user.getPhone().matches("\\d{10,13}")) {
        model.addAttribute("error", "Nomor telepon harus 10-13 digit angka!");
        return "register";
    }

    // ✅ Cek email sudah terdaftar
    String result = userService.register(user);

    if (result.equals("EXIST")) {
        // 🔥 PERUBAHAN DI SINI:
        // Kita kirim pesan error spesifik untuk field email, bukan error global
        model.addAttribute("emailExistsError", "Email sudah terdaftar! Silakan login.");
        
        // Agar data yang sudah diketik tidak hilang saat reload
        model.addAttribute("user", user); 
        return "register";
    }

        model.addAttribute("message", "Akun berhasil dibuat! Silakan login.");
        return "login";
}


    @PostMapping("/login")
public String login(@RequestParam String email,
                    @RequestParam String password,
                    Model model,
                    HttpSession session) {

    User user = userService.findByEmail(email);

    if (user == null) {
        model.addAttribute("error", "Email tidak terdaftar!");
        return "login";
    }

    if (!user.getPassword().equals(password)) {
        model.addAttribute("error", "Password salah!");
        model.addAttribute("email", email);
        return "login";
    }

    // --- SIMPAN KE SESSION ---
    session.setAttribute("loggedUser", user);
    session.setAttribute("email", user.getEmail());

    // ==============================
    // 🔥 CHECK ADMIN LOGIN
    // ==============================
    if (email.equals("admin@gmail.com") && password.equals("67890")) {
        session.setAttribute("role", "ADMIN");  // Opsional kalau mau dibutuhkan
        return "redirect:/dashboardAdmin";
    }

    // USER BIASA
    session.setAttribute("role", "USER");
    return "redirect:/dashboard";
}




    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userService.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Email tidak ditemukan!");
            return "forgot-password";
        }

        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Password tidak cocok!");
            return "reset-password";
        }

        userService.updatePassword(email, password);
        model.addAttribute("message", "Password berhasil direset. Silakan login.");
        return "login";
    }
}
