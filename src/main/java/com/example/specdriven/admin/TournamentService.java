package com.example.specdriven.admin;

import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public Tournament save(Tournament tournament) {
        validate(tournament);
        return tournamentRepository.save(tournament);
    }

    public void delete(Tournament tournament) {
        tournamentRepository.delete(tournament);
    }

    private void validate(Tournament tournament) {
        if (tournament.getTitle() == null || tournament.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tournament name is required");
        }
        if (tournament.getStartDate() != null && tournament.getId() == null
                && tournament.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Starting date cannot be in the past");
        }
    }
}
