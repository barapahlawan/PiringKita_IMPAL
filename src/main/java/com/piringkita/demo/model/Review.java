package com.piringkita.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;


@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String comment;

    private int rating;

    @ManyToOne
    @JoinColumn(name = "warung_id")
    private Warung warung;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt = new Date();


}
