package com.desh.teammanagement.service;

import com.desh.teammanagement.dto.request.ProjectRequestDTO;
import com.desh.teammanagement.dto.response.ProjectResponseDTO;
import com.desh.teammanagement.dto.response.TeamSummaryDTO;
import com.desh.teammanagement.entity.Project;
import com.desh.teammanagement.entity.Team;
import com.desh.teammanagement.exception.DuplicateResourceException;
import com.desh.teammanagement.exception.ResourceNotFoundException;
import com.desh.teammanagement.repository.ProjectRepository;
import com.desh.teammanagement.repository.TeamRepository;
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

    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO) {
        if (projectRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException(
                    "Project with name '" + requestDTO.getName() + "' already exists"
            );
        }

        Project project = new Project();
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        if (requestDTO.getTeamIds() != null && !requestDTO.getTeamIds().isEmpty()) {
            Set<Team> teams = validateAndGetTeams(requestDTO.getTeamIds());
            teams.forEach(project::addTeam);
        }

        Project savedProject = projectRepository.save(project);
        return convertToResponseDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {
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

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> searchProjects(String keyword) {
        List<Project> projects = projectRepository.searchProjects(keyword);
        return projects.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO requestDTO) {
        Project project = projectRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + id
                ));

        if (!project.getName().equals(requestDTO.getName())) {
            if (projectRepository.existsByName(requestDTO.getName())) {
                throw new DuplicateResourceException(
                        "Project with name '" + requestDTO.getName() + "' already exists"
                );
            }
        }

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        if (requestDTO.getTeamIds() != null) {
            new HashSet<>(project.getTeams()).forEach(project::removeTeam);

            if (!requestDTO.getTeamIds().isEmpty()) {
                Set<Team> newTeams = validateAndGetTeams(requestDTO.getTeamIds());
                newTeams.forEach(project::addTeam);
            }
        }

        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

    public ProjectResponseDTO assignTeam(Long projectId, Long teamId) {
        Project project = projectRepository.findByIdWithTeams(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId
                ));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId
                ));

        project.addTeam(team);
        Project updatedProject = projectRepository.save(project);
        return convertToResponseDTO(updatedProject);
    }

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

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id
            );
        }
        projectRepository.deleteById(id);
    }

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

    private ProjectResponseDTO convertToResponseDTO(Project project) {
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
                .teams(teamDTOs)
                .teamCount(project.getTeams().size())
                .build();
    }
}