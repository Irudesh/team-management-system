package com.desh.teammanagement.controller;

import com.desh.teammanagement.dto.request.ProjectRequestDTO;
import com.desh.teammanagement.dto.response.ProjectResponseDTO;
import com.desh.teammanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO
    ) {
        ProjectResponseDTO createdProject = projectService.createProject(requestDTO);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByTeam(@PathVariable Long teamId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByTeamId(teamId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDTO>> searchProjects(@RequestParam String keyword) {
        List<ProjectResponseDTO> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO requestDTO
    ) {
        ProjectResponseDTO updatedProject = projectService.updateProject(id, requestDTO);
        return ResponseEntity.ok(updatedProject);
    }

    @PostMapping("/{projectId}/teams/{teamId}")
    public ResponseEntity<ProjectResponseDTO> assignTeam(
            @PathVariable Long projectId,
            @PathVariable Long teamId
    ) {
        ProjectResponseDTO updatedProject = projectService.assignTeam(projectId, teamId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}/teams/{teamId}")
    public ResponseEntity<ProjectResponseDTO> removeTeam(
            @PathVariable Long projectId,
            @PathVariable Long teamId
    ) {
        ProjectResponseDTO updatedProject = projectService.removeTeam(projectId, teamId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}