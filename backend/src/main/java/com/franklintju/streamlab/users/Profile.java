package com.franklintju.streamlab.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, orphanRemoval = true)
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
    private Integer level = 0;

    @Column(name = "coins")
    private Integer coins = 0;

    @Column(name = "followers_count")
    private Integer followersCount = 0;

    @Column(name = "following_count")
    private Integer followingCount = 0;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}