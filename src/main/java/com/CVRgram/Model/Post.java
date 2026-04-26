package com.CVRgram.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection
    private java.util.Set<Long> likedUserIds = new java.util.HashSet<>();

    @ElementCollection
    private java.util.Set<Long> savedUserIds = new java.util.HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.util.Set<Long> getLikedUserIds() { return likedUserIds; }
    public void setLikedUserIds(java.util.Set<Long> likedUserIds) { this.likedUserIds = likedUserIds; }
    public java.util.Set<Long> getSavedUserIds() { return savedUserIds; }
    public void setSavedUserIds(java.util.Set<Long> savedUserIds) { this.savedUserIds = savedUserIds; }
    public java.util.List<Comment> getComments() { return comments; }
    public void setComments(java.util.List<Comment> comments) { this.comments = comments; }
}
