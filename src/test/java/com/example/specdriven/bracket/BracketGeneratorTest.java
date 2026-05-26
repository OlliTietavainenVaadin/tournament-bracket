package com.example.specdriven.bracket;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BracketGeneratorTest {

    private static List<Participant> mockParticipants(int count) {
        List<Participant> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Participant p = new Participant();
            p.setId((long) (i + 1));
            p.setName("p" + (i + 1));
            p.setRegistrationStatus(RegistrationStatus.ACCEPTED);
            result.add(p);
        }
        return result;
    }

    private static int countByRound(List<Fixture> fixtures, int round) {
        return (int) fixtures.stream().filter(f -> f.getRound() == round).count();
    }

    @Test
    void rejectsFewerThanThreeParticipants() {
        assertThrows(IllegalArgumentException.class,
                () -> BracketGenerator.build(new Tournament(), mockParticipants(2), new Random(0)));
    }

    @Test
    void rejectsMoreThanThirtyTwoParticipants() {
        assertThrows(IllegalArgumentException.class,
                () -> BracketGenerator.build(new Tournament(), mockParticipants(33), new Random(0)));
    }

    @Test
    void bracketSizingForVariousCounts() {
        record Case(int n, int bracket, int rounds, int round1Matches) {}
        List<Case> cases = List.of(
                new Case(3, 4, 2, 2),
                new Case(4, 4, 2, 2),
                new Case(5, 8, 3, 4),
                new Case(8, 8, 3, 4),
                new Case(11, 16, 4, 8),
                new Case(16, 16, 4, 8));

        for (Case c : cases) {
            Tournament t = new Tournament();
            List<Fixture> fixtures = BracketGenerator.build(t, mockParticipants(c.n), new Random(7));

            assertEquals(c.bracket - 1, fixtures.size(),
                    "Total fixtures = bracketSize - 1 for n=" + c.n);
            assertEquals(c.round1Matches, countByRound(fixtures, 1),
                    "Round 1 matches for n=" + c.n);
            assertEquals(1, countByRound(fixtures, c.rounds),
                    "Final round has one fixture for n=" + c.n);
        }
    }

    @Test
    void firstRoundContainsEveryParticipantOnce() {
        Tournament t = new Tournament();
        List<Participant> ps = mockParticipants(11);
        List<Fixture> fixtures = BracketGenerator.build(t, ps, new Random(123));

        List<Long> participantIds = new ArrayList<>();
        for (Fixture f : fixtures) {
            if (f.getRound() != 1) continue;
            if (f.getParticipant1() != null) participantIds.add(f.getParticipant1().getId());
            if (f.getParticipant2() != null) participantIds.add(f.getParticipant2().getId());
        }
        assertEquals(ps.size(), participantIds.size());
        assertEquals(ps.size(), participantIds.stream().distinct().count(),
                "Each accepted participant appears exactly once in round 1");
    }

    @Test
    void byeAutoAdvancesParticipant() {
        Tournament t = new Tournament();
        List<Participant> ps = mockParticipants(3); // 4-slot bracket, one bye in round 1
        List<Fixture> fixtures = BracketGenerator.build(t, ps, new Random(0));

        long firstRoundWinners = fixtures.stream()
                .filter(f -> f.getRound() == 1 && f.getWinner() != null)
                .count();
        assertEquals(1, firstRoundWinners,
                "The single bye fixture should have an auto-advanced winner");

        Fixture finalFx = fixtures.stream()
                .filter(f -> f.getRound() == 2)
                .findFirst().orElseThrow();
        assertTrue(finalFx.getParticipant1() != null || finalFx.getParticipant2() != null,
                "Final must have the auto-advanced participant set in one slot");
    }

    @Test
    void nextFixtureLinksAreSet() {
        Tournament t = new Tournament();
        List<Fixture> fixtures = BracketGenerator.build(t, mockParticipants(8), new Random(0));
        int rounds = fixtures.stream().mapToInt(Fixture::getRound).max().orElseThrow();
        for (Fixture f : fixtures) {
            if (f.getRound() == rounds) {
                assertNull(f.getNextFixture(), "Final has no next fixture");
            } else {
                assertNotNull(f.getNextFixture(),
                        "Non-final fixtures must link to a next fixture");
                assertEquals(f.getRound() + 1, f.getNextFixture().getRound());
            }
        }
    }
}
