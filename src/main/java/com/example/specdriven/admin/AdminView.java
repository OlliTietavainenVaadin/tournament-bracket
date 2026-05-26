package com.example.specdriven.admin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@PageTitle("Admin")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView() {
        addClassName("admin-view");
        setPadding(true);

        add(new H1("Admin"));
        add(new Paragraph(
                "Manage tournaments, review registrations, and record results from here."));

        UnorderedList list = new UnorderedList();
        list.addClassName("admin-nav");
        list.add(new ListItem(new RouterLink("Manage Tournaments", ManageTournamentsView.class)));
        add(list);
    }
}
