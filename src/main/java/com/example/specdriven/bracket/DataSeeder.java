package com.example.specdriven.bracket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with the three demo tournaments described in the
 * datamodel spec. Runs only when the database is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final TournamentRepository tournamentRepository;

    public DataSeeder(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void run(String... args) {
        if (tournamentRepository.count() > 0) {
            return;
        }
        seedFinishedTournament();
        seedOngoingTournament();
        seedOpenTournament();
    }

    /** Tournament A — Spring Chess Classic, fully played out. */
    private void seedFinishedTournament() {
        Tournament t = newTournament("Spring Chess Classic",
                "An 8-player classic played out in full. Use it to inspect a completed bracket.",
                8,
                LocalDate.now().minusMonths(2),
                TournamentStatus.FINISHED);

        List<Participant> players = makeNamedParticipants(t, List.of(
                "James", "Tina", "Lizzy B", "Reindeer Rudolph",
                "Benoit", "Mary", "Robot Rolf", "Camper Vance"));
        t.setParticipants(players);

        Participant james = players.get(0);
        Participant tina = players.get(1);
        Participant lizzy = players.get(2);
        Participant rudolph = players.get(3);
        Participant benoit = players.get(4);
        Participant mary = players.get(5);
        Participant rolf = players.get(6);
        Participant vance = players.get(7);

        List<Fixture> fixtures = new ArrayList<>();
        Fixture finalFx = newFixture(t, 3, 0, lizzy, vance, lizzy, null);

        Fixture sf1 = newFixture(t, 2, 0, tina, lizzy, lizzy, finalFx);
        Fixture sf2 = newFixture(t, 2, 1, mary, vance, vance, finalFx);

        Fixture qf1 = newFixture(t, 1, 0, james, tina, tina, sf1);
        Fixture qf2 = newFixture(t, 1, 1, lizzy, rudolph, lizzy, sf1);
        Fixture qf3 = newFixture(t, 1, 2, benoit, mary, mary, sf2);
        Fixture qf4 = newFixture(t, 1, 3, rolf, vance, vance, sf2);

        fixtures.add(qf1);
        fixtures.add(qf2);
        fixtures.add(qf3);
        fixtures.add(qf4);
        fixtures.add(sf1);
        fixtures.add(sf2);
        fixtures.add(finalFx);
        t.setFixtures(fixtures);
        t.setWinner(lizzy);

        tournamentRepository.save(t);
    }

    /** Tournament B — Summer Chess Cup, round 1 complete. */
    private void seedOngoingTournament() {
        Tournament t = newTournament("Summer Chess Cup",
                "8 players, round 1 already played. Use it to record semifinal results.",
                8,
                LocalDate.now().minusDays(7),
                TournamentStatus.ONGOING);

        List<Participant> players = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            Participant p = new Participant();
            p.setName("player" + i);
            p.setUsername("player" + i);
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            p.setTournament(t);
            players.add(p);
        }
        t.setParticipants(players);

        // Fixed shuffle by using a seeded generator so the bracket is reproducible
        Random rnd = new Random(42L);
        List<Participant> shuffled = new ArrayList<>(players);
        java.util.Collections.shuffle(shuffled, rnd);

        Fixture finalFx = newFixture(t, 3, 0, null, null, null, null);
        Fixture sf1 = newFixture(t, 2, 0, null, null, null, finalFx);
        Fixture sf2 = newFixture(t, 2, 1, null, null, null, finalFx);

        Fixture qf1 = newFixture(t, 1, 0, shuffled.get(0), shuffled.get(1), shuffled.get(0), sf1);
        BracketGenerator.advanceWinner(qf1, shuffled.get(0));
        Fixture qf2 = newFixture(t, 1, 1, shuffled.get(2), shuffled.get(3), shuffled.get(3), sf1);
        BracketGenerator.advanceWinner(qf2, shuffled.get(3));
        Fixture qf3 = newFixture(t, 1, 2, shuffled.get(4), shuffled.get(5), shuffled.get(4), sf2);
        BracketGenerator.advanceWinner(qf3, shuffled.get(4));
        Fixture qf4 = newFixture(t, 1, 3, shuffled.get(6), shuffled.get(7), shuffled.get(7), sf2);
        BracketGenerator.advanceWinner(qf4, shuffled.get(7));

        t.setFixtures(new ArrayList<>(Arrays.asList(qf1, qf2, qf3, qf4, sf1, sf2, finalFx)));

        tournamentRepository.save(t);
    }

    /** Tournament C — Autumn Chess Open, accepting registrations. */
    private void seedOpenTournament() {
        Tournament t = newTournament("Autumn Chess Open",
                "Registrations are open. Up to 16 players will play single-elimination.",
                16,
                LocalDate.now().plusWeeks(2),
                TournamentStatus.OPEN_FOR_REGISTRATIONS);

        List<Participant> participants = new ArrayList<>();
        // player9..player11 -> ACCEPTED, player12..player14 -> PENDING
        for (int i = 9; i <= 14; i++) {
            Participant p = new Participant();
            String username = "player" + i;
            p.setName(username);
            p.setUsername(username);
            p.setRegistrationStatus(i <= 11 ? RegistrationStatus.ACCEPTED : RegistrationStatus.PENDING);
            p.setTournament(t);
            participants.add(p);
        }
        t.setParticipants(participants);
        tournamentRepository.save(t);
    }

    private Tournament newTournament(String title, String description, int maxParticipants,
                                     LocalDate startDate, TournamentStatus status) {
        Tournament t = new Tournament();
        t.setTitle(title);
        t.setDescription(description);
        t.setMaxParticipants(maxParticipants);
        t.setStartDate(startDate);
        t.setStatus(status);
        return t;
    }

    private List<Participant> makeNamedParticipants(Tournament tournament, List<String> names) {
        List<Participant> result = new ArrayList<>();
        for (String name : names) {
            Participant p = new Participant();
            p.setName(name);
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            p.setTournament(tournament);
            result.add(p);
        }
        return result;
    }

    private Fixture newFixture(Tournament tournament,
                               int round, int position,
                               Participant p1, Participant p2,
                               Participant winner, Fixture next) {
        Fixture f = new Fixture();
        f.setTournament(tournament);
        f.setRound(round);
        f.setPosition(position);
        f.setPlayDate(LocalDate.now().plusDays(round * 7L));
        f.setParticipant1(p1);
        f.setParticipant2(p2);
        f.setWinner(winner);
        f.setNextFixture(next);
        return f;
    }
}
