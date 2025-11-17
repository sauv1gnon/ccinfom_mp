package com.ccinfoms17grp2.models;

import java.util.Objects;

public class Specialization {

    private int specializationId;
    private String specializationName;
    private String specializationCode;
    private String description;

    public Specialization() {
    }

    public Specialization(int specializationId, String specializationName, String specializationCode, String description) {
        this.specializationId = specializationId;
        this.specializationName = specializationName;
        this.specializationCode = specializationCode;
        this.description = description;
    }

    public Specialization(int specializationId, String specializationName, String specializationCode) {
        this(specializationId, specializationName, specializationCode, null);
    }

    public int getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(int specializationId) {
        this.specializationId = specializationId;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
    }

    public String getSpecializationCode() {
        return specializationCode;
    }

    public void setSpecializationCode(String specializationCode) {
        this.specializationCode = specializationCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Specialization)) {
            return false;
        }
        Specialization that = (Specialization) o;
        return specializationId == that.specializationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(specializationId);
    }

    @Override
    public String toString() {
        return "Specialization{" +
                "specializationId=" + specializationId +
                ", specializationName='" + specializationName + '\'' +
                ", specializationCode='" + specializationCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
