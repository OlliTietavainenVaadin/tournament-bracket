package com.example.specdriven.bracket;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.RouterLink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    void listsTournamentsWithLinks() {
        navigate(TournamentListView.class);

        var links = $(RouterLink.class).withClassName("tournament-list-link").all();
        assertFalse(links.isEmpty(), "Should show tournament links");

        // The seeded tournament should be listed
        boolean hasChessTournament = links.stream()
                .anyMatch(link -> link.getText().contains("Chess Tournament 1"));
        assertTrue(hasChessTournament, "Should list the seeded Chess Tournament 1");
    }

    @Test
    void tournamentLinksNavigateToBracketView() {
        navigate(TournamentListView.class);

        var link = $(RouterLink.class).withClassName("tournament-list-link").first();
        assertNotNull(link, "Should have at least one tournament link");

        // Click the link to navigate
        test(link).click();

        // Should now be on the bracket view
        H1 title = $(H1.class).first();
        assertEquals("Chess Tournament 1", title.getText());
    }

    @Test
    void isAccessibleAtRoot() {
        TournamentListView view = navigate(TournamentListView.class);
        assertNotNull(view);
    }
}
