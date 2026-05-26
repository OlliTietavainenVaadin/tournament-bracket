package com.example.specdriven.bracket;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDate startDate;

    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.CREATED;

    @OneToOne
    private Participant winner;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fixture> fixtures = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public Participant getWinner() { return winner; }
    public void setWinner(Participant winner) { this.winner = winner; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public List<Fixture> getFixtures() { return fixtures; }
    public void setFixtures(List<Fixture> fixtures) { this.fixtures = fixtures; }

    public long acceptedParticipantCount() {
        return participants.stream()
                .filter(p -> p.getRegistrationStatus() == RegistrationStatus.ACCEPTED)
                .count();
    }

    public long pendingParticipantCount() {
        return participants.stream()
                .filter(p -> p.getRegistrationStatus() == RegistrationStatus.PENDING)
                .count();
    }
}
