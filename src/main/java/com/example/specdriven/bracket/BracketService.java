package com.example.specdriven.bracket;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class BracketService {

    private final TournamentRepository tournamentRepository;
    private final FixtureRepository fixtureRepository;

    public BracketService(TournamentRepository tournamentRepository,
                          FixtureRepository fixtureRepository) {
        this.tournamentRepository = tournamentRepository;
        this.fixtureRepository = fixtureRepository;
    }

    public Tournament getActiveTournament() {
        return tournamentRepository.findAll().stream().findFirst().orElse(null);
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public List<Fixture> getFixtures(Tournament tournament) {
        return fixtureRepository.findByTournamentOrderByRoundAscPositionAsc(tournament);
    }

    public Map<Integer, List<Fixture>> getFixturesByRound(Tournament tournament) {
        return getFixtures(tournament).stream()
                .collect(Collectors.groupingBy(Fixture::getRound));
    }

    public int getTotalRounds(Tournament tournament) {
        return getFixtures(tournament).stream()
                .mapToInt(Fixture::getRound)
                .max()
                .orElse(0);
    }
}
