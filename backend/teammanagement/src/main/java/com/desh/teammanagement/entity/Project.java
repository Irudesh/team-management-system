package com.desh.teammanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Project")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
            name = "Project_Team",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> teams = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addTeam(Team team) {
        this.teams.add(team);
        team.getProjects().add(this);
    }

    public void removeTeam(Team team) {
        this.teams.remove(team);
        team.getProjects().remove(this);
    }
}