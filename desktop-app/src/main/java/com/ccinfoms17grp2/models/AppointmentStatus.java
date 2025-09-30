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
        switch (value.trim().toUpperCase()) {
            case "SCHEDULED":
                return SCHEDULED;
            case "IN PROGRESS":
            case "IN_PROGRESS":
                return IN_PROGRESS;
            case "COMPLETED":
                return COMPLETED;
            case "CANCELLED":
                return CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown appointment status: " + value);
        }
    }

    public String toDatabaseValue() {
        switch (this) {
            case SCHEDULED:
                return "Scheduled";
            case IN_PROGRESS:
                return "In Progress";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
