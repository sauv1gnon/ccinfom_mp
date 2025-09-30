package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Branch {

    private int branchId;
    private String branchName;
    private String address;
    private int capacity;
    private LocalDateTime createdAt;

    public Branch() {
    }

    public Branch(int branchId, String branchName, String address, int capacity, LocalDateTime createdAt) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Branch)) {
            return false;
        }
        Branch branch = (Branch) o;
        return branchId == branch.branchId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "branchId=" + branchId +
                ", branchName='" + branchName + '\'' +
                ", address='" + address + '\'' +
                ", capacity=" + capacity +
                ", createdAt=" + createdAt +
                '}';
    }
}
