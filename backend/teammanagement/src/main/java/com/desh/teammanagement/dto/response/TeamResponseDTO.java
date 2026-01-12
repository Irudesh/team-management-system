package com.desh.teammanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
    private int projectCount;
    private Set<TeamMemberSummaryDTO> members;
    private Set<ProjectSummaryDTO> projects;
}