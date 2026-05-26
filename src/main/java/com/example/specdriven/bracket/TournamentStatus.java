package com.example.specdriven.bracket;

public enum TournamentStatus {
    CREATED,
    OPEN_FOR_REGISTRATIONS,
    ONGOING,
    FINISHED;

    public String displayLabel() {
        return switch (this) {
            case CREATED -> "Draft";
            case OPEN_FOR_REGISTRATIONS -> "Open";
            case ONGOING -> "Ongoing";
            case FINISHED -> "Finished";
        };
    }
}
