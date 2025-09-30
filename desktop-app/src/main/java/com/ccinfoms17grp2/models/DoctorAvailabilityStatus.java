package com.ccinfoms17grp2.models;

public enum DoctorAvailabilityStatus {
    AVAILABLE,
    BUSY,
    OFF_DUTY;

    public static DoctorAvailabilityStatus fromDatabaseValue(String value) {
        if (value == null) {
            return AVAILABLE;
        }
        final String normalized = value.trim().toUpperCase();
        switch (normalized) {
            case "AVAILABLE":
                return AVAILABLE;
            case "BUSY":
                return BUSY;
            case "OFF DUTY":
            case "OFF_DUTY":
            case "OFFDUTY":
                return OFF_DUTY;
            default:
                throw new IllegalArgumentException("Unknown availability status: " + value);
        }
    }

    public String toDatabaseValue() {
        switch (this) {
            case AVAILABLE:
                return "Available";
            case BUSY:
                return "Busy";
            case OFF_DUTY:
                return "Off Duty";
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
