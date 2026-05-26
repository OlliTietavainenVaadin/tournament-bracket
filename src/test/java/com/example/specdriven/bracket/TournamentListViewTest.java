package com.example.specdriven.bracket;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouterLink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TournamentListViewTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Test
    void showsTitle() {
        navigate(TournamentListView.class);
        H1 title = $(H1.class).first();
        assertEquals("Tournaments", title.getText());
    }

    @Test
    void listsAllVisibleTournaments() {
        navigate(TournamentListView.class);

        List<RouterLink> links = $(RouterLink.class).withClassName("tournament-list-link").all();
        // The seeder creates 3 tournaments; CREATED ones are hidden but none seeded as CREATED
        long expectedVisible = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() != TournamentStatus.CREATED)
                .count();
        assertEquals(expectedVisible, links.size(),
                "Should list every non-draft tournament");
    }

    @Test
    void rendersStatusBadgePerTournament() {
        navigate(TournamentListView.class);
        var badges = $(Span.class).withClassName("status-badge").all();
        assertFalse(badges.isEmpty(),
                "Each tournament card should render a status badge");
    }

    @Test
    void draftTournamentsAreHiddenFromVisitors() {
        Tournament draft = new Tournament();
        draft.setTitle("Hidden Draft Tournament");
        draft.setMaxParticipants(8);
        draft.setStatus(TournamentStatus.CREATED);
        tournamentRepository.save(draft);

        navigate(TournamentListView.class);

        boolean exposesDraft = $(RouterLink.class).withClassName("tournament-list-link").all().stream()
                .anyMatch(link -> "Hidden Draft Tournament".equals(link.getText()));
        assertFalse(exposesDraft,
                "Draft tournaments should not be visible on the public list");
    }

    @Test
    void tournamentLinksNavigateToBracketView() {
        navigate(TournamentListView.class);
        var link = $(RouterLink.class).withClassName("tournament-list-link").first();
        assertNotNull(link, "Should have at least one tournament link");

        test(link).click();

        // After click we should be on a BracketView showing some tournament's title
        H1 title = $(H1.class).first();
        assertNotNull(title);
        assertNotEquals("Tournaments", title.getText());
    }

    @Test
    void isAccessibleAtRoot() {
        TournamentListView view = navigate(TournamentListView.class);
        assertNotNull(view);
    }
}
