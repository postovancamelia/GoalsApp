package com.goalsapp.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a single goal item belonging to a user.
 *
 * <p>
 * Each goal item is associated with exactly one user and one category,
 * and records the creation timestamp.
 * </p>
 */
@Entity
@Table(name = "goal_items")
public class GoalItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    public GoalItem() {}

    public GoalItem(User user, Category category, String text) {
        this.user = user;
        this.category = category;
        this.text = text;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
    public User getUser() { return user; }

    public void setCategory(Category category) { this.category = category; }
    public void setText(String text) { this.text = text; }
    public void setUser(User user) { this.user = user; }
}
