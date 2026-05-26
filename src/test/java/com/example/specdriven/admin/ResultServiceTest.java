package com.example.specdriven.admin;

import java.util.Random;

import com.example.specdriven.bracket.Fixture;
import com.example.specdriven.bracket.FixtureRepository;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResultServiceTest {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private ResultService resultService;

    private Tournament prepareOngoingTournament(int participantCount) {
        Tournament t = new Tournament();
        t.setTitle("Result test " + System.nanoTime());
        t.setMaxParticipants(8);
        t.setStatus(TournamentStatus.OPEN_FOR_REGISTRATIONS);
        Tournament saved = tournamentRepository.save(t);

        for (int i = 0; i < participantCount; i++) {
            Participant p = new Participant();
            p.setName("p" + i);
            p.setUsername("p" + i);
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            p.setTournament(saved);
            saved.getParticipants().add(p);
        }
        tournamentRepository.save(saved);
        return tournamentService.startTournament(saved.getId(), new Random(0L));
    }

    @Test
    void recordingFirstRoundWinnerAdvancesParticipant() {
        Tournament t = prepareOngoingTournament(4);
        Fixture firstRoundFixture = fixtureRepository
                .findByTournamentOrderByRoundAscPositionAsc(t)
                .stream()
                .filter(f -> f.getRound() == 1 && f.getWinner() == null)
                .findFirst().orElseThrow();
        Participant winner = firstRoundFixture.getParticipant1();
        resultService.recordWinner(firstRoundFixture.getId(), winner.getId());

        Fixture next = fixtureRepository.findById(firstRoundFixture.getNextFixture().getId())
                .orElseThrow();
        boolean placed = (next.getParticipant1() != null
                && next.getParticipant1().getId().equals(winner.getId()))
                || (next.getParticipant2() != null
                && next.getParticipant2().getId().equals(winner.getId()));
        assertTrue(placed, "Winner should advance into the next fixture");
    }

    @Test
    void recordingFinalWinnerFinishesTournament() {
        Tournament t = prepareOngoingTournament(4);

        // Play out round 1
        for (Fixture f : fixtureRepository.findByTournamentOrderByRoundAscPositionAsc(t)) {
            if (f.getRound() == 1 && f.getWinner() == null) {
                resultService.recordWinner(f.getId(), f.getParticipant1().getId());
            }
        }
        // Final
        Fixture finalFx = fixtureRepository
                .findByTournamentOrderByRoundAscPositionAsc(t)
                .stream()
                .filter(f -> f.getRound() == 2)
                .findFirst().orElseThrow();
        Participant champion = finalFx.getParticipant1();
        resultService.recordWinner(finalFx.getId(), champion.getId());

        Tournament finished = tournamentRepository.findById(t.getId()).orElseThrow();
        assertEquals(TournamentStatus.FINISHED, finished.getStatus());
        assertNotNull(finished.getWinner());
        assertEquals(champion.getId(), finished.getWinner().getId());
    }

    @Test
    void cannotRecordTwice() {
        Tournament t = prepareOngoingTournament(4);
        Fixture f = fixtureRepository
                .findByTournamentOrderByRoundAscPositionAsc(t)
                .stream()
                .filter(x -> x.getRound() == 1 && x.getWinner() == null)
                .findFirst().orElseThrow();
        resultService.recordWinner(f.getId(), f.getParticipant1().getId());
        assertThrows(IllegalStateException.class,
                () -> resultService.recordWinner(f.getId(), f.getParticipant2().getId()));
    }

    @Test
    void cannotRecordWhenFixtureHasNoParticipants() {
        Tournament t = prepareOngoingTournament(4);
        Fixture finalFx = fixtureRepository
                .findByTournamentOrderByRoundAscPositionAsc(t)
                .stream()
                .filter(x -> x.getRound() == 2)
                .findFirst().orElseThrow();
        if (finalFx.getParticipant1() == null || finalFx.getParticipant2() == null) {
            assertThrows(IllegalStateException.class,
                    () -> resultService.recordWinner(finalFx.getId(), 1L));
        }
    }
}
