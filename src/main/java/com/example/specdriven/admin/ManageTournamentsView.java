package com.example.specdriven.admin;

import java.util.Random;

import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/manage")
@PageTitle("Manage Tournaments")
@RolesAllowed("ADMIN")
public class ManageTournamentsView extends VerticalLayout {

    private final TournamentService tournamentService;
    private final Grid<Tournament> grid = new Grid<>(Tournament.class, false);
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final Button openButton = new Button("Open for registrations");
    private final Button reviewButton = new Button("Review registrations");
    private final Button startButton = new Button("Start tournament");
    private final Button resultsButton = new Button("Record results");

    public ManageTournamentsView(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
        addClassName("manage-tournaments-view");
        setPadding(true);

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

        openButton.setEnabled(false);
        openButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(this::openForRegistrations));

        reviewButton.setEnabled(false);
        reviewButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(t -> getUI().ifPresent(ui -> ui.navigate(
                        ReviewRegistrationsView.class,
                        new RouteParameters(new RouteParam("tournamentId", t.getId().toString()))))));

        startButton.setEnabled(false);
        startButton.addThemeVariants(ButtonVariant.PRIMARY);
        startButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(this::confirmStart));

        resultsButton.setEnabled(false);
        resultsButton.addThemeVariants(ButtonVariant.PRIMARY);
        resultsButton.addClickListener(e -> grid.asSingleSelect().getOptionalValue()
                .ifPresent(t -> getUI().ifPresent(ui -> ui.navigate(
                        RecordResultsView.class,
                        new RouteParameters(new RouteParam("tournamentId", t.getId().toString()))))));

        HorizontalLayout toolbar = new HorizontalLayout(
                createButton, editButton, deleteButton,
                openButton, reviewButton, startButton, resultsButton);
        toolbar.addClassName("manage-toolbar");
        add(toolbar);

        configureGrid();
        add(grid);

        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(t -> t.getStatus() == null ? "" : t.getStatus().displayLabel())
                .setHeader("Status").setSortable(true).setKey("status");
        grid.addColumn(Tournament::getTitle).setHeader("Name").setSortable(true);
        grid.addColumn(Tournament::getDescription).setHeader("Description");
        grid.addColumn(Tournament::getStartDate).setHeader("Starting Date").setSortable(true);
        grid.addColumn(t -> t.acceptedParticipantCount() + " / " + t.getMaxParticipants())
                .setHeader("Players").setSortable(false);

        grid.asSingleSelect().addValueChangeListener(e -> updateToolbar(e.getValue()));
    }

    private void updateToolbar(Tournament t) {
        TournamentStatus s = t == null ? null : t.getStatus();
        boolean editable = s == TournamentStatus.CREATED || s == TournamentStatus.OPEN_FOR_REGISTRATIONS;
        editButton.setEnabled(editable);
        deleteButton.setEnabled(editable);
        openButton.setEnabled(s == TournamentStatus.CREATED);
        reviewButton.setEnabled(s == TournamentStatus.OPEN_FOR_REGISTRATIONS);
        startButton.setEnabled(s == TournamentStatus.OPEN_FOR_REGISTRATIONS);
        resultsButton.setEnabled(s == TournamentStatus.ONGOING);
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

        IntegerField maxField = new IntegerField("Max Participants");
        maxField.setMin(3);
        maxField.setMax(32);
        maxField.setStepButtonsVisible(true);
        if (tournament.getMaxParticipants() != null) {
            maxField.setValue(tournament.getMaxParticipants());
        } else {
            maxField.setValue(8);
        }

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, descriptionField, startDateField, maxField);
        formLayout.setColspan(nameField, 2);
        formLayout.setColspan(descriptionField, 2);
        dialog.add(formLayout);

        String saveLabel = isNew ? "Save draft" : "Save";
        Button saveButton = new Button(saveLabel, e -> {
            tournament.setTitle(nameField.getValue());
            tournament.setDescription(descriptionField.getValue());
            tournament.setStartDate(startDateField.getValue());
            tournament.setMaxParticipants(maxField.getValue());
            try {
                tournamentService.save(tournament);
                dialog.close();
                refreshGrid();
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openForRegistrations(Tournament tournament) {
        try {
            tournamentService.openForRegistrations(tournament.getId());
            refreshGrid();
            Notification.show("Registrations are now open for \"" + tournament.getTitle() + "\"");
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void confirmStart(Tournament tournament) {
        long accepted = tournament.acceptedParticipantCount();
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Start \"" + tournament.getTitle() + "\"?");
        dialog.add(new Span(
                accepted + " accepted players will be seeded into a single-elimination bracket. "
                        + "This action is irreversible."));
        dialog.setCancelable(true);
        dialog.setConfirmText("Start");
        dialog.setConfirmButtonTheme("primary");
        dialog.addConfirmListener(e -> {
            try {
                tournamentService.startTournament(tournament.getId(), new Random());
                refreshGrid();
                Notification.show("Tournament started");
                getUI().ifPresent(ui -> ui.navigate(
                        RecordResultsView.class,
                        new RouteParameters(new RouteParam("tournamentId", tournament.getId().toString()))));
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
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
            try {
                tournamentService.delete(tournament);
                refreshGrid();
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(tournamentService.findAll());
        updateToolbar(null);
    }
}
