package com.example.specdriven.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import com.example.specdriven.bracket.BracketGenerator;
import com.example.specdriven.bracket.Fixture;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public Tournament findById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    @Transactional
    public Tournament save(Tournament tournament) {
        validate(tournament);
        if (tournament.getStatus() == null) {
            tournament.setStatus(TournamentStatus.CREATED);
        }
        if (tournament.getId() != null) {
            Tournament existing = tournamentRepository.findById(tournament.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown tournament"));
            if (existing.getStatus() == TournamentStatus.ONGOING
                    || existing.getStatus() == TournamentStatus.FINISHED) {
                throw new IllegalStateException(
                        "Ongoing or finished tournaments cannot be edited");
            }
        }
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public void delete(Tournament tournament) {
        Tournament existing = tournamentRepository.findById(tournament.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown tournament"));
        if (existing.getStatus() == TournamentStatus.ONGOING
                || existing.getStatus() == TournamentStatus.FINISHED) {
            throw new IllegalStateException(
                    "Ongoing or finished tournaments cannot be deleted");
        }
        tournamentRepository.delete(existing);
    }

    @Transactional
    public Tournament openForRegistrations(Long tournamentId) {
        Tournament t = require(tournamentId);
        if (t.getStatus() != TournamentStatus.CREATED) {
            throw new IllegalStateException(
                    "Only draft tournaments can be opened for registrations");
        }
        t.setStatus(TournamentStatus.OPEN_FOR_REGISTRATIONS);
        return tournamentRepository.save(t);
    }

    /**
     * Generate the bracket from accepted participants and transition the
     * tournament to ONGOING. Returns the persisted tournament with fixtures.
     */
    @Transactional
    public Tournament startTournament(Long tournamentId, Random random) {
        Tournament t = require(tournamentId);
        if (t.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATIONS) {
            throw new IllegalStateException(
                    "Only tournaments open for registrations can be started");
        }
        List<Participant> accepted = t.getParticipants().stream()
                .filter(p -> p.getRegistrationStatus() == RegistrationStatus.ACCEPTED)
                .toList();
        if (accepted.size() < 3) {
            throw new IllegalStateException(
                    "At least 3 accepted participants are required to start a tournament");
        }
        if (accepted.size() > 32) {
            throw new IllegalStateException(
                    "A tournament cannot have more than 32 participants");
        }
        List<Fixture> fixtures = BracketGenerator.build(t, accepted, random);
        // Don't reassign the collection (orphanRemoval would lose its reference);
        // mutate in place.
        t.getFixtures().clear();
        t.getFixtures().addAll(fixtures);
        t.setStatus(TournamentStatus.ONGOING);
        return tournamentRepository.save(t);
    }

    private Tournament require(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown tournament"));
    }

    private void validate(Tournament tournament) {
        if (tournament.getTitle() == null || tournament.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tournament name is required");
        }
        Integer max = tournament.getMaxParticipants();
        if (max == null || max < 3 || max > 32) {
            throw new IllegalArgumentException(
                    "Max participants must be between 3 and 32");
        }
        if (tournament.getStartDate() != null && tournament.getId() == null
                && tournament.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Starting date cannot be in the past");
        }
    }
}
