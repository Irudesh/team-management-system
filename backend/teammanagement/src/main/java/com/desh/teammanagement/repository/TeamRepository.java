package com.desh.teammanagement.repository;

import com.desh.teammanagement.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    List<Team> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.teamMembers")
    List<Team> findAllWithMembers();

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.teamMembers WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);

    long countByDescriptionContaining(String keyword);
}