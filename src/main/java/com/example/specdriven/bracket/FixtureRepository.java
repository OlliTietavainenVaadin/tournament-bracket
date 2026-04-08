package com.example.specdriven.bracket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findByTournamentOrderByRoundAscPositionAsc(Tournament tournament);
}
