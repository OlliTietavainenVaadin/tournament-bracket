package com.example.specdriven.bracket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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

        Tournament tournament = new Tournament();
        tournament.setTitle("Chess Tournament 1");
        tournament.setDescription("An 8-player single elimination chess tournament");
        tournament.setMaxParticipants(8);

        List<String> playerNames = List.of(
                "James", "Tina", "Lizzy B", "Reindeer Rudolph",
                "Benoit", "Mary", "Robot Rolf", "Camper Vance"
        );

        List<Participant> participants = new ArrayList<>();
        for (String name : playerNames) {
            Participant p = new Participant();
            p.setName(name);
            p.setTournament(tournament);
            participants.add(p);
        }
        tournament.setParticipants(participants);

        // Shuffle for random pairing
        List<Participant> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled);

        // Create bracket fixtures: 3 rounds for 8 players
        // Round 1: 4 quarterfinals
        // Round 2: 2 semifinals
        // Round 3: 1 final
        List<Fixture> fixtures = new ArrayList<>();

        // Create all fixtures first (final → semis → quarters) so we can set nextFixture
        Fixture finalFixture = createFixture(tournament, 3, 0);
        fixtures.add(finalFixture);

        Fixture semi1 = createFixture(tournament, 2, 0);
        semi1.setNextFixture(finalFixture);
        fixtures.add(semi1);

        Fixture semi2 = createFixture(tournament, 2, 1);
        semi2.setNextFixture(finalFixture);
        fixtures.add(semi2);

        Fixture qf1 = createFixture(tournament, 1, 0);
        qf1.setNextFixture(semi1);
        qf1.setParticipant1(shuffled.get(0));
        qf1.setParticipant2(shuffled.get(1));
        fixtures.add(qf1);

        Fixture qf2 = createFixture(tournament, 1, 1);
        qf2.setNextFixture(semi1);
        qf2.setParticipant1(shuffled.get(2));
        qf2.setParticipant2(shuffled.get(3));
        fixtures.add(qf2);

        Fixture qf3 = createFixture(tournament, 1, 2);
        qf3.setNextFixture(semi2);
        qf3.setParticipant1(shuffled.get(4));
        qf3.setParticipant2(shuffled.get(5));
        fixtures.add(qf3);

        Fixture qf4 = createFixture(tournament, 1, 3);
        qf4.setNextFixture(semi2);
        qf4.setParticipant1(shuffled.get(6));
        qf4.setParticipant2(shuffled.get(7));
        fixtures.add(qf4);

        tournament.setFixtures(fixtures);
        tournamentRepository.save(tournament);
    }

    private Fixture createFixture(Tournament tournament, int round, int position) {
        Fixture fixture = new Fixture();
        fixture.setTournament(tournament);
        fixture.setRound(round);
        fixture.setPosition(position);
        fixture.setPlayDate(LocalDate.now().plusDays(round * 7L));
        return fixture;
    }
}
