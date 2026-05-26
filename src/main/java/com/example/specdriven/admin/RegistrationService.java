package com.example.specdriven.admin;

import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.ParticipantRepository;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;

    public RegistrationService(TournamentRepository tournamentRepository,
                               ParticipantRepository participantRepository) {
        this.tournamentRepository = tournamentRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Register the given username for the given tournament. Returns the new
     * {@link Participant} with status {@link RegistrationStatus#PENDING}.
     */
    @Transactional
    public Participant register(Long tournamentId, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown tournament"));
        if (tournament.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATIONS) {
            throw new IllegalStateException(
                    "Tournament is not open for registrations");
        }
        boolean alreadyRegistered = tournament.getParticipants().stream()
                .anyMatch(p -> username.equals(p.getUsername()));
        if (alreadyRegistered) {
            throw new IllegalStateException("You are already registered for this tournament");
        }
        long activeCount = tournament.getParticipants().stream()
                .filter(p -> p.getRegistrationStatus() != RegistrationStatus.DECLINED)
                .count();
        if (activeCount >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Tournament is full");
        }

        Participant p = new Participant();
        p.setName(username);
        p.setUsername(username);
        p.setRegistrationStatus(RegistrationStatus.PENDING);
        p.setTournament(tournament);
        Participant saved = participantRepository.save(p);
        tournament.getParticipants().add(saved);
        return saved;
    }

    @Transactional
    public Participant accept(Long participantId) {
        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown participant"));
        Tournament t = p.getTournament();
        if (t.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATIONS) {
            throw new IllegalStateException(
                    "Registrations can only be accepted while the tournament is open for registrations");
        }
        if (p.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only pending registrations can be accepted");
        }
        long acceptedCount = t.getParticipants().stream()
                .filter(other -> other.getRegistrationStatus() == RegistrationStatus.ACCEPTED)
                .count();
        if (acceptedCount >= t.getMaxParticipants()) {
            throw new IllegalStateException("Tournament already has the maximum number of accepted participants");
        }
        p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
        return participantRepository.save(p);
    }

    @Transactional
    public Participant decline(Long participantId) {
        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown participant"));
        Tournament t = p.getTournament();
        if (t.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATIONS) {
            throw new IllegalStateException(
                    "Registrations can only be declined while the tournament is open for registrations");
        }
        if (p.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only pending registrations can be declined");
        }
        p.setRegistrationStatus(RegistrationStatus.DECLINED);
        return participantRepository.save(p);
    }
}
