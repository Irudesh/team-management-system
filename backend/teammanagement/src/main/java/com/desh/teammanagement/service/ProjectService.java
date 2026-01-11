package com.teammanagement.service;

import com.teammanagement.dto.request.ProjectRequestDTO;
import com.teammanagement.dto.response.ProjectResponseDTO;
import com.teammanagement.dto.response.TeamSummaryDTO;
import com.teammanagement.entity.Project;
import com.teammanagement.entity.Team;
import com.teammanagement.exception.DuplicateResourceException;
import com.teammanagement.exception.ResourceNotFoundException;
import com.teammanagement.repository.ProjectRepository;
import com.teammanagement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    // ============================================
    // CREATE
    // ============================================

    /**
     * Create new project with assigned teams
     *
     * This satisfies your requirement:
     * "Add project (each project should display its assigned teams)"
     */
    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO) {
        // Check for duplicate project name
        if (projectRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException(
                    "Project with name '" + requestDTO.getName() + "' already exists"
            );
        }

        // Create project
        Project project = new Project();
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        // Assign teams if provided
        if (requestDTO.getTeamIds() != null && !requestDTO.getTeamIds().isEmpty()) {
            Set<Team> teams = validateAndGetTeams(requestDTO.getTeamIds());
            // Use helper method from Project entity to manage both sides
            teams.forEach(project::addTeam);
        }

        // Save project (teams are saved due to cascade)
        Project savedProject = projectRepository.save(project);

        return convertToResponseDTO(savedProject);
    }

// ============================================
// READ
// ============================================

    /**
     * Get all projects with their assigned teams
     *
     * IMPORTANT: This is what you'll call to display projects with teams!
     */
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {
        // Use custom query that loads teams (avoids N+1 problem)
        List<Project> projects = projectRepository.findAllWithTeams();

        return projects.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + id
                ));

        return convertToResponseDTO(project);
    }

    /**
     * Get all projects assigned to a specific team
     */
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByTeamId(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        List<Project> projects = projectRepository.findByTeamId(teamId);
        return projects.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search projects
     */
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> searchProjects(String keyword) {
        List<Project> projects = projectRepository.searchProjects(keyword);
        return projects.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

// ============================================
// UPDATE
// ============================================

    /**
     * Update project
     */
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO requestDTO) {
        Project project = projectRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + id
                ));

        // Check name uniqueness (if name is changing)
        if (!project.getName().equals(requestDTO.getName())) {
            if (projectRepository.existsByName(requestDTO.getName())) {
                throw new DuplicateResourceException(
                        "Project with name '" + requestDTO.getName() + "' already exists"
                );
            }
        }

        // Update basic fields
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        // Update team assignments
        if (requestDTO.getTeamIds() != null) {
            // Remove all current teams
            new HashSet<>(project.getTeams()).forEach(project::removeTeam);

            // Add new teams
            if (!requestDTO.getTeamIds().isEmpty()) {
                Set<Team> newTeams = validateAndGetTeams(requestDTO.getTeamIds());
                newTeams.forEach(project::addTeam);
            }
        }

        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Assign team to project
     */
    public ProjectResponseDTO assignTeam(Long projectId, Long teamId) {
        Project project = projectRepository.findByIdWithTeams(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId
                ));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId
                ));

        // Use helper method to manage both sides of relationship
        project.addTeam(team);

        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    /**
     * Remove team from project
     */
    public ProjectResponseDTO removeTeam(Long projectId, Long teamId) {
        Project project = projectRepository.findByIdWithTeams(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId
                ));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId
                ));

        project.removeTeam(team);

        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

// ============================================
// DELETE
// ============================================

    /**
     * Delete project
     * Teams are NOT deleted (only the relationship)
     */
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id
            );
        }
        projectRepository.deleteById(id);
    }

// ============================================
// HELPER METHODS
// ============================================

    /**
     * Validate team IDs and fetch teams
     * Throws exception if any team doesn't exist
     */
    private Set<Team> validateAndGetTeams(Set<Long> teamIds) {
        Set<Team> teams = new HashSet<>();

        for (Long teamId : teamIds) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found with id: " + teamId
                    ));
            teams.add(team);
        }

        return teams;
    }

    /**
     * Convert Project entity to ProjectResponseDTO
     * Includes all assigned teams (your requirement!)
     */
    private ProjectResponseDTO convertToResponseDTO(Project project) {
        // Convert teams to summary DTOs
        Set<TeamSummaryDTO> teamDTOs = project.getTeams().stream()
                .map(team -> TeamSummaryDTO.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .memberCount(team.getTeamMembers() != null ?
                                team.getTeamMembers().size() : 0)
                        .build())
                .collect(Collectors.toSet());

        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .teams(teamDTOs)  // Assigned teams!
                .teamCount(project.getTeams().size())
                .build();
    }

