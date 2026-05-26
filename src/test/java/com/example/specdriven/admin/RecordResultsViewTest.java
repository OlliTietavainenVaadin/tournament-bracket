package com.example.specdriven.admin;

import java.util.Map;
import java.util.Random;

import com.example.specdriven.bracket.Fixture;
import com.example.specdriven.bracket.FixtureRepository;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class RecordResultsViewTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private FixtureRepository fixtureRepository;

    private Tournament ongoingTournament() {
        return tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.ONGOING)
                .findFirst().orElseThrow();
    }

    private Tournament freshOngoing(int count) {
        Tournament t = new Tournament();
        t.setTitle("Results " + System.nanoTime());
        t.setMaxParticipants(8);
        t.setStatus(TournamentStatus.OPEN_FOR_REGISTRATIONS);
        Tournament saved = tournamentRepository.save(t);
        for (int i = 0; i < count; i++) {
            Participant p = new Participant();
            p.setName("p" + i);
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            p.setTournament(saved);
            saved.getParticipants().add(p);
        }
        tournamentRepository.save(saved);
        return tournamentService.startTournament(saved.getId(), new Random(0));
    }

    @Test
    void rendersFixtureCardsForOngoingTournament() {
        Tournament t = ongoingTournament();
        navigate(RecordResultsView.class,
                Map.of("tournamentId", t.getId().toString()));
        var cards = $(Div.class).withClassName("results-fixture").all();
        assertFalse(cards.isEmpty(), "Should render at least one fixture card");
    }

    @Test
    void clickingWinnerButtonAdvancesParticipant() {
        Tournament t = freshOngoing(4);
        navigate(RecordResultsView.class,
                Map.of("tournamentId", t.getId().toString()));

        var winButtons = $(Button.class).withClassName("results-win-button").all();
        assertFalse(winButtons.isEmpty(),
                "Initial round should expose 'wins' buttons");
        test(winButtons.get(0)).click();

        // Refresh from DB and check at least one round-1 fixture has a winner.
        long decided = fixtureRepository.findByTournamentOrderByRoundAscPositionAsc(
                        tournamentRepository.findById(t.getId()).orElseThrow())
                .stream()
                .filter(f -> f.getRound() == 1 && f.getWinner() != null)
                .count();
        assertTrue(decided >= 1, "At least one round-1 fixture should now have a winner");
    }

    @Test
    void finalWinSetsTournamentStatusToFinished() {
        Tournament t = freshOngoing(3);
        // Play out every remaining fixture programmatically through the view.
        boolean progress = true;
        while (progress) {
            navigate(RecordResultsView.class,
                    Map.of("tournamentId", t.getId().toString()));
            var winButtons = $(Button.class).withClassName("results-win-button").all();
            if (winButtons.isEmpty()) {
                progress = false;
            } else {
                test(winButtons.get(0)).click();
            }
        }

        Tournament finished = tournamentRepository.findById(t.getId()).orElseThrow();
        assertEquals(TournamentStatus.FINISHED, finished.getStatus());
        assertNotNull(finished.getWinner());
    }

    @Test
    @WithAnonymousUser
    void anonymousUsersCannotAccessResultsView() {
        Tournament t = ongoingTournament();
        assertThrows(Exception.class, () -> navigate(RecordResultsView.class,
                Map.of("tournamentId", t.getId().toString())));
    }
}
