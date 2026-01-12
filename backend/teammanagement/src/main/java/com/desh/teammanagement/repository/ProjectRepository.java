package com.desh.teammanagement.repository;

import com.desh.teammanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);

    boolean existsByName(String name);

    List<Project> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.teams WHERE p.id = :id")
    Optional<Project> findByIdWithTeams(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.teams")
    List<Project> findAllWithTeams();

    @Query("SELECT p FROM Project p JOIN p.teams t WHERE t.id = :teamId")
    List<Project> findByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT p FROM Project p WHERE p.teams IS EMPTY")
    List<Project> findProjectsWithoutTeams();

    @Query("SELECT COUNT(p) FROM Project p JOIN p.teams t WHERE t.id = :teamId")
    long countProjectsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.teams " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Project> searchProjects(@Param("keyword") String keyword);
}