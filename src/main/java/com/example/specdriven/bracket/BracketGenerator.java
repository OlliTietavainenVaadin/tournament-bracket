package com.example.specdriven.bracket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Builds a single-elimination bracket from a list of accepted participants. The
 * resulting fixtures are linked through {@link Fixture#getNextFixture()} so the
 * winner of each match can advance automatically.
 */
public final class BracketGenerator {

    private BracketGenerator() {}

    /**
     * Build a fully linked bracket of fixtures for the supplied participants.
     *
     * <p>The bracket size is the smallest power of two greater than or equal to
     * {@code participants.size()}. Missing slots become byes — a first-round
     * fixture where one side is {@code null} immediately advances the present
     * participant to the next round.
     *
     * @param tournament   the parent tournament; fixtures are linked back to it.
     * @param participants the accepted participants, in any order.
     * @param random       randomness source for first-round seeding (use a
     *                     deterministic seed for tests).
     * @return all created fixtures (final fixture last).
     */
    public static List<Fixture> build(Tournament tournament,
                                      List<Participant> participants,
                                      Random random) {
        if (participants == null || participants.size() < 3) {
            throw new IllegalArgumentException(
                    "A tournament needs at least 3 accepted participants to start");
        }
        if (participants.size() > 32) {
            throw new IllegalArgumentException(
                    "A tournament can have at most 32 participants");
        }

        int bracketSize = nextPowerOfTwo(participants.size());
        int rounds = Integer.numberOfTrailingZeros(bracketSize);

        // Build fixtures in reverse order so we can wire up nextFixture references.
        List<List<Fixture>> fixturesByRound = new ArrayList<>(rounds);
        for (int i = 0; i < rounds; i++) {
            fixturesByRound.add(new ArrayList<>());
        }

        // The single final at round = rounds (1-based)
        Fixture finalFixture = createFixture(tournament, rounds, 0);
        fixturesByRound.get(rounds - 1).add(finalFixture);

        // Build the rest of the rounds, top-down: rounds-1, rounds-2 ... 1
        for (int round = rounds - 1; round >= 1; round--) {
            int matchesThisRound = 1 << (rounds - round);
            for (int pos = 0; pos < matchesThisRound; pos++) {
                Fixture f = createFixture(tournament, round, pos);
                Fixture parent = fixturesByRound.get(round).get(pos / 2);
                f.setNextFixture(parent);
                fixturesByRound.get(round - 1).add(f);
            }
        }

        // Seed first round with shuffled participants; missing slots are byes.
        List<Participant> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled, random);

        List<Fixture> firstRound = fixturesByRound.get(0);
        int idx = 0;
        for (Fixture f : firstRound) {
            Participant p1 = idx < shuffled.size() ? shuffled.get(idx++) : null;
            Participant p2 = idx < shuffled.size() ? shuffled.get(idx++) : null;
            f.setParticipant1(p1);
            f.setParticipant2(p2);
        }

        // Apply byes: a first-round fixture with exactly one participant
        // immediately advances that participant.
        for (Fixture f : firstRound) {
            boolean p1Present = f.getParticipant1() != null;
            boolean p2Present = f.getParticipant2() != null;
            if (p1Present ^ p2Present) {
                Participant winner = p1Present ? f.getParticipant1() : f.getParticipant2();
                f.setWinner(winner);
                advanceWinner(f, winner);
            }
        }

        // Flatten in round/position order (round 1 first, final last).
        List<Fixture> all = new ArrayList<>();
        for (List<Fixture> round : fixturesByRound) {
            all.addAll(round);
        }
        return all;
    }

    /**
     * Set the winner on the next fixture in the appropriate slot. Fixtures with
     * an even position feed into participant1 of their nextFixture; odd
     * positions feed into participant2.
     */
    public static void advanceWinner(Fixture from, Participant winner) {
        Fixture next = from.getNextFixture();
        if (next == null) {
            return;
        }
        if (from.getPosition() % 2 == 0) {
            next.setParticipant1(winner);
        } else {
            next.setParticipant2(winner);
        }
    }

    private static Fixture createFixture(Tournament tournament, int round, int position) {
        Fixture fixture = new Fixture();
        fixture.setTournament(tournament);
        fixture.setRound(round);
        fixture.setPosition(position);
        fixture.setPlayDate(LocalDate.now().plusDays(round * 7L));
        return fixture;
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p <<= 1;
        }
        return p;
    }
}
