package com.example.specdriven.bracket;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("view/:tournamentId")
@PageTitle("Tournament Bracket")
@AnonymousAllowed
public class BracketView extends VerticalLayout implements BeforeEnterObserver {

    private final BracketService bracketService;

    public BracketView(BracketService bracketService) {
        this.bracketService = bracketService;
        addClassName("bracket-view");
        setSizeFull();
        setPadding(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("tournamentId").orElse(null);
        if (idParam == null) {
            event.forwardTo(TournamentListView.class);
            return;
        }

        Long tournamentId;
        try {
            tournamentId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            event.forwardTo(TournamentListView.class);
            return;
        }

        Tournament tournament = bracketService.getTournamentById(tournamentId);
        if (tournament == null) {
            event.forwardTo(TournamentListView.class);
            return;
        }

        buildHeader(tournament);
        buildBracket(tournament);
    }

    private void buildHeader(Tournament tournament) {
        H1 title = new H1(tournament.getTitle());
        title.addClassName("tournament-title");

        Div header = new Div();
        header.addClassName("tournament-header");
        header.add(title);

        if (tournament.getDescription() != null && !tournament.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(tournament.getDescription());
            description.addClassName("tournament-description");
            header.add(description);
        }

        add(header);
    }

    private void buildBracket(Tournament tournament) {
        Map<Integer, List<Fixture>> fixturesByRound = bracketService.getFixturesByRound(tournament);
        int totalRounds = bracketService.getTotalRounds(tournament);

        Div bracket = new Div();
        bracket.addClassName("bracket");

        for (int round = 1; round <= totalRounds; round++) {
            List<Fixture> roundFixtures = fixturesByRound.getOrDefault(round, List.of());
            roundFixtures = roundFixtures.stream()
                    .sorted(Comparator.comparingInt(Fixture::getPosition))
                    .toList();

            // Round column
            Div roundColumn = new Div();
            roundColumn.addClassName("round");

            String roundLabel = getRoundLabel(round, totalRounds);
            H3 roundHeader = new H3(roundLabel);
            roundHeader.addClassName("round-header");
            roundColumn.add(roundHeader);

            Div matchesContainer = new Div();
            matchesContainer.addClassName("round-matches");

            for (Fixture fixture : roundFixtures) {
                Div matchWrapper = new Div();
                matchWrapper.addClassName("match-wrapper");
                if (round > 1) {
                    matchWrapper.addClassName("has-prev");
                }
                if (round < totalRounds) {
                    matchWrapper.addClassName("has-next");
                }
                matchWrapper.add(createMatchCard(fixture));
                matchesContainer.add(matchWrapper);
            }

            roundColumn.add(matchesContainer);
            bracket.add(roundColumn);

            // Connector column between rounds
            if (round < totalRounds) {
                bracket.add(createConnectorColumn(roundFixtures.size()));
            }
        }

        // Winner display
        if (tournament.getWinner() != null) {
            Div winnerDisplay = new Div();
            winnerDisplay.addClassName("winner-display");
            Span trophy = new Span("Winner");
            trophy.addClassName("winner-label");
            Span winnerName = new Span(tournament.getWinner().getName());
            winnerName.addClassName("winner-name");
            winnerDisplay.add(trophy, winnerName);
            bracket.add(winnerDisplay);
        }

        add(bracket);
    }

    private Div createMatchCard(Fixture fixture) {
        Div card = new Div();
        card.addClassName("match-card");

        Div player1 = createPlayerSlot(fixture.getParticipant1(), fixture.getWinner());
        Div player2 = createPlayerSlot(fixture.getParticipant2(), fixture.getWinner());

        Div separator = new Div();
        separator.addClassName("match-separator");

        card.add(player1, separator, player2);
        return card;
    }

    private Div createPlayerSlot(Participant participant, Participant winner) {
        Div slot = new Div();
        slot.addClassName("player-slot");

        if (participant == null) {
            Span name = new Span("TBD");
            name.addClassName("player-name");
            name.addClassName("player-tbd");
            slot.add(name);
        } else {
            Span name = new Span(participant.getName());
            name.addClassName("player-name");
            slot.add(name);

            if (winner != null && winner.getId().equals(participant.getId())) {
                slot.addClassName("player-winner");
            }
        }

        return slot;
    }

    private Div createConnectorColumn(int fixtureCount) {
        Div column = new Div();
        column.addClassName("connector-column");

        // Invisible spacer matching round header height for vertical alignment
        Html headerSpacer = new Html(
                "<h3 class='round-header connector-header-spacer'>&nbsp;</h3>");
        column.getElement().appendChild(headerSpacer.getElement());

        Div groupsContainer = new Div();
        groupsContainer.addClassName("connector-groups");

        for (int i = 0; i < fixtureCount / 2; i++) {
            Div group = new Div();
            group.addClassName("connector-group");

            Div spacerTop = new Div();
            spacerTop.addClassName("connector-spacer");

            Div bracket = new Div();
            bracket.addClassName("connector-bracket");

            Div spacerBottom = new Div();
            spacerBottom.addClassName("connector-spacer");

            group.add(spacerTop, bracket, spacerBottom);
            groupsContainer.add(group);
        }

        column.add(groupsContainer);
        return column;
    }

    private String getRoundLabel(int round, int totalRounds) {
        int roundsFromEnd = totalRounds - round;
        return switch (roundsFromEnd) {
            case 0 -> "Final";
            case 1 -> "Semifinals";
            case 2 -> "Quarterfinals";
            default -> "Round " + round;
        };
    }
}
