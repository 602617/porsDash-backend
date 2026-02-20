package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class ShoppingListItemEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "list_id", nullable = false)
    private ShoppingList list;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ShoppingListItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private AppUser actor; // who did it (nice later for shared lists)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    private Instant createdAt = Instant.now();

    // store “what changed” as JSON text (simple, flexible)
    @Column(columnDefinition = "TEXT")
    private String detailsJson;

    public enum EventType {
        CREATED,
        UPDATED,
        BOUGHT,
        UNBOUGHT,
        REORDERED,
        DELETED
    }
}
