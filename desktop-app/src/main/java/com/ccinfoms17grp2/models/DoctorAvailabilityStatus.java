package com.ccinfoms17grp2.models;

public enum DoctorAvailabilityStatus {
    AVAILABLE,
    BUSY,
    OFF_DUTY;

    public static DoctorAvailabilityStatus fromDatabaseValue(String value) {
        if (value == null) {
            return AVAILABLE;
        }
        final String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "available":
                return AVAILABLE;
            case "unavailable":
                return OFF_DUTY;
            default:
                throw new IllegalArgumentException("Unknown availability status: " + value);
        }
    }

    public String toDatabaseValue() {
        switch (this) {
            case AVAILABLE:
                return "available";
            case BUSY:
                return "unavailable";
            case OFF_DUTY:
                return "unavailable";
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
