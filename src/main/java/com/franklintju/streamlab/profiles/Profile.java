package com.franklintju.streamlab.profiles;

import com.franklintju.streamlab.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "username")
    private String username;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "level")
    private Integer level;

    @Column(name = "coins")
    private Integer coins;

    @Column(name = "followers_count")
    private Integer followersCount;

    @Column(name = "following_count")
    private Integer followingCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private enum Gender {
        MALE, FEMALE, OTHER
    }
}