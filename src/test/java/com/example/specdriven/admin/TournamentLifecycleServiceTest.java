package com.example.specdriven.admin;

import java.time.LocalDate;
import java.util.Random;

import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TournamentLifecycleServiceTest {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentRepository tournamentRepository;

    private Tournament newDraft(String title) {
        Tournament t = new Tournament();
        t.setTitle(title);
        t.setMaxParticipants(8);
        t.setStatus(TournamentStatus.CREATED);
        return t;
    }

    private void addAccepted(Tournament t, int count) {
        for (int i = 0; i < count; i++) {
            Participant p = new Participant();
            p.setName("p" + i + "-" + t.getTitle());
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            p.setTournament(t);
            t.getParticipants().add(p);
        }
    }

    @Test
    void rejectsNewTournamentWithoutMaxParticipants() {
        Tournament t = new Tournament();
        t.setTitle("No max");
        t.setMaxParticipants(null);
        assertThrows(IllegalArgumentException.class, () -> tournamentService.save(t));
    }

    @Test
    void rejectsMaxParticipantsOutOfRange() {
        Tournament t = new Tournament();
        t.setTitle("Bad max");
        t.setMaxParticipants(40);
        assertThrows(IllegalArgumentException.class, () -> tournamentService.save(t));
    }

    @Test
    void rejectsStartDateInThePastWhenCreating() {
        Tournament t = new Tournament();
        t.setTitle("Past");
        t.setMaxParticipants(8);
        t.setStartDate(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.save(t));
    }

    @Test
    void openForRegistrationsRequiresDraft() {
        Tournament t = tournamentService.save(newDraft("Draft → Open"));
        Tournament opened = tournamentService.openForRegistrations(t.getId());
        assertEquals(TournamentStatus.OPEN_FOR_REGISTRATIONS, opened.getStatus());

        assertThrows(IllegalStateException.class,
                () -> tournamentService.openForRegistrations(opened.getId()),
                "Cannot re-open a tournament that is already open");
    }

    @Test
    void startTournamentGeneratesBracketAndChangesStatus() {
        Tournament t = tournamentService.save(newDraft("Run me"));
        tournamentService.openForRegistrations(t.getId());
        Tournament reloaded = tournamentRepository.findById(t.getId()).orElseThrow();
        addAccepted(reloaded, 5);
        tournamentRepository.save(reloaded);

        Tournament started = tournamentService.startTournament(t.getId(), new Random(1L));
        assertEquals(TournamentStatus.ONGOING, started.getStatus());
        assertFalse(started.getFixtures().isEmpty(), "Fixtures should be generated");
    }

    @Test
    void startTournamentRejectsTooFewAcceptedParticipants() {
        Tournament t = tournamentService.save(newDraft("Too small"));
        tournamentService.openForRegistrations(t.getId());
        Tournament reloaded = tournamentRepository.findById(t.getId()).orElseThrow();
        addAccepted(reloaded, 2);
        tournamentRepository.save(reloaded);

        assertThrows(IllegalStateException.class,
                () -> tournamentService.startTournament(t.getId(), new Random(0)));
    }

    @Test
    void ongoingTournamentCannotBeEditedOrDeleted() {
        Tournament t = tournamentService.save(newDraft("Ongoing"));
        tournamentService.openForRegistrations(t.getId());
        Tournament reloaded = tournamentRepository.findById(t.getId()).orElseThrow();
        addAccepted(reloaded, 4);
        tournamentRepository.save(reloaded);
        tournamentService.startTournament(t.getId(), new Random(0));

        Tournament ongoing = tournamentRepository.findById(t.getId()).orElseThrow();
        ongoing.setTitle("Trying to edit");

        assertThrows(IllegalStateException.class, () -> tournamentService.save(ongoing));
        assertThrows(IllegalStateException.class, () -> tournamentService.delete(ongoing));
    }
}
