package com.example.specdriven.bracket;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BracketViewTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    private BracketView navigateToFirstTournament() {
        Tournament tournament = tournamentRepository.findAll().stream().findFirst().orElseThrow();
        return navigate(BracketView.class,
                Map.of("tournamentId", tournament.getId().toString()));
    }

    @Test
    void bracketViewShowsTournamentTitle() {
        navigateToFirstTournament();
        H1 title = $(H1.class).first();
        assertEquals("Chess Tournament 1", title.getText());
    }

    @Test
    void bracketViewShowsAllRounds() {
        navigateToFirstTournament();

        var roundHeaders = $(H3.class).withClassName("round-header").all();
        assertEquals(3, roundHeaders.size(), "Should have 3 rounds (QF, SF, Final)");
        assertEquals("Quarterfinals", roundHeaders.get(0).getText());
        assertEquals("Semifinals", roundHeaders.get(1).getText());
        assertEquals("Final", roundHeaders.get(2).getText());
    }

    @Test
    void bracketViewShowsAllMatchCards() {
        navigateToFirstTournament();

        // 7 total fixtures: 4 QF + 2 SF + 1 Final
        var matchCards = $(Div.class).withClassName("match-card").all();
        assertEquals(7, matchCards.size(), "Should have 7 match cards");
    }

    @Test
    void bracketViewShowsParticipantsInFirstRound() {
        navigateToFirstTournament();

        var playerNames = $(Span.class).withClassName("player-name").all();

        // Count non-TBD players (should be 8 from round 1)
        long actualPlayers = playerNames.stream()
                .filter(span -> !span.getText().equals("TBD"))
                .count();
        assertEquals(8, actualPlayers, "Should show all 8 participants in first round");
    }

    @Test
    void bracketViewShowsTbdPlaceholders() {
        navigateToFirstTournament();

        // Semi-finals and final should have TBD placeholders (6 total: 2*2 + 1*2)
        var tbdSlots = $(Span.class).withClassName("player-tbd").all();
        assertEquals(6, tbdSlots.size(), "Should show 6 TBD placeholders for undecided fixtures");
    }

    @Test
    void bracketViewShowsConnectors() {
        navigateToFirstTournament();

        // Should have connector columns between rounds (2 connector columns)
        var connectorColumns = $(Div.class).withClassName("connector-column").all();
        assertEquals(2, connectorColumns.size(), "Should have 2 connector columns");

        // Each connector group should have a bracket element for visual connection
        var connectorBrackets = $(Div.class).withClassName("connector-bracket").all();
        assertEquals(3, connectorBrackets.size(),
                "Should have 3 connector brackets (2 for QF→SF + 1 for SF→Final)");
    }

    @Test
    void bracketViewShowsSpecificTournamentById() {
        Tournament tournament = tournamentRepository.findAll().stream().findFirst().orElseThrow();
        navigate(BracketView.class,
                Map.of("tournamentId", tournament.getId().toString()));

        H1 title = $(H1.class).first();
        assertEquals(tournament.getTitle(), title.getText());
    }

    @Test
    void invalidTournamentIdForwardsToList() {
        navigate("view/99999", TournamentListView.class);

        // Should forward to TournamentListView which shows "Tournaments" title
        H1 title = $(H1.class).first();
        assertEquals("Tournaments", title.getText());
    }
}
