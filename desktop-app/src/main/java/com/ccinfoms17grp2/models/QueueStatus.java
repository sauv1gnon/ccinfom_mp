package com.ccinfoms17grp2.models;

public enum QueueStatus {
    WAITING("waiting"),
    CALLED("called"),
    SERVED("served"),
    NO_SHOW("no_show");

    private final String value;

    QueueStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QueueStatus fromValue(String value) {
        for (QueueStatus status : QueueStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown QueueStatus: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
