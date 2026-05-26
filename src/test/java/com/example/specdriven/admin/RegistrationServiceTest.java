package com.example.specdriven.admin;

import com.example.specdriven.bracket.Participant;
import com.example.specdriven.bracket.RegistrationStatus;
import com.example.specdriven.bracket.Tournament;
import com.example.specdriven.bracket.TournamentRepository;
import com.example.specdriven.bracket.TournamentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private TournamentRepository tournamentRepository;

    private Tournament newOpenTournament(int maxParticipants) {
        Tournament t = new Tournament();
        t.setTitle("Reg test " + System.nanoTime());
        t.setMaxParticipants(maxParticipants);
        t.setStatus(TournamentStatus.OPEN_FOR_REGISTRATIONS);
        return tournamentRepository.save(t);
    }

    @Test
    void registerCreatesPendingParticipant() {
        Tournament t = newOpenTournament(4);
        Participant p = registrationService.register(t.getId(), "alice");
        assertEquals(RegistrationStatus.PENDING, p.getRegistrationStatus());
        assertEquals("alice", p.getUsername());
    }

    @Test
    void cannotRegisterTwiceForSameTournament() {
        Tournament t = newOpenTournament(4);
        registrationService.register(t.getId(), "alice");
        assertThrows(IllegalStateException.class,
                () -> registrationService.register(t.getId(), "alice"));
    }

    @Test
    void cannotRegisterWhenTournamentIsFull() {
        Tournament t = newOpenTournament(3);
        registrationService.register(t.getId(), "alice");
        registrationService.register(t.getId(), "bob");
        registrationService.register(t.getId(), "cathy");
        assertThrows(IllegalStateException.class,
                () -> registrationService.register(t.getId(), "dave"));
    }

    @Test
    void cannotRegisterWhenTournamentIsNotOpen() {
        Tournament t = new Tournament();
        t.setTitle("Not open");
        t.setMaxParticipants(8);
        t.setStatus(TournamentStatus.ONGOING);
        Tournament saved = tournamentRepository.save(t);
        assertThrows(IllegalStateException.class,
                () -> registrationService.register(saved.getId(), "alice"));
    }

    @Test
    void acceptTransitionsStatus() {
        Tournament t = newOpenTournament(4);
        Participant p = registrationService.register(t.getId(), "alice");
        Participant accepted = registrationService.accept(p.getId());
        assertEquals(RegistrationStatus.ACCEPTED, accepted.getRegistrationStatus());
    }

    @Test
    void declineTransitionsStatus() {
        Tournament t = newOpenTournament(4);
        Participant p = registrationService.register(t.getId(), "alice");
        Participant declined = registrationService.decline(p.getId());
        assertEquals(RegistrationStatus.DECLINED, declined.getRegistrationStatus());
    }

    @Test
    void cannotAcceptBeyondMaxParticipants() {
        // Set up: max=3, 3 pending, accept 2 -> third accept ok, but if max
        // is reduced afterwards (admin tweaks), the next accept fails.
        Tournament t = newOpenTournament(4);
        Participant a = registrationService.register(t.getId(), "alice");
        Participant b = registrationService.register(t.getId(), "bob");
        Participant c = registrationService.register(t.getId(), "cathy");
        registrationService.accept(a.getId());
        registrationService.accept(b.getId());

        // Admin shrinks the tournament size so we are now at the cap.
        Tournament t2 = tournamentRepository.findById(t.getId()).orElseThrow();
        t2.setMaxParticipants(2);
        tournamentRepository.save(t2);

        assertThrows(IllegalStateException.class,
                () -> registrationService.accept(c.getId()),
                "Accepting beyond max participants must be rejected");
    }

    @Test
    void cannotAcceptNonPendingParticipant() {
        Tournament t = newOpenTournament(4);
        Participant p = registrationService.register(t.getId(), "alice");
        registrationService.accept(p.getId());
        assertThrows(IllegalStateException.class,
                () -> registrationService.accept(p.getId()));
    }
}
