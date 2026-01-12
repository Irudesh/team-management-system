package com.desh.teammanagement.repository;

import com.desh.teammanagement.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByEmail(String email);

    boolean existsByEmail(String email);

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByRole(String role);

    List<TeamMember> findByNameContainingIgnoreCase(String keyword);

    List<TeamMember> findByRoleAndTeamId(String role, Long teamId);

    long countByTeamId(Long teamId);

    @Query("SELECT m FROM TeamMember m LEFT JOIN FETCH m.team")
    List<TeamMember> findAllWithTeam();

    @Query("SELECT m FROM TeamMember m LEFT JOIN FETCH m.team WHERE m.id = :id")
    Optional<TeamMember> findByIdWithTeam(@Param("id") Long id);

    @Query("SELECT m FROM TeamMember m WHERE m.team IS NULL")
    List<TeamMember> findMembersWithoutTeam();

    @Query("SELECT m FROM TeamMember m WHERE " +
            "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:role IS NULL OR m.role = :role) AND " +
            "(:teamId IS NULL OR m.team.id = :teamId)")
    List<TeamMember> searchMembers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("role") String role,
            @Param("teamId") Long teamId
    );
}
