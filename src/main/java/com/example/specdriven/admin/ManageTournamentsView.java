package com.example.specdriven.admin;

import com.example.specdriven.bracket.Tournament;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/manage")
@PageTitle("Manage Tournaments")
@RolesAllowed("ADMIN")
public class ManageTournamentsView extends VerticalLayout {

    private final TournamentService tournamentService;
    private final Grid<Tournament> grid = new Grid<>(Tournament.class, false);
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");

    public ManageTournamentsView(TournamentService tournamentService) {
        this.tournamentService = tournamentService;

        add(new H1("Manage Tournaments"));

        Button createButton = new Button("Create tournament", e -> openForm(new Tournament()));
        createButton.addThemeVariants(ButtonVariant.PRIMARY);

        editButton.setEnabled(false);
        editButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(this::openForm));

        deleteButton.setEnabled(false);
        deleteButton.addThemeVariants(ButtonVariant.ERROR);
        deleteButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(this::confirmDelete));

        HorizontalLayout toolbar = new HorizontalLayout(createButton, editButton, deleteButton);
        add(toolbar);

        configureGrid();
        add(grid);

        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Tournament::getTitle).setHeader("Name").setSortable(true);
        grid.addColumn(Tournament::getDescription).setHeader("Description");
        grid.addColumn(Tournament::getStartDate).setHeader("Starting Date").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(e -> {
            boolean selected = e.getValue() != null;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        });
    }

    private void openForm(Tournament tournament) {
        boolean isNew = tournament.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Create Tournament" : "Edit Tournament");

        TextField nameField = new TextField("Name");
        nameField.setValue(tournament.getTitle() != null ? tournament.getTitle() : "");
        nameField.setRequired(true);
        nameField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(tournament.getDescription() != null ? tournament.getDescription() : "");
        descriptionField.setWidthFull();

        DatePicker startDateField = new DatePicker("Starting Date");
        if (tournament.getStartDate() != null) {
            startDateField.setValue(tournament.getStartDate());
        }

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, descriptionField, startDateField);
        formLayout.setColspan(nameField, 2);
        formLayout.setColspan(descriptionField, 2);
        dialog.add(formLayout);

        String saveLabel = isNew ? "Open for registrations" : "Save";
        Button saveButton = new Button(saveLabel, e -> {
            tournament.setTitle(nameField.getValue());
            tournament.setDescription(descriptionField.getValue());
            tournament.setStartDate(startDateField.getValue());
            try {
                tournamentService.save(tournament);
                dialog.close();
                refreshGrid();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void confirmDelete(Tournament tournament) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete \"" + tournament.getTitle() + "\"?");
        dialog.setText("Are you sure you want to permanently delete this tournament?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            tournamentService.delete(tournament);
            refreshGrid();
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(tournamentService.findAll());
    }
}
