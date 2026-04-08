package com.example.specdriven.admin;

import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class ManageTournamentsTest extends SpringBrowserlessTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Test
    void canSeeListOfAllTournaments() {
        navigate(ManageTournamentsView.class);

        var grid = $(Grid.class).first();
        assertNotNull(grid, "Should display a tournament grid");
    }

    @Test
    void canAddNewTournament() {
        navigate(ManageTournamentsView.class);

        // Click "Create tournament" button
        var createButton = $(Button.class).withText("Create tournament").first();
        test(createButton).click();

        // Fill in the form
        var nameField = $(TextField.class).withCaption("Name").first();
        var descriptionField = $(TextArea.class).withCaption("Description").first();
        var startDateField = $(DatePicker.class).withCaption("Starting Date").first();

        test(nameField).setValue("Test Tournament");
        test(descriptionField).setValue("A test tournament");
        test(startDateField).setValue(LocalDate.now().plusDays(7));

        // Click save
        var saveButton = $(Button.class).withText("Open for registrations").first();
        test(saveButton).click();

        // Verify tournament was saved
        var saved = tournamentRepository.findAll().stream()
                .filter(t -> "Test Tournament".equals(t.getTitle()))
                .findFirst();
        assertTrue(saved.isPresent(), "Tournament should be saved to database");
        assertEquals("A test tournament", saved.get().getDescription());
        assertEquals(LocalDate.now().plusDays(7), saved.get().getStartDate());
    }

    @Test
    void canEditExistingTournament() {
        // Create a tournament to edit
        Tournament tournament = new Tournament();
        tournament.setTitle("Editable Tournament");
        tournament.setDescription("Original description");
        tournamentRepository.save(tournament);

        navigate(ManageTournamentsView.class);

        // Select the tournament in the grid
        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        var items = grid.getListDataView().getItems().toList();
        var target = items.stream()
                .filter(t -> "Editable Tournament".equals(t.getTitle()))
                .findFirst().orElseThrow();
        grid.select(target);

        // Click "Edit" button
        var editButton = $(Button.class).withText("Edit").first();
        test(editButton).click();

        // Modify the name
        var nameField = $(TextField.class).withCaption("Name").first();
        test(nameField).setValue("Updated Tournament");

        // Click save
        var saveButton = $(Button.class).withText("Save").first();
        test(saveButton).click();

        // Verify update
        var updated = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals("Updated Tournament", updated.getTitle());
    }

    @Test
    void canDeleteTournamentWithConfirmation() {
        // Create a tournament to delete
        Tournament tournament = new Tournament();
        tournament.setTitle("Delete Me");
        tournamentRepository.save(tournament);
        Long id = tournament.getId();

        navigate(ManageTournamentsView.class);

        // Select the tournament in the grid
        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        var items = grid.getListDataView().getItems().toList();
        var target = items.stream()
                .filter(t -> "Delete Me".equals(t.getTitle()))
                .findFirst().orElseThrow();
        grid.select(target);

        // Click "Delete" button
        var deleteButton = $(Button.class).withText("Delete").first();
        test(deleteButton).click();

        // Confirm dialog should appear
        var confirmDialog = $(ConfirmDialog.class).first();
        assertNotNull(confirmDialog, "Confirm dialog should appear before deletion");

        // Confirm deletion
        test(confirmDialog).confirm();

        // Verify tournament was deleted
        assertFalse(tournamentRepository.existsById(id), "Tournament should be deleted");
    }

    @Test
    void viewShowsTitle() {
        navigate(ManageTournamentsView.class);
        var title = $(H1.class).first();
        assertEquals("Manage Tournaments", title.getText());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserCannotAccessManageView() {
        assertThrows(Exception.class, () -> navigate(ManageTournamentsView.class),
                "Anonymous users should not be able to access the manage tournaments view");
    }
}
