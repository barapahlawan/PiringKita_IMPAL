package com.piringkita.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.piringkita.demo.model.User;
import com.piringkita.demo.model.Warung;
import com.piringkita.demo.repository.UserRepository;
import com.piringkita.demo.repository.WarungRepository;
import jakarta.transaction.Transactional;


import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WarungRepository warungRepository;


    public String register(User user) {
    User existing = userRepository.findByEmail(user.getEmail());
    if (existing != null) {
        return "EXIST"; // Email sudah dipakai
    }

    userRepository.save(user);
    return "OK";
}


    public User login(String email, String password) {
        User existing = userRepository.findByEmail(email);
        if (existing != null && existing.getPassword().equals(password)) {
            return existing;
        }
        return null;
    }

    public User findByEmail(String email) {
    return userRepository.findByEmail(email);
}

public void updatePassword(String email, String newPassword) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
        user.setPassword(newPassword);
        userRepository.save(user);
    }
}

public void save(User user) {
    userRepository.save(user);
}
@Transactional
public void toggleFavorite(Long userId, Long warungId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Warung warung = warungRepository.findById(warungId)
        .orElseThrow(() -> new RuntimeException("Warung not found"));

    if (user.getFavorites().contains(warung)) {
        user.getFavorites().remove(warung);
    } else {
        user.getFavorites().add(warung);
    }

    userRepository.save(user);
}



public List<Warung> getFavorites(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return user.getFavorites();
}



}
