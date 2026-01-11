package com.teammanagement.service;

import com.teammanagement.dto.request.TeamRequestDTO;
import com.teammanagement.dto.response.*;
import com.teammanagement.entity.Team;
import com.teammanagement.entity.TeamMember;
import com.teammanagement.exception.DuplicateResourceException;
import com.teammanagement.exception.ResourceNotFoundException;
import com.teammanagement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TeamService - Business logic for Team operations
 *
 * @Service: Marks this as a Spring service component
 * @RequiredArgsConstructor: Lombok generates constructor with final fields (dependency injection)
 * @Transactional: All methods run in database transaction (auto rollback on error)
 */
@Service
@RequiredArgsConstructor
@Transactional  // Database transactions for all methods
public class TeamService {

    // Dependency injection via constructor (RequiredArgsConstructor creates this)
    private final TeamRepository teamRepository;

    // ============================================
    // CREATE
    // ============================================

    /**
     * Create new team
     *
     * Flow:
     * 1. Check if team name already exists (prevent duplicates)
     * 2. Convert DTO to Entity
     * 3. Save to database
     * 4. Convert Entity to Response DTO
     * 5. Return Response DTO
     *
     * @param requestDTO Team data from client
     * @return Created team with ID
     * @throws DuplicateResourceException if team name exists
     */
    public TeamResponseDTO createTeam(TeamRequestDTO requestDTO) {
        // Step 1: Validation - check for duplicate name
        if (teamRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException(
                    "Team with name '" + requestDTO.getName() + "' already exists"
            );
        }

        // Step 2: Convert DTO to Entity
        Team team = new Team();
        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        // Step 3: Save to database
        // save() returns the saved entity with generated ID
        Team savedTeam = teamRepository.save(team);

        // Step 4 & 5: Convert to Response DTO and return
        return convertToResponseDTO(savedTeam);
    }

    // ============================================
    // READ
    // ============================================

    /**
     * Get all teams
     *
     * @return List of all teams
     */
    @Transactional(readOnly = true)  // Optimization: read-only transaction
    public List<TeamResponseDTO> getAllTeams() {
        // Get all teams from database
        List<Team> teams = teamRepository.findAll();

        // Convert each Team entity to TeamResponseDTO
        // Using Java Streams:
        //   - stream(): Convert list to stream
        //   - map(): Transform each Team to TeamResponseDTO
        //   - collect(): Collect results back to List
        return teams.stream()
                .map(this::convertToResponseDTO)  // Method reference
                .collect(Collectors.toList());
    }

    /**
     * Get all teams with members loaded
     * Use this when you need member details
     */
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeamsWithMembers() {
        // Use custom query that loads members (avoids N+1 problem)
        List<Team> teams = teamRepository.findAllWithMembers();

        return teams.stream()
                .map(this::convertToDetailedResponseDTO)  // Include members
                .collect(Collectors.toList());
    }

    /**
     * Get team by ID
     *
     * @param id Team ID
     * @return Team details
     * @throws ResourceNotFoundException if team not found
     */
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(Long id) {
        // findById returns Optional<Team>
        // orElseThrow: if not found, throw exception
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));

        return convertToResponseDTO(team);
    }

    /**
     * Get team by ID with all members
     */
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamByIdWithMembers(Long id) {
        Team team = teamRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));

        return convertToDetailedResponseDTO(team);
    }

    /**
     * Search teams by name
     */
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> searchTeamsByName(String keyword) {
        List<Team> teams = teamRepository.findByNameContainingIgnoreCase(keyword);
        return teams.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // UPDATE
    // ============================================

    /**
     * Update team
     *
     * @param id Team ID to update
     * @param requestDTO Updated data
     * @return Updated team
     * @throws ResourceNotFoundException if team not found
     * @throws DuplicateResourceException if new name already exists
     */
    public TeamResponseDTO updateTeam(Long id, TeamRequestDTO requestDTO) {
        // Step 1: Check if team exists
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));

        // Step 2: Check if new name conflicts with another team
        // Only check if name is being changed
        if (!team.getName().equals(requestDTO.getName())) {
            if (teamRepository.existsByName(requestDTO.getName())) {
                throw new DuplicateResourceException(
                        "Team with name '" + requestDTO.getName() + "' already exists"
                );
            }
        }

        // Step 3: Update fields
        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());
        // Note: updatedAt is automatically set by @PreUpdate in entity

        // Step 4: Save changes
        Team updatedTeam = teamRepository.save(team);

        // Step 5: Return updated team
        return convertToResponseDTO(updatedTeam);
    }

    // ============================================
    // DELETE
    // ============================================

    /**
     * Delete team
     *
     * What happens to members?
     * - If cascade=ALL and orphanRemoval=true: Members are deleted
     * - If only cascade=ALL: Members' team_id is set to NULL
     * - Our setup: Members are deleted (check Team entity)
     *
     * @param id Team ID to delete
     * @throws ResourceNotFoundException if team not found
     */
    public void deleteTeam(Long id) {
        // Step 1: Check if team exists
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Team not found with id: " + id
            );
        }

        // Step 2: Delete team (and members if cascade configured)
        teamRepository.deleteById(id);
    }

    // ============================================
    // HELPER METHODS (Private)
    // ============================================

    /**
     * Convert Team entity to TeamResponseDTO (basic info)
     *
     * Private method - only used within this service
     */
    private TeamResponseDTO convertToResponseDTO(Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .memberCount(team.getTeamMembers() != null ? team.getTeamMembers().size() : 0)
                .projectCount(team.getProjects() != null ? team.getProjects().size() : 0)
                .build();
    }

    /**
     * Convert Team entity to TeamResponseDTO with members
     * Used when you need detailed information
     */
    private TeamResponseDTO convertToDetailedResponseDTO(Team team) {
        // Convert team members to summary DTOs
        Set<TeamMemberSummaryDTO> memberDTOs = team.getTeamMembers().stream()
                .map(this::convertToMemberSummaryDTO)
                .collect(Collectors.toSet());

        // Convert projects to summary DTOs
        Set<ProjectSummaryDTO> projectDTOs = team.getProjects().stream()
                .map(project -> ProjectSummaryDTO.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .teamCount(project.getTeams() != null ? project.getTeams().size() : 0)
                        .build())
                .collect(Collectors.toSet());

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .memberCount(team.getTeamMembers().size())
                .projectCount(team.getProjects().size())
                .members(memberDTOs)      // Include members
                .projects(projectDTOs)    // Include projects
                .build();
    }

    /**
     * Convert TeamMember to summary DTO
     */
    private TeamMemberSummaryDTO convertToMemberSummaryDTO(TeamMember member) {
        return TeamMemberSummaryDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}