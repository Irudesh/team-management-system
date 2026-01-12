package com.desh.teammanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Team")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private Set<TeamMember> teamMembers = new HashSet<>();

    @ManyToMany(mappedBy = "teams", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

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

    public void addTeamMember(TeamMember member) {
        teamMembers.add(member);
        member.setTeam(this);
    }

    public void removeTeamMember(TeamMember member) {
        teamMembers.remove(member);
        member.setTeam(null);
    }
}