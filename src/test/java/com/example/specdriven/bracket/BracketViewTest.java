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

    private Tournament finishedTournament() {
        return tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.FINISHED)
                .findFirst().orElseThrow();
    }

    private Tournament openTournament() {
        return tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.OPEN_FOR_REGISTRATIONS)
                .findFirst().orElseThrow();
    }

    private BracketView navigateTo(Tournament t) {
        return navigate(BracketView.class,
                Map.of("tournamentId", t.getId().toString()));
    }

    @Test
    void bracketViewShowsTournamentTitle() {
        Tournament t = finishedTournament();
        navigateTo(t);
        H1 title = $(H1.class).first();
        assertEquals(t.getTitle(), title.getText());
    }

    @Test
    void bracketViewShowsStatusBadge() {
        Tournament t = finishedTournament();
        navigateTo(t);
        var badges = $(Span.class).withClassName("status-badge").all();
        assertFalse(badges.isEmpty(), "Bracket page should render a status badge");
        assertEquals(t.getStatus().displayLabel(), badges.get(0).getText());
    }

    @Test
    void bracketViewShowsAllRounds() {
        navigateTo(finishedTournament());

        var roundHeaders = $(H3.class).withClassName("round-header").all();
        assertEquals(3, roundHeaders.size(), "Should have 3 rounds (QF, SF, Final)");
        assertEquals("Quarterfinals", roundHeaders.get(0).getText());
        assertEquals("Semifinals", roundHeaders.get(1).getText());
        assertEquals("Final", roundHeaders.get(2).getText());
    }

    @Test
    void bracketViewShowsAllMatchCards() {
        navigateTo(finishedTournament());
        var matchCards = $(Div.class).withClassName("match-card").all();
        assertEquals(7, matchCards.size(), "Should have 7 match cards (4 QF + 2 SF + 1 Final)");
    }

    @Test
    void bracketViewShowsParticipantsInFirstRound() {
        navigateTo(finishedTournament());
        var playerNames = $(Span.class).withClassName("player-name").all();
        long actualPlayers = playerNames.stream()
                .filter(span -> !span.getText().equals("TBD"))
                .count();
        // Finished tournament: every slot is filled (8 + 4 + 2 = 14 named slots)
        assertEquals(14, actualPlayers,
                "Should show participants for every fixture slot in a finished tournament");
    }

    @Test
    void bracketViewShowsTbdPlaceholdersForOngoingFutureRounds() {
        Tournament ongoing = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.ONGOING)
                .findFirst().orElseThrow();
        navigateTo(ongoing);
        var tbdSlots = $(Span.class).withClassName("player-tbd").all();
        // Ongoing tournament's final has 2 TBD slots (semis not yet played)
        assertEquals(2, tbdSlots.size(),
                "Should show TBD placeholders for the final's empty slots");
    }

    @Test
    void bracketViewShowsConnectors() {
        navigateTo(finishedTournament());
        var connectorColumns = $(Div.class).withClassName("connector-column").all();
        assertEquals(2, connectorColumns.size(), "Should have 2 connector columns");

        var connectorBrackets = $(Div.class).withClassName("connector-bracket").all();
        assertEquals(3, connectorBrackets.size(),
                "Should have 3 connector brackets (2 for QF→SF + 1 for SF→Final)");
    }

    @Test
    void bracketViewShowsWinnerForFinishedTournament() {
        navigateTo(finishedTournament());
        var winnerName = $(Span.class).withClassName("winner-name").first();
        assertNotNull(winnerName, "Finished tournament should show winner name");
    }

    @Test
    void bracketViewShowsParticipantsListForOpenTournament() {
        Tournament open = openTournament();
        navigateTo(open);

        // No bracket should be rendered
        var matchCards = $(Div.class).withClassName("match-card").all();
        assertTrue(matchCards.isEmpty(), "Open tournament has no bracket yet");

        var items = $(Div.class).withClassName("participant-list-item").all();
        // Three accepted players for the seeded open tournament
        assertEquals(open.acceptedParticipantCount(), items.size(),
                "Should show one card per accepted player");
    }

    @Test
    void invalidTournamentIdForwardsToList() {
        navigate("view/99999", TournamentListView.class);
        H1 title = $(H1.class).first();
        assertEquals("Tournaments", title.getText());
    }
}
