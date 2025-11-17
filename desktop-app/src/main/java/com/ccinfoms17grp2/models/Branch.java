package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Branch {

    private int branchId;
    private String branchName;
    private String address;
    private Double latitude;
    private Double longitude;
    private int capacity;
    private String contactNumber;
    private LocalDateTime createdAt;

    public Branch() {
    }

    public Branch(int branchId, String branchName, String address, Double latitude, Double longitude, 
                  int capacity, String contactNumber, LocalDateTime createdAt) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.contactNumber = contactNumber;
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

    public String getName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
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
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", capacity=" + capacity +
                ", contactNumber='" + contactNumber + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
