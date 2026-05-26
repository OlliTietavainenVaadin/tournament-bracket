package com.example.specdriven.bracket;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@PageTitle("Tournaments")
@AnonymousAllowed
public class TournamentListView extends VerticalLayout {

    private static final Comparator<Tournament> STATUS_ORDER = Comparator
            .comparingInt((Tournament t) -> statusRank(t.getStatus()))
            .thenComparing(Tournament::getStartDate,
                    Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Tournament::getTitle,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    public TournamentListView(BracketService bracketService) {
        addClassName("tournament-list-view");
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Tournaments");
        title.addClassName("tournament-list-title");
        add(title);

        List<Tournament> visible = bracketService.getAllTournaments().stream()
                .filter(t -> t.getStatus() != TournamentStatus.CREATED)
                .sorted(STATUS_ORDER)
                .toList();

        if (visible.isEmpty()) {
            Paragraph empty = new Paragraph("No tournaments available yet — check back soon.");
            empty.addClassName("tournament-list-empty");
            add(empty);
            return;
        }

        Div list = new Div();
        list.addClassName("tournament-list");

        for (Tournament tournament : visible) {
            list.add(buildCard(tournament));
        }
        add(list);
    }

    private Div buildCard(Tournament tournament) {
        Div card = new Div();
        card.addClassName("tournament-list-card");

        Div headerRow = new Div();
        headerRow.addClassName("tournament-list-header");

        RouterLink link = new RouterLink(tournament.getTitle(),
                BracketView.class,
                new RouteParameters(new RouteParam("tournamentId", tournament.getId().toString())));
        link.addClassName("tournament-list-link");
        headerRow.add(link);

        Span statusBadge = new Span(tournament.getStatus().displayLabel());
        statusBadge.addClassName("status-badge");
        statusBadge.addClassName(statusClass(tournament.getStatus()));
        headerRow.add(statusBadge);

        card.add(headerRow);

        if (tournament.getDescription() != null && !tournament.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(tournament.getDescription());
            desc.addClassName("tournament-list-description");
            card.add(desc);
        }

        Div meta = new Div();
        meta.addClassName("tournament-list-meta");
        meta.add(metaItem("Players",
                tournament.acceptedParticipantCount() + " / " + tournament.getMaxParticipants()));
        if (tournament.getStartDate() != null) {
            meta.add(metaItem("Starts", tournament.getStartDate().toString()));
        }
        card.add(meta);

        return card;
    }

    private static Div metaItem(String label, String value) {
        Div item = new Div();
        item.addClassName("tournament-list-meta-item");
        Span labelSpan = new Span(label);
        labelSpan.addClassName("tournament-list-meta-label");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("tournament-list-meta-value");
        item.add(labelSpan, valueSpan);
        return item;
    }

    private static int statusRank(TournamentStatus status) {
        if (status == null) return 99;
        return switch (status) {
            case OPEN_FOR_REGISTRATIONS -> 0;
            case ONGOING -> 1;
            case FINISHED -> 2;
            case CREATED -> 3;
        };
    }

    private static String statusClass(TournamentStatus status) {
        return "status-" + status.name().toLowerCase().replace('_', '-');
    }
}
