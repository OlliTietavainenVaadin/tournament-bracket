package com.example.specdriven.bracket;

public enum RegistrationStatus {
    PENDING,
    ACCEPTED,
    DECLINED;

    public String displayLabel() {
        return switch (this) {
            case PENDING -> "Pending";
            case ACCEPTED -> "Accepted";
            case DECLINED -> "Declined";
        };
    }
}
