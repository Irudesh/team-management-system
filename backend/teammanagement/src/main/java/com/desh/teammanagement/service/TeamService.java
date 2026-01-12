package com.desh.teammanagement.service;

import com.desh.teammanagement.dto.request.TeamRequestDTO;
import com.desh.teammanagement.dto.response.*;
import com.desh.teammanagement.entity.Team;
import com.desh.teammanagement.entity.TeamMember;
import com.desh.teammanagement.exception.DuplicateResourceException;
import com.desh.teammanagement.exception.ResourceNotFoundException;
import com.desh.teammanagement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamResponseDTO createTeam(TeamRequestDTO requestDTO) {
        if (teamRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException(
                    "Team with name '" + requestDTO.getName() + "' already exists"
            );
        }

        Team team = new Team();
        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        Team savedTeam = teamRepository.save(team);
        return convertToResponseDTO(savedTeam);
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeamsWithMembers() {
        List<Team> teams = teamRepository.findAllWithMembers();
        return teams.stream()
                .map(this::convertToDetailedResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));
        return convertToResponseDTO(team);
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamByIdWithMembers(Long id) {
        Team team = teamRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));
        return convertToDetailedResponseDTO(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> searchTeamsByName(String keyword) {
        List<Team> teams = teamRepository.findByNameContainingIgnoreCase(keyword);
        return teams.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public TeamResponseDTO updateTeam(Long id, TeamRequestDTO requestDTO) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + id
                ));

        if (!team.getName().equals(requestDTO.getName())) {
            if (teamRepository.existsByName(requestDTO.getName())) {
                throw new DuplicateResourceException(
                        "Team with name '" + requestDTO.getName() + "' already exists"
                );
            }
        }

        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        Team updatedTeam = teamRepository.save(team);
        return convertToResponseDTO(updatedTeam);
    }

    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Team not found with id: " + id
            );
        }
        teamRepository.deleteById(id);
    }

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

    private TeamResponseDTO convertToDetailedResponseDTO(Team team) {
        Set<TeamMemberSummaryDTO> memberDTOs = team.getTeamMembers().stream()
                .map(this::convertToMemberSummaryDTO)
                .collect(Collectors.toSet());

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
                .members(memberDTOs)
                .projects(projectDTOs)
                .build();
    }

    private TeamMemberSummaryDTO convertToMemberSummaryDTO(TeamMember member) {
        return TeamMemberSummaryDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}