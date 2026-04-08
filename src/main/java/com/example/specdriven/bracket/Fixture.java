package com.example.specdriven.bracket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate playDate;

    private int round;

    private int position;

    @ManyToOne
    private Tournament tournament;

    @ManyToOne
    private Participant participant1;

    @ManyToOne
    private Participant participant2;

    @ManyToOne
    private Participant winner;

    @ManyToOne
    private Fixture nextFixture;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getPlayDate() { return playDate; }
    public void setPlayDate(LocalDate playDate) { this.playDate = playDate; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Participant getParticipant1() { return participant1; }
    public void setParticipant1(Participant participant1) { this.participant1 = participant1; }

    public Participant getParticipant2() { return participant2; }
    public void setParticipant2(Participant participant2) { this.participant2 = participant2; }

    public Participant getWinner() { return winner; }
    public void setWinner(Participant winner) { this.winner = winner; }

    public Fixture getNextFixture() { return nextFixture; }
    public void setNextFixture(Fixture nextFixture) { this.nextFixture = nextFixture; }
}
