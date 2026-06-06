package com.piringkita.demo.controller;

import com.piringkita.demo.model.Review;
import com.piringkita.demo.model.User;
import com.piringkita.demo.model.Warung;
import com.piringkita.demo.service.ReviewService;   
import com.piringkita.demo.service.WarungService;
import com.piringkita.demo.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;   // <-- WAJIB ADA

    @Autowired
    private WarungService warungService;

    @Autowired
    private UserService userService;

    @GetMapping("/add/{warungId}")
    public String showReviewForm(@PathVariable Long warungId, Model model) {
        model.addAttribute("warungId", warungId);
        model.addAttribute("review", new Review());
        return "tambah-review";
    }

    @PostMapping("/save/{warungId}")
    public String saveReview(
            @PathVariable Long warungId,
            @ModelAttribute Review review,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        Warung warung = warungService.getById(warungId);

        review.setUser(user);
        review.setWarung(warung);

        reviewService.saveReview(review);

        return "redirect:/warung/detail/" + warungId;
    }

    

    // =============================
    // 1️⃣ Edit Review - GET (tampilkan popup)
    // =============================
    @PostMapping("/edit/{id}")
public String editReview(
        @PathVariable Long id,
        @RequestParam int rating,
        @RequestParam String title,
        @RequestParam String comment,
        HttpSession session) {

    User user = (User) session.getAttribute("loggedUser");
    if (user == null) return "redirect:/login";

    Review review = reviewService.getById(id);

    if (!review.getUser().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    review.setRating(rating);
    review.setTitle(title);
    review.setComment(comment);

    reviewService.saveReview(review);

    return "redirect:/profile";
}


    // =============================
    // 2️⃣ Edit Review - POST (simpan perubahan)
    // =============================
    @PostMapping("/update")
public String updateReview(@RequestParam Long id,
                           @RequestParam int rating,
                           @RequestParam String title,
                           @RequestParam String comment,
                           HttpSession session) {

    User user = (User) session.getAttribute("loggedUser");

    if (user == null) {
        return "redirect:/login";
    }

    Review review = reviewService.getById(id);

    // Validasi kepemilikan review
    if (!review.getUser().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    review.setRating(rating);
    review.setTitle(title);
    review.setComment(comment);

    reviewService.saveReview(review);

    return "redirect:/profile";
}


    // =============================
    // 3️⃣ Hapus Review
    // =============================
    @PostMapping("/delete/{id}")
public String deleteReview(@PathVariable Long id, HttpSession session) {

    User user = (User) session.getAttribute("loggedUser");
    if (user == null) return "redirect:/login";

    Review review = reviewService.getById(id);

    if (!review.getUser().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    reviewService.deleteById(id);
    return "redirect:/profile";
}

}


