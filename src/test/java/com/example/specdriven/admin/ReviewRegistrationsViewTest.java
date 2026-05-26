package com.example.specdriven.admin;

import java.util.Map;

import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
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
class ReviewRegistrationsViewTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private RegistrationService registrationService;

    private Tournament prepareOpenTournament(int max) {
        Tournament t = new Tournament();
        t.setTitle("Review test " + System.nanoTime());
        t.setMaxParticipants(max);
        t.setStatus(TournamentStatus.OPEN_FOR_REGISTRATIONS);
        return tournamentRepository.save(t);
    }

    @Test
    void viewRendersTournamentTitle() {
        Tournament t = prepareOpenTournament(8);
        navigate(ReviewRegistrationsView.class,
                Map.of("tournamentId", t.getId().toString()));

        H1 title = $(H1.class).first();
        assertTrue(title.getText().contains(t.getTitle()),
                "Header should contain the tournament title");
    }

    @Test
    void gridListsAllRegistrations() {
        Tournament t = prepareOpenTournament(8);
        registrationService.register(t.getId(), "alice");
        registrationService.register(t.getId(), "bob");

        navigate(ReviewRegistrationsView.class,
                Map.of("tournamentId", t.getId().toString()));

        @SuppressWarnings("unchecked")
        Grid<Participant> grid = $(Grid.class).first();
        assertEquals(2, grid.getListDataView().getItemCount());
    }

    @Test
    void gridReflectsAcceptedStatusAfterServiceCall() {
        Tournament t = prepareOpenTournament(8);
        Participant p = registrationService.register(t.getId(), "alice");
        registrationService.accept(p.getId());

        navigate(ReviewRegistrationsView.class,
                Map.of("tournamentId", t.getId().toString()));

        @SuppressWarnings("unchecked")
        Grid<Participant> grid = $(Grid.class).first();
        Participant inGrid = grid.getListDataView().getItems()
                .filter(part -> part.getId().equals(p.getId()))
                .findFirst().orElseThrow();
        assertEquals(RegistrationStatus.ACCEPTED, inGrid.getRegistrationStatus());
    }

    @Test
    @WithAnonymousUser
    void anonymousUsersCannotAccessReviewView() {
        Tournament t = prepareOpenTournament(8);
        assertThrows(Exception.class, () -> navigate(ReviewRegistrationsView.class,
                Map.of("tournamentId", t.getId().toString())));
    }
}
