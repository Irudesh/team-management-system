package com.desh.teammanagement.controller;

import com.desh.teammanagement.dto.request.TeamRequestDTO;
import com.desh.teammanagement.dto.response.TeamResponseDTO;
import com.desh.teammanagement.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TeamController - REST API endpoints for Team management
 *
 * Base URL: http://localhost:8080/api/teams
 *
 * @RestController: Combines @Controller + @ResponseBody
 *   - Automatically converts return values to JSON
 * @RequestMapping: Sets base path for all endpoints in this controller
 * @RequiredArgsConstructor: Lombok generates constructor for dependency injection
 * @CrossOrigin: Allows requests from frontend (React/Angular) during development
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all origins for development (restrict in production!)
public class TeamController {

    // Dependency injection - Spring automatically provides TeamService instance
    private final TeamService teamService;

    // ============================================
    // CREATE - POST /api/teams
    // ============================================

    /**
     * Create new team
     *
     * Endpoint: POST http://localhost:8080/api/teams
     * Request Body: {
     *   "name": "Engineering Team",
     *   "description": "Software development team"
     * }
     *
     * Response: 201 CREATED
     * {
     *   "id": 1,
     *   "name": "Engineering Team",
     *   "description": "Software development team",
     *   "memberCount": 0,
     *   "projectCount": 0,
     *   "createdAt": "2026-01-11T10:30:00",
     *   "updatedAt": "2026-01-11T10:30:00"
     * }
     *
     * @PostMapping: Handles HTTP POST requests
     * @RequestBody: Converts JSON from request body to Java object
     * @Valid: Triggers validation (@NotBlank, @Size, etc.)
     * ResponseEntity<T>: Wrapper for HTTP response with status code
     */
    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(@Valid @RequestBody TeamRequestDTO requestDTO) {
        TeamResponseDTO createdTeam = teamService.createTeam(requestDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED); // 201
    }

    // ============================================
    // READ - GET Endpoints
    // ============================================

    /**
     * Get all teams
     *
     * Endpoint: GET http://localhost:8080/api/teams
     * Response: 200 OK
     * [
     *   {
     *     "id": 1,
     *     "name": "Engineering Team",
     *     "memberCount": 5,
     *     ...
     *   },
     *   {
     *     "id": 2,
     *     "name": "Design Team",
     *     "memberCount": 3,
     *     ...
     *   }
     * ]
     *
     * @GetMapping: Handles HTTP GET requests
     * @RequestParam: Extracts query parameters from URL
     *   Example: /api/teams?includeMembers=true
     */
    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams(
            @RequestParam(required = false, defaultValue = "false") boolean includeMembers
    ) {
        List<TeamResponseDTO> teams;

        if (includeMembers) {
            // Load teams with members (for detailed view)
            teams = teamService.getAllTeamsWithMembers();
        } else {
            // Load teams without members (for list view - faster)
            teams = teamService.getAllTeams();
        }

        return ResponseEntity.ok(teams); // 200 OK
    }

    /**
     * Get team by ID
     *
     * Endpoint: GET http://localhost:8080/api/teams/1
     * Response: 200 OK
     * {
     *   "id": 1,
     *   "name": "Engineering Team",
     *   ...
     * }
     *
     * @PathVariable: Extracts value from URL path
     *   Example: /api/teams/5 → id = 5
     *
     * If team not found: Service throws ResourceNotFoundException
     * GlobalExceptionHandler catches it and returns 404 NOT FOUND
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamById(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean includeMembers
    ) {
        TeamResponseDTO team;

        if (includeMembers) {
            team = teamService.getTeamByIdWithMembers(id);
        } else {
            team = teamService.getTeamById(id);
        }

        return ResponseEntity.ok(team);
    }

    /**
     * Search teams by name
     *
     * Endpoint: GET http://localhost:8080/api/teams/search?keyword=dev
     * Response: Returns all teams with "dev" in their name
     *
     * Example: "Engineering", "DevOps", "Backend Development"
     */
    @GetMapping("/search")
    public ResponseEntity<List<TeamResponseDTO>> searchTeams(
            @RequestParam String keyword
    ) {
        List<TeamResponseDTO> teams = teamService.searchTeamsByName(keyword);
        return ResponseEntity.ok(teams);
    }

    // ============================================
    // UPDATE - PUT /api/teams/{id}
    // ============================================

    /**
     * Update team
     *
     * Endpoint: PUT http://localhost:8080/api/teams/1
     * Request Body: {
     *   "name": "Updated Team Name",
     *   "description": "Updated description"
     * }
     *
     * Response: 200 OK with updated team
     *
     * @PutMapping: Handles HTTP PUT requests (full update)
     * PUT = Replace entire resource
     * PATCH = Partial update (we're not using this, but it exists)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequestDTO requestDTO
    ) {
        TeamResponseDTO updatedTeam = teamService.updateTeam(id, requestDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    // ============================================
    // DELETE - DELETE /api/teams/{id}
    // ============================================

    /**
     * Delete team
     *
     * Endpoint: DELETE http://localhost:8080/api/teams/1
     * Response: 204 NO CONTENT (successful deletion, no data to return)
     *
     * @DeleteMapping: Handles HTTP DELETE requests
     *
     * Note: This will also delete all team members (cascade delete)
     * Check your Team entity's cascade configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build(); // 204 NO CONTENT
    }

    // ============================================
    // ADDITIONAL USEFUL ENDPOINTS
    // ============================================

    /**
     * Get team statistics
     *
     * Endpoint: GET http://localhost:8080/api/teams/1/stats
     * Response: {
     *   "teamId": 1,
     *   "teamName": "Engineering",
     *   "memberCount": 5,
     *   "projectCount": 3
     * }
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<TeamResponseDTO> getTeamStats(@PathVariable Long id) {
        TeamResponseDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }
}