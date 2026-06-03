package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

@Entity
public class ApplicationPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationPermissionRole role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public ApplicationPermissionRole getRole() { return role; }
    public void setRole(ApplicationPermissionRole role) { this.role = role; }
}
