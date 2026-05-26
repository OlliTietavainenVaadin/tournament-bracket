package com.example.specdriven.admin;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.specdriven.bracket.BracketService;
import com.example.specdriven.bracket.Fixture;
import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/results/:tournamentId")
@PageTitle("Record Results")
@RolesAllowed("ADMIN")
public class RecordResultsView extends VerticalLayout implements BeforeEnterObserver {

    private final BracketService bracketService;
    private final TournamentService tournamentService;
    private final ResultService resultService;

    private Tournament tournament;

    public RecordResultsView(BracketService bracketService,
                             TournamentService tournamentService,
                             ResultService resultService) {
        this.bracketService = bracketService;
        this.tournamentService = tournamentService;
        this.resultService = resultService;
        addClassName("record-results-view");
        setPadding(true);
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
        render();
    }

    private void render() {
        removeAll();
        H1 title = new H1("Record Results — " + tournament.getTitle());
        title.addClassName("results-title");
        Span status = new Span(tournament.getStatus().displayLabel());
        status.addClassName("status-badge");
        status.addClassName(statusClass(tournament.getStatus()));
        add(title, status);

        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            Span winnerInfo = new Span(
                    tournament.getWinner() != null
                            ? "Tournament complete — winner: " + tournament.getWinner().getName()
                            : "Tournament complete.");
            winnerInfo.addClassName("results-finished");
            add(winnerInfo);
        }

        Map<Integer, List<Fixture>> roundsMap = bracketService.getFixturesByRound(tournament);
        int totalRounds = bracketService.getTotalRounds(tournament);

        Div roundsContainer = new Div();
        roundsContainer.addClassName("results-rounds");

        for (int round = 1; round <= totalRounds; round++) {
            List<Fixture> roundFixtures = roundsMap.getOrDefault(round, List.of())
                    .stream()
                    .sorted(Comparator.comparingInt(Fixture::getPosition))
                    .toList();
            roundsContainer.add(buildRoundColumn(round, totalRounds, roundFixtures));
        }
        add(roundsContainer);
    }

    private Div buildRoundColumn(int round, int totalRounds, List<Fixture> fixtures) {
        Div column = new Div();
        column.addClassName("results-round");
        H3 header = new H3(roundLabel(round, totalRounds));
        header.addClassName("results-round-header");
        column.add(header);

        for (Fixture fixture : fixtures) {
            column.add(buildFixtureCard(fixture));
        }
        return column;
    }

    private Div buildFixtureCard(Fixture fixture) {
        Div card = new Div();
        card.addClassName("results-fixture");

        Participant p1 = fixture.getParticipant1();
        Participant p2 = fixture.getParticipant2();
        Participant winner = fixture.getWinner();
        boolean decided = winner != null;
        boolean canRecord = !decided && p1 != null && p2 != null
                && tournament.getStatus() == TournamentStatus.ONGOING;

        card.add(buildSlot(p1, winner));
        card.add(buildSlot(p2, winner));

        if (decided) {
            Span verdict = new Span("Winner: " + winner.getName());
            verdict.addClassName("results-winner-label");
            card.add(verdict);
        } else if (canRecord) {
            Button w1 = new Button(p1.getName() + " wins",
                    e -> recordWinner(fixture, p1));
            w1.addClassName("results-win-button");
            w1.addThemeVariants(ButtonVariant.PRIMARY);
            Button w2 = new Button(p2.getName() + " wins",
                    e -> recordWinner(fixture, p2));
            w2.addClassName("results-win-button");
            w2.addThemeVariants(ButtonVariant.PRIMARY);
            card.add(w1, w2);
        } else {
            Span pending = new Span("Waiting for previous round");
            pending.addClassName("results-pending");
            card.add(pending);
        }

        return card;
    }

    private Div buildSlot(Participant p, Participant winner) {
        Div slot = new Div();
        slot.addClassName("results-slot");
        Span name = new Span(p == null ? "TBD" : p.getName());
        if (p == null) {
            name.addClassName("results-tbd");
        } else if (winner != null && p.getId().equals(winner.getId())) {
            slot.addClassName("results-winner");
        }
        slot.add(name);
        return slot;
    }

    private void recordWinner(Fixture fixture, Participant winner) {
        try {
            resultService.recordWinner(fixture.getId(), winner.getId());
            Notification.show(winner.getName() + " advances");
            tournament = tournamentService.findById(tournament.getId());
            render();
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private static String roundLabel(int round, int totalRounds) {
        int fromEnd = totalRounds - round;
        return switch (fromEnd) {
            case 0 -> "Final";
            case 1 -> "Semifinals";
            case 2 -> "Quarterfinals";
            default -> "Round " + round;
        };
    }

    private static String statusClass(TournamentStatus status) {
        return "status-" + status.name().toLowerCase().replace('_', '-');
    }

    /** Used by tests to fetch the current tournament after navigation. */
    Tournament currentTournament() {
        return tournament;
    }
}
