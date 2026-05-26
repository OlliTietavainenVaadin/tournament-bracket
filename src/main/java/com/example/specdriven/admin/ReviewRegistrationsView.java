package com.example.specdriven.admin;

import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/registrations/:tournamentId")
@PageTitle("Review Registrations")
@RolesAllowed("ADMIN")
public class ReviewRegistrationsView extends VerticalLayout implements BeforeEnterObserver {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;

    private final H1 title = new H1();
    private final H3 counts = new H3();
    private final Grid<Participant> grid = new Grid<>(Participant.class, false);

    private Tournament tournament;

    public ReviewRegistrationsView(TournamentService tournamentService,
                                   RegistrationService registrationService) {
        this.tournamentService = tournamentService;
        this.registrationService = registrationService;
        addClassName("review-registrations-view");
        setPadding(true);

        title.addClassName("review-title");
        counts.addClassName("review-counts");

        configureGrid();

        add(title, counts, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("tournamentId").orElse(null);
        if (idParam == null) {
            event.forwardTo(ManageTournamentsView.class);
            return;
        }
        try {
            tournament = tournamentService.findById(Long.parseLong(idParam));
        } catch (NumberFormatException e) {
            tournament = null;
        }
        if (tournament == null) {
            event.forwardTo(ManageTournamentsView.class);
            return;
        }
        refresh();
    }

    private void configureGrid() {
        grid.addColumn(Participant::getName).setHeader("Name").setSortable(true);
        grid.addColumn(p -> p.getUsername() == null ? "—" : p.getUsername())
                .setHeader("Username").setSortable(true);
        grid.addColumn(p -> p.getRegistrationStatus().displayLabel())
                .setHeader("Status").setSortable(true);
        grid.addComponentColumn(this::actionButtons).setHeader("Actions");
    }

    private HorizontalLayout actionButtons(Participant participant) {
        Button accept = new Button("Accept", e -> {
            try {
                registrationService.accept(participant.getId());
                Notification.show(participant.getName() + " accepted");
                refresh();
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        accept.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.SUCCESS);

        Button decline = new Button("Decline", e -> {
            try {
                registrationService.decline(participant.getId());
                Notification.show(participant.getName() + " declined");
                refresh();
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        decline.addThemeVariants(ButtonVariant.ERROR);

        boolean actionable = tournament != null
                && tournament.getStatus() == TournamentStatus.OPEN_FOR_REGISTRATIONS
                && participant.getRegistrationStatus() == RegistrationStatus.PENDING;
        accept.setEnabled(actionable);
        decline.setEnabled(actionable);

        HorizontalLayout layout = new HorizontalLayout(accept, decline);
        return layout;
    }

    private void refresh() {
        tournament = tournamentService.findById(tournament.getId());
        title.setText("Review Registrations — " + tournament.getTitle());
        counts.setText(String.format("Accepted %d / %d · Pending %d",
                tournament.acceptedParticipantCount(),
                tournament.getMaxParticipants(),
                tournament.pendingParticipantCount()));
        grid.setItems(tournament.getParticipants());
    }
}
