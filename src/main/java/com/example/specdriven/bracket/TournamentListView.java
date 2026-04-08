package com.example.specdriven.bracket;

import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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

    public TournamentListView(BracketService bracketService) {
        addClassName("tournament-list-view");
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Tournaments");
        title.addClassName("tournament-list-title");
        add(title);

        List<Tournament> tournaments = bracketService.getAllTournaments();

        if (tournaments.isEmpty()) {
            add(new Paragraph("No tournaments available."));
            return;
        }

        Div list = new Div();
        list.addClassName("tournament-list");

        for (Tournament tournament : tournaments) {
            Div card = new Div();
            card.addClassName("tournament-list-card");

            RouterLink link = new RouterLink(tournament.getTitle(),
                    BracketView.class,
                    new RouteParameters(new RouteParam("tournamentId", tournament.getId().toString())));
            link.addClassName("tournament-list-link");
            card.add(link);

            if (tournament.getDescription() != null && !tournament.getDescription().isEmpty()) {
                Paragraph desc = new Paragraph(tournament.getDescription());
                desc.addClassName("tournament-list-description");
                card.add(desc);
            }

            list.add(card);
        }

        add(list);
    }
}
