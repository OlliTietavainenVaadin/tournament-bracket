package com.example.specdriven.admin;

import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.IntegerField;
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

    private Tournament newDraft(String title) {
        Tournament t = new Tournament();
        t.setTitle(title);
        t.setMaxParticipants(8);
        t.setStatus(TournamentStatus.CREATED);
        return t;
    }

    @Test
    void viewShowsTitle() {
        navigate(ManageTournamentsView.class);
        var title = $(H1.class).first();
        assertEquals("Manage Tournaments", title.getText());
    }

    @Test
    void canSeeListOfAllTournaments() {
        navigate(ManageTournamentsView.class);

        var grid = $(Grid.class).first();
        assertNotNull(grid, "Should display a tournament grid");
    }

    @Test
    void canAddNewTournament() {
        navigate(ManageTournamentsView.class);

        var createButton = $(Button.class).withText("Create tournament").first();
        test(createButton).click();

        var nameField = $(TextField.class).withCaption("Name").first();
        var descriptionField = $(TextArea.class).withCaption("Description").first();
        var startDateField = $(DatePicker.class).withCaption("Starting Date").first();
        var maxField = $(IntegerField.class).withCaption("Max Participants").first();

        test(nameField).setValue("Test Tournament");
        test(descriptionField).setValue("A test tournament");
        test(startDateField).setValue(LocalDate.now().plusDays(7));
        test(maxField).setValue(8);

        var saveButton = $(Button.class).withText("Save draft").first();
        test(saveButton).click();

        var saved = tournamentRepository.findAll().stream()
                .filter(t -> "Test Tournament".equals(t.getTitle()))
                .findFirst();
        assertTrue(saved.isPresent(), "Tournament should be saved");
        assertEquals(TournamentStatus.CREATED, saved.get().getStatus(),
                "New tournaments are saved as drafts");
        assertEquals(8, saved.get().getMaxParticipants());
    }

    @Test
    void canEditExistingTournament() {
        Tournament tournament = newDraft("Editable Tournament");
        tournament.setDescription("Original description");
        tournamentRepository.save(tournament);

        navigate(ManageTournamentsView.class);

        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        var target = grid.getListDataView().getItems()
                .filter(t -> "Editable Tournament".equals(t.getTitle()))
                .findFirst().orElseThrow();
        grid.select(target);

        var editButton = $(Button.class).withText("Edit").first();
        test(editButton).click();

        var nameField = $(TextField.class).withCaption("Name").first();
        test(nameField).setValue("Updated Tournament");

        var saveButton = $(Button.class).withText("Save").first();
        test(saveButton).click();

        var updated = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals("Updated Tournament", updated.getTitle());
    }

    @Test
    void canDeleteTournamentWithConfirmation() {
        Tournament tournament = newDraft("Delete Me");
        tournamentRepository.save(tournament);
        Long id = tournament.getId();

        navigate(ManageTournamentsView.class);

        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        var target = grid.getListDataView().getItems()
                .filter(t -> "Delete Me".equals(t.getTitle()))
                .findFirst().orElseThrow();
        grid.select(target);

        var deleteButton = $(Button.class).withText("Delete").first();
        test(deleteButton).click();

        var confirmDialog = $(ConfirmDialog.class).first();
        assertNotNull(confirmDialog, "Confirm dialog should appear before deletion");

        test(confirmDialog).confirm();

        assertFalse(tournamentRepository.existsById(id), "Tournament should be deleted");
    }

    @Test
    void openForRegistrationsActionTransitionsStatus() {
        Tournament tournament = newDraft("To Open");
        tournamentRepository.save(tournament);
        Long id = tournament.getId();

        navigate(ManageTournamentsView.class);

        @SuppressWarnings("unchecked")
        Grid<Tournament> grid = $(Grid.class).first();
        var target = grid.getListDataView().getItems()
                .filter(t -> "To Open".equals(t.getTitle()))
                .findFirst().orElseThrow();
        grid.select(target);

        var openButton = $(Button.class).withText("Open for registrations").first();
        assertTrue(openButton.isEnabled(),
                "Open for registrations should be enabled on a draft tournament");
        test(openButton).click();

        var updated = tournamentRepository.findById(id).orElseThrow();
        assertEquals(TournamentStatus.OPEN_FOR_REGISTRATIONS, updated.getStatus());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserCannotAccessManageView() {
        assertThrows(Exception.class, () -> navigate(ManageTournamentsView.class),
                "Anonymous users cannot access the manage tournaments view");
    }
}
