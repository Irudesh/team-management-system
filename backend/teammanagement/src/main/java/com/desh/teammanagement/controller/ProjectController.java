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

/**
 * ProjectController - REST API endpoints for Project management
 *
 * Base URL: http://localhost:8080/api/projects
 *
 * IMPORTANT: This satisfies your requirement:
 * "each project should display its assigned teams"
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    // ============================================
    // CREATE
    // ============================================

    /**
     * Create new project with assigned teams
     *
     * POST http://localhost:8080/api/projects
     * Body: {
     *   "name": "Mobile App Redesign",
     *   "description": "Complete mobile app overhaul",
     *   "teamIds": [1, 2, 3]
     * }
     *
     * Response includes all assigned teams!
     */
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO
    ) {
        ProjectResponseDTO createdProject = projectService.createProject(requestDTO);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    // ============================================
    // READ
    // ============================================

    /**
     * Get all projects with their assigned teams
     *
     * GET http://localhost:8080/api/projects
     *
     * Response: [
     *   {
     *     "id": 1,
     *     "name": "Mobile App",
     *     "teams": [
     *       {"id": 1, "name": "Engineering", "memberCount": 5},
     *       {"id": 2, "name": "Design", "memberCount": 3}
     *     ],
     *     "teamCount": 2
     *   }
     * ]
     *
     * THIS IS YOUR KEY REQUIREMENT!
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get project by ID with assigned teams
     *
     * GET http://localhost:8080/api/projects/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Get all projects assigned to a specific team
     *
     * GET http://localhost:8080/api/projects/team/1
     * (Get all projects that team 1 is working on)
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByTeam(@PathVariable Long teamId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByTeamId(teamId);
        return ResponseEntity.ok(projects);
    }

    /**
     * Search projects
     *
     * GET http://localhost:8080/api/projects/search?keyword=mobile
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDTO>> searchProjects(@RequestParam String keyword) {
        List<ProjectResponseDTO> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }

    // ============================================
    // UPDATE
    // ============================================

    /**
     * Update project (including team assignments)
     *
     * PUT http://localhost:8080/api/projects/1
     * Body: {
     *   "name": "Updated Project Name",
     *   "description": "Updated description",
     *   "teamIds": [2, 3, 4]
     * }
     *
     * This will replace all team assignments with the new list
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO requestDTO
    ) {
        ProjectResponseDTO updatedProject = projectService.updateProject(
                @PathVariable Long id,
                @Valid @RequestBody ProjectRequestDTO requestDTO
        ) {
            ProjectResponseDTO updatedProject = projectService.updateProject(id, requestDTO);
            return ResponseEntity.ok(updatedProject);
        }

        /**
         * Assign a team to project
         *
         * POST http://localhost:8080/api/projects/1/teams/2
         * (Assign team 2 to project 1)
         */
        @PostMapping("/{projectId}/teams/{teamId}")
        public ResponseEntity<ProjectResponseDTO> assignTeam(
                @PathVariable Long projectId,
                @PathVariable Long teamId
    ) {
            ProjectResponseDTO updatedProject = projectService.assignTeam(projectId, teamId);
            return ResponseEntity.ok(updatedProject);
        }

        /**
         * Remove a team from project
         *
         * DELETE http://localhost:8080/api/projects/1/teams/2
         * (Remove team 2 from project 1)
         */
        @DeleteMapping("/{projectId}/teams/{teamId}")
        public ResponseEntity<ProjectResponseDTO> removeTeam(
                @PathVariable Long projectId,
                @PathVariable Long teamId
    ) {
            ProjectResponseDTO updatedProject = projectService.removeTeam(projectId, teamId);
            return ResponseEntity.ok(updatedProject);
        }

        // ============================================
        // DELETE
        // ============================================

        /**
         * Delete project
         *
         * DELETE http://localhost:8080/api/projects/1
         *
         * Note: Teams are NOT deleted, only the project-team relationships
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        }
    }

