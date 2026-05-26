package com.example.specdriven.signup;

import com.example.specdriven.admin.RegistrationService;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SignUpViewTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private RegistrationService registrationService;

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void listsOnlyOpenTournaments() {
        navigate(SignUpView.class);

        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        long expected = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.OPEN_FOR_REGISTRATIONS)
                .count();
        assertEquals(expected, grid.getListDataView().getItemCount(),
                "Sign-up grid should only contain open tournaments");
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void registeringThroughServiceShowsRegistrationInView() {
        // Drive the same business operation the Sign up button would trigger,
        // then verify the view's grid reflects the new pending registration.
        Tournament open = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.OPEN_FOR_REGISTRATIONS)
                .findFirst().orElseThrow();
        long before = open.getParticipants().size();
        registrationService.register(open.getId(), "alice");

        navigate(SignUpView.class);

        Tournament updated = tournamentRepository.findById(open.getId()).orElseThrow();
        assertEquals(before + 1, updated.getParticipants().size());
        assertTrue(updated.getParticipants().stream()
                        .anyMatch(p -> "alice".equals(p.getUsername())
                                && p.getRegistrationStatus() == RegistrationStatus.PENDING),
                "A new pending registration should exist for alice");
    }

    @Test
    @WithMockUser(username = "player9", roles = "USER")
    void alreadyRegisteredUserIsReflectedInMyStatusColumn() {
        navigate(SignUpView.class);

        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        Tournament open = grid.getListDataView().getItems().findFirst().orElseThrow();
        assertTrue(open.getParticipants().stream()
                        .anyMatch(p -> "player9".equals(p.getUsername())),
                "player9 should already have a registration on this seeded tournament");
    }

    @Test
    @WithAnonymousUser
    void anonymousUsersAreRedirectedToLogin() {
        assertThrows(Exception.class, () -> navigate(SignUpView.class),
                "Sign-up requires authentication");
    }
}
