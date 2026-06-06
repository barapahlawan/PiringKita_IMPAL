package com.piringkita.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Warung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nama;
    private String alamat;
    private String telepon;
    private String deskripsi;
    private String tag;

    private Double latitude;
    private Double longitude;

    private int rating;

    private String imageName;

    private String status;
    private String rejectReason;

    @ManyToMany(mappedBy = "favorites")
    private List<User> likedByUsers = new ArrayList<>();


    // 🔥 Tambahkan relasi ke User (PEMILIK WARUNG)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 🔥 Tambahkan createdAt agar bisa sort by createdAt
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Transient fields
    @Transient private Double ratingAverage;
    @Transient private Integer reviewCount;

    // === Getter Setter ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        this.rating = Math.min(5, Math.max(1, rating));
    }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status=status; }

    public String getRejectReason(){ return rejectReason; }
    public void setRejectReason(String rejectReason){ this.rejectReason=rejectReason; }

    // 🔥 Getter setter untuk User
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // 🔥 Getter setter untuk createdAt
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Transient
    public Double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(Double ratingAverage) { this.ratingAverage = ratingAverage; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
}
