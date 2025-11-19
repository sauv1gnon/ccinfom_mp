package com.ccinfoms17grp2.models;

public enum AppointmentStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static AppointmentStatus fromDatabaseValue(String value) {
        if (value == null) {
            return SCHEDULED;
        }
        switch (value.trim().toLowerCase()) {
            case "scheduled":
                return SCHEDULED;
            case "in progress":
            case "in_progress":
                return IN_PROGRESS;
            case "completed":
                return COMPLETED;
            case "canceled":
            case "cancelled":
                return CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown appointment status: " + value);
        }
    }

    public String toDatabaseValue() {
        switch (this) {
            case SCHEDULED:
                return "scheduled";
            case IN_PROGRESS:
                return "in_progress";
            case COMPLETED:
                return "completed";
            case CANCELLED:
                return "canceled";
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
