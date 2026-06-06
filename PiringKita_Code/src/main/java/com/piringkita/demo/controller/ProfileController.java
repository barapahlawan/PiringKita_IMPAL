package com.piringkita.demo.controller;

import com.piringkita.demo.model.User;
import com.piringkita.demo.service.UserService;
import com.piringkita.demo.service.ReviewService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;


    
    @GetMapping("/edit-profile")
    public String editProfilePage(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedUser);
        return "edit-profile";
    }

   
@PostMapping("/edit-profile")
public String updateProfile(
        @RequestParam("username") String username,
        @RequestParam("email") String email,
        @RequestParam("phone") String phone,
        @RequestParam(value = "photo", required = false) MultipartFile file,
        HttpSession session
) throws IOException {

    User user = (User) session.getAttribute("loggedUser");

    if (file != null && !file.isEmpty()) {
        String namaFile = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String uploadDir = "uploads/profile/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // buat folder kalau belum ada
        }

        // ⬅⬅ COPY FILE KE LOKASI YANG BISA DI-ACCESS SPRING
        Files.copy(file.getInputStream(),
                Paths.get(uploadDir + namaFile),
                StandardCopyOption.REPLACE_EXISTING);

        user.setProfilePicture(namaFile);
    }

    user.setUsername(username);
    user.setEmail(email);
    user.setPhone(phone);

    userService.save(user);

    // update session untuk menampilkan foto terbaru
    session.setAttribute("loggedUser", user);

    return "redirect:/profile";
}

    @GetMapping("/profile")
public String userProfile(HttpSession session, Model model) {

    User user = (User) session.getAttribute("loggedUser");

    if (user == null) {
        return "redirect:/login";
    }

    // kirim data user
    model.addAttribute("username", user.getUsername());
    model.addAttribute("email", user.getEmail());
    model.addAttribute("phone", user.getPhone());
    model.addAttribute("profilePicture", user.getProfilePicture());

    // kirim review milik user
    model.addAttribute("reviews", reviewService.getReviewsByUser(user.getId()));

    return "profile"; // nama file profile.html
}



    
}
