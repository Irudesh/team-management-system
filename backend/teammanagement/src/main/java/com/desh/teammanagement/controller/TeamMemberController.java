package com.desh.teammanagement.controller;

import com.desh.teammanagement.dto.request.TeamMemberRequestDTO;
import com.desh.teammanagement.dto.response.TeamMemberResponseDTO;
import com.desh.teammanagement.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TeamMemberController - REST API endpoints for TeamMember management
 *
 * Base URL: http://localhost:8080/api/members
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamMemberController {

    private final TeamMemberService memberService;

    // ============================================
    // CREATE
    // ============================================

    /**
     * Create new team member
     *
     * POST http://localhost:8080/api/members
     * Body: {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "role": "Developer",
     *   "teamId": 1
     * }
     */
    @PostMapping
    public ResponseEntity<TeamMemberResponseDTO> createMember(
            @Valid @RequestBody TeamMemberRequestDTO requestDTO
    ) {
        TeamMemberResponseDTO createdMember = memberService.createMember(requestDTO);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    // ============================================
    // READ
    // ============================================

    /**
     * Get all members
     *
     * GET http://localhost:8080/api/members
     */
    @GetMapping
    public ResponseEntity<List<TeamMemberResponseDTO>> getAllMembers() {
        List<TeamMemberResponseDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * Get member by ID
     *
     * GET http://localhost:8080/api/members/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamMemberResponseDTO> getMemberById(@PathVariable Long id) {
        TeamMemberResponseDTO member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    /**
     * Get all members of a specific team
     *
     * GET http://localhost:8080/api/members/team/1
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamMemberResponseDTO>> getMembersByTeam(@PathVariable Long teamId) {
        List<TeamMemberResponseDTO> members = memberService.getMembersByTeamId(teamId);
        return ResponseEntity.ok(members);
    }

    /**
     * Get members by role
     *
     * GET http://localhost:8080/api/members/role?role=Developer
     */
    @GetMapping("/role")
    public ResponseEntity<List<TeamMemberResponseDTO>> getMembersByRole(@RequestParam String role) {
        List<TeamMemberResponseDTO> members = memberService.getMembersByRole(role);
        return ResponseEntity.ok(members);
    }

    /**
     * Search members by name
     *
     * GET http://localhost:8080/api/members/search?keyword=john
     */
    @GetMapping("/search")
    public ResponseEntity<List<TeamMemberResponseDTO>> searchMembers(@RequestParam String keyword) {
        List<TeamMemberResponseDTO> members = memberService.searchMembersByName(keyword);
        return ResponseEntity.ok(members);
    }

    // ============================================
    // UPDATE
    // ============================================

    /**
     * Update member
     *
     * PUT http://localhost:8080/api/members/1
     * Body: {
     *   "name": "John Updated",
     *   "email": "john.updated@example.com",
     *   "role": "Senior Developer",
     *   "teamId": 2
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamMemberResponseDTO> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberRequestDTO requestDTO
    ) {
        TeamMemberResponseDTO updatedMember = memberService.updateMember(id, requestDTO);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * Assign member to team
     *
     * PUT http://localhost:8080/api/members/1/assign/2
     * (Assigns member 1 to team 2)
     */
    @PutMapping("/{memberId}/assign/{teamId}")
    public ResponseEntity<TeamMemberResponseDTO> assignToTeam(
            @PathVariable Long memberId,
            @PathVariable Long teamId
    ) {
        TeamMemberResponseDTO updatedMember = memberService.assignToTeam(memberId, teamId);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * Remove member from team (but don't delete member)
     *
     * PUT http://localhost:8080/api/members/1/remove-from-team
     */
    @PutMapping("/{memberId}/remove-from-team")
    public ResponseEntity<TeamMemberResponseDTO> removeFromTeam(@PathVariable Long memberId) {
        TeamMemberResponseDTO updatedMember = memberService.removeFromTeam(memberId);
        return ResponseEntity.ok(updatedMember);
    }

    // ============================================
    // DELETE
    // ============================================

    /**
     * Delete member
     *
     * DELETE http://localhost:8080/api/members/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}