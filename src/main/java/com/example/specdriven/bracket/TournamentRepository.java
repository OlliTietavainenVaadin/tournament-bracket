package com.example.specdriven.bracket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);
}
