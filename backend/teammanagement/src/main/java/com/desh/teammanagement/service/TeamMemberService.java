package com.teammanagement.service;

import com.teammanagement.dto.request.TeamMemberRequestDTO;
import com.teammanagement.dto.response.TeamMemberResponseDTO;
import com.teammanagement.dto.response.TeamSummaryDTO;
import com.teammanagement.entity.Team;
import com.teammanagement.entity.TeamMember;
import com.teammanagement.exception.DuplicateResourceException;
import com.teammanagement.exception.InvalidOperationException;
import com.teammanagement.exception.ResourceNotFoundException;
import com.teammanagement.repository.TeamMemberRepository;
import com.teammanagement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository memberRepository;
    private final TeamRepository teamRepository;  // Needed to validate team exists

    // ============================================
    // CREATE
    // ============================================

    /**
     * Create new team member
     *
     * Business rules:
     * 1. Email must be unique
     * 2. If teamId provided, team must exist
     * 3. All validations from DTO (@NotBlank, @Email) already checked by Spring
     */
    public TeamMemberResponseDTO createMember(TeamMemberRequestDTO requestDTO) {
        // Rule 1: Check email uniqueness
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException(
                    "Member with email '" + requestDTO.getEmail() + "' already exists"
            );
        }

        // Create new member
        TeamMember member = new TeamMember();
        member.setName(requestDTO.getName());
        member.setEmail(requestDTO.getEmail());
        member.setRole(requestDTO.getRole());

        // Rule 2: If team specified, validate and assign
        if (requestDTO.getTeamId() != null) {
            Team team = teamRepository.findById(requestDTO.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found with id: " + requestDTO.getTeamId()
                    ));
            member.setTeam(team);
        }

        // Save and return
        TeamMember savedMember = memberRepository.save(member);
        return convertToResponseDTO(savedMember);
    }

    // ============================================
    // READ
    // ============================================

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamMemberResponseDTO getMemberById(Long id) {
        TeamMember member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team member not found with id: " + id
                ));
        return convertToResponseDTO(member);
    }

    /**
     * Get all members of a specific team
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getMembersByTeamId(Long teamId) {
        // First, verify team exists
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        List<TeamMember> members = memberRepository.findByTeamId(teamId);
        return members.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get members by role
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getMembersByRole(String role) {
        return memberRepository.findByRole(role).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search members by name
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> searchMembersByName(String keyword) {
        return memberRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // UPDATE
    // ============================================

    /**
     * Update team member
     */
    public TeamMemberResponseDTO updateMember(Long id, TeamMemberRequestDTO requestDTO) {
        // Find existing member
        TeamMember member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team member not found with id: " + id
                ));

        // Check email uniqueness (only if email is being changed)
        if (!member.getEmail().equals(requestDTO.getEmail())) {
            if (memberRepository.existsByEmail(requestDTO.getEmail())) {
                throw new DuplicateResourceException(
                        "Member with email '" + requestDTO.getEmail() + "' already exists"
                );
            }
        }

        // Update fields
        member.setName(requestDTO.getName());
        member.setEmail(requestDTO.getEmail());
        member.setRole(requestDTO.getRole());

        // Update team if provided
        if (requestDTO.getTeamId() != null) {
            Team team = teamRepository.findById(requestDTO.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found with id: " + requestDTO.getTeamId()
                    ));
            member.setTeam(team);
        } else {
            // If teamId is null, remove from current team
            member.setTeam(null);
        }

        TeamMember updatedMember = memberRepository.save(member);
        return convertToResponseDTO(updatedMember);
    }

    /**
     * Assign member to team
     * Separate method for just changing team assignment
     */
    public TeamMemberResponseDTO assignToTeam(Long memberId, Long teamId) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team member not found with id: " + memberId
                ));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId
                ));

        member.setTeam(team);
        TeamMember updatedMember = memberRepository.save(member);
        return convertToResponseDTO(updatedMember);
    }

    /**
     * Remove member from team (but don't delete member)
     */
    public TeamMemberResponseDTO removeFromTeam(Long memberId) {
        TeamMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team member not found with id: " + memberId
                ));

        if (member.getTeam() == null) {
            throw new InvalidOperationException(
                    "Member is not assigned to any team"
            );
        }

        member.setTeam(null);
        TeamMember updatedMember = memberRepository.save(member);
        return convertToResponseDTO(updatedMember);
    }

    // ============================================
    // DELETE
    // ============================================

    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Team member not found with id: " + id
            );
        }
        memberRepository.deleteById(id);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private TeamMemberResponseDTO convertToResponseDTO(TeamMember member) {
        TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder = TeamMemberResponseDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt());

        // Include team summary if member has a team
        if (member.getTeam() != null) {
            Team team = member.getTeam();
            TeamSummaryDTO teamSummary = TeamSummaryDTO.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .memberCount(team.getTeamMembers() != null ? team.getTeamMembers().size() : 0)
                    .build();
            builder.team(teamSummary);
        }

        return builder.build();
    }
}