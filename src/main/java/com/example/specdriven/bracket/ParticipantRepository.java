package com.example.specdriven.bracket;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByTournamentAndUsername(Tournament tournament, String username);
}
