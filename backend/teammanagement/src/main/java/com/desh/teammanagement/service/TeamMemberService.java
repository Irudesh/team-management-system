package com.desh.teammanagement.service;

import com.desh.teammanagement.dto.request.TeamMemberRequestDTO;
import com.desh.teammanagement.dto.response.TeamMemberResponseDTO;
import com.desh.teammanagement.dto.response.TeamSummaryDTO;
import com.desh.teammanagement.entity.Team;
import com.desh.teammanagement.entity.TeamMember;
import com.desh.teammanagement.exception.DuplicateResourceException;
import com.desh.teammanagement.exception.InvalidOperationException;
import com.desh.teammanagement.exception.ResourceNotFoundException;
import com.desh.teammanagement.repository.TeamMemberRepository;
import com.desh.teammanagement.repository.TeamRepository;
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
    private final TeamRepository teamRepository;

    public TeamMemberResponseDTO createMember(TeamMemberRequestDTO requestDTO) {
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException(
                    "Member with email '" + requestDTO.getEmail() + "' already exists"
            );
        }

        TeamMember member = new TeamMember();
        member.setName(requestDTO.getName());
        member.setEmail(requestDTO.getEmail());
        member.setRole(requestDTO.getRole());

        if (requestDTO.getTeamId() != null) {
            Team team = teamRepository.findById(requestDTO.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found with id: " + requestDTO.getTeamId()
                    ));
            member.setTeam(team);
        }

        TeamMember savedMember = memberRepository.save(member);
        return convertToResponseDTO(savedMember);
    }

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

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getMembersByTeamId(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        List<TeamMember> members = memberRepository.findByTeamId(teamId);
        return members.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getMembersByRole(String role) {
        return memberRepository.findByRole(role).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> searchMembersByName(String keyword) {
        return memberRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public TeamMemberResponseDTO updateMember(Long id, TeamMemberRequestDTO requestDTO) {
        TeamMember member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team member not found with id: " + id
                ));

        if (!member.getEmail().equals(requestDTO.getEmail())) {
            if (memberRepository.existsByEmail(requestDTO.getEmail())) {
                throw new DuplicateResourceException(
                        "Member with email '" + requestDTO.getEmail() + "' already exists"
                );
            }
        }

        member.setName(requestDTO.getName());
        member.setEmail(requestDTO.getEmail());
        member.setRole(requestDTO.getRole());

        if (requestDTO.getTeamId() != null) {
            Team team = teamRepository.findById(requestDTO.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found with id: " + requestDTO.getTeamId()
                    ));
            member.setTeam(team);
        } else {
            member.setTeam(null);
        }

        TeamMember updatedMember = memberRepository.save(member);
        return convertToResponseDTO(updatedMember);
    }

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

    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Team member not found with id: " + id
            );
        }
        memberRepository.deleteById(id);
    }

    private TeamMemberResponseDTO convertToResponseDTO(TeamMember member) {
        TeamMemberResponseDTO.TeamMemberResponseDTOBuilder builder = TeamMemberResponseDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt());

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
