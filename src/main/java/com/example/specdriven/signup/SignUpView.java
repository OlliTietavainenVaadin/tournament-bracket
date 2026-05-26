package com.example.specdriven.signup;

import java.util.List;

import com.example.specdriven.admin.RegistrationService;
import com.example.specdriven.admin.TournamentService;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route("signup")
@PageTitle("Sign Up")
@RolesAllowed({"USER", "ADMIN"})
public class SignUpView extends VerticalLayout {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final AuthenticationContext authenticationContext;
    private final Grid<Tournament> grid = new Grid<>(Tournament.class, false);

    public SignUpView(TournamentService tournamentService,
                      RegistrationService registrationService,
                      AuthenticationContext authenticationContext) {
        this.tournamentService = tournamentService;
        this.registrationService = registrationService;
        this.authenticationContext = authenticationContext;

        addClassName("signup-view");
        setPadding(true);

        H1 title = new H1("Sign up for a tournament");
        title.addClassName("signup-title");
        add(title);

        currentUsername().ifPresent(username -> add(new Paragraph(
                "You are signed in as " + username + ". Sign up for any open tournament below.")));

        configureGrid();
        add(grid);
        refresh();
    }

    private void configureGrid() {
        grid.addColumn(Tournament::getTitle).setHeader("Tournament").setSortable(true);
        grid.addColumn(Tournament::getDescription).setHeader("Description");
        grid.addColumn(t -> t.acceptedParticipantCount() + " accepted, "
                        + t.pendingParticipantCount() + " pending / " + t.getMaxParticipants())
                .setHeader("Players");
        grid.addColumn(t -> myStatusLabel(t)).setHeader("My status");
        grid.addComponentColumn(this::actionFor).setHeader("Actions");
    }

    private HorizontalLayout actionFor(Tournament t) {
        HorizontalLayout layout = new HorizontalLayout();
        String username = currentUsername().orElse(null);
        if (username == null) {
            return layout;
        }
        boolean alreadyRegistered = t.getParticipants().stream()
                .anyMatch(p -> username.equals(p.getUsername()));
        boolean full = t.getParticipants().stream()
                .filter(p -> p.getRegistrationStatus() != RegistrationStatus.DECLINED)
                .count() >= t.getMaxParticipants();

        if (alreadyRegistered) {
            Span span = new Span("Registered");
            span.addClassName("signup-status");
            layout.add(span);
        } else if (full) {
            Span span = new Span("Full");
            span.addClassName("signup-full");
            layout.add(span);
        } else {
            Button signUp = new Button("Sign up", e -> register(t));
            signUp.addThemeVariants(ButtonVariant.PRIMARY);
            signUp.addClassName("signup-button");
            layout.add(signUp);
        }
        return layout;
    }

    private String myStatusLabel(Tournament t) {
        String username = currentUsername().orElse(null);
        if (username == null) {
            return "—";
        }
        return t.getParticipants().stream()
                .filter(p -> username.equals(p.getUsername()))
                .map(Participant::getRegistrationStatus)
                .findFirst()
                .map(RegistrationStatus::displayLabel)
                .orElse("Not registered");
    }

    private void register(Tournament t) {
        try {
            String username = currentUsername()
                    .orElseThrow(() -> new IllegalStateException("Not authenticated"));
            registrationService.register(t.getId(), username);
            Notification.show("Registration submitted — awaiting admin approval");
            refresh();
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private java.util.Optional<String> currentUsername() {
        return authenticationContext.getPrincipalName();
    }

    private void refresh() {
        List<Tournament> open = tournamentService.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.OPEN_FOR_REGISTRATIONS)
                .toList();
        grid.setItems(open);
    }
}
