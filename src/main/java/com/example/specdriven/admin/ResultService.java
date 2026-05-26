package com.example.specdriven.admin;

import java.util.Objects;

import com.example.specdriven.bracket.BracketGenerator;
import com.example.specdriven.bracket.Fixture;
import com.example.specdriven.bracket.FixtureRepository;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultService {

    private final FixtureRepository fixtureRepository;
    private final TournamentRepository tournamentRepository;

    public ResultService(FixtureRepository fixtureRepository,
                         TournamentRepository tournamentRepository) {
        this.fixtureRepository = fixtureRepository;
        this.tournamentRepository = tournamentRepository;
    }

    /**
     * Record {@code winner} as the winner of the given fixture, advancing them
     * into the next fixture's appropriate slot. Setting the final's winner
     * transitions the tournament to {@link TournamentStatus#FINISHED}.
     */
    @Transactional
    public Fixture recordWinner(Long fixtureId, Long winnerParticipantId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown fixture"));
        Tournament tournament = fixture.getTournament();
        if (tournament.getStatus() != TournamentStatus.ONGOING) {
            throw new IllegalStateException("Only ongoing tournaments accept results");
        }
        if (fixture.getWinner() != null) {
            throw new IllegalStateException("Fixture already has a winner");
        }
        if (fixture.getParticipant1() == null || fixture.getParticipant2() == null) {
            throw new IllegalStateException(
                    "Cannot record a winner before both participants are known");
        }

        Participant winner = pickWinner(fixture, winnerParticipantId);
        fixture.setWinner(winner);
        fixtureRepository.save(fixture);

        if (fixture.getNextFixture() != null) {
            BracketGenerator.advanceWinner(fixture, winner);
            fixtureRepository.save(fixture.getNextFixture());
        } else {
            // Final fixture decided: finish the tournament.
            tournament.setWinner(winner);
            tournament.setStatus(TournamentStatus.FINISHED);
            tournamentRepository.save(tournament);
        }
        return fixture;
    }

    private Participant pickWinner(Fixture fixture, Long winnerId) {
        if (Objects.equals(fixture.getParticipant1().getId(), winnerId)) {
            return fixture.getParticipant1();
        }
        if (Objects.equals(fixture.getParticipant2().getId(), winnerId)) {
            return fixture.getParticipant2();
        }
        throw new IllegalArgumentException(
                "Winner must be one of the fixture's two participants");
    }
}
