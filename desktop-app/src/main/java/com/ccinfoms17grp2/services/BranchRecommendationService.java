package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.utils.LocationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service for recommending branches based on patient location and doctor
 * availability.
 */
public class BranchRecommendationService {

    private final BranchDAO branchDAO;
    private final DoctorDAO doctorDAO;

    public BranchRecommendationService(BranchDAO branchDAO, DoctorDAO doctorDAO) {
        this.branchDAO = Objects.requireNonNull(branchDAO);
        this.doctorDAO = Objects.requireNonNull(doctorDAO);
    }

    /**
     * Represents a branch recommendation with distance and doctor count.
     */
    public static class BranchRecommendation {

        private final Branch branch;
        private final double distanceKm;
        private final int availableDoctorCount;

        public BranchRecommendation(Branch branch, double distanceKm, int availableDoctorCount) {
            this.branch = branch;
            this.distanceKm = distanceKm;
            this.availableDoctorCount = availableDoctorCount;
        }

        public Branch getBranch() {
            return branch;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public int getAvailableDoctorCount() {
            return availableDoctorCount;
        }

        @Override
        public String toString() {
            return String.format("%s - %.2f km away, %d doctors available",
                    branch.getBranchName(), distanceKm, availableDoctorCount);
        }
    }

    /**
     * Recommend branches based on patient location and required
     * specializations. Sorts by distance first, then by doctor count as
     * tiebreaker.
     *
     * @param patientLatitude Patient's latitude
     * @param patientLongitude Patient's longitude
     * @param specializationIds List of specialization IDs patient needs
     * @return List of branch recommendations sorted by distance
     */
    public List<BranchRecommendation> recommendBranches(double patientLatitude, double patientLongitude,
            List<Integer> specializationIds) {
        if (!LocationUtil.isValidLatitude(patientLatitude) || !LocationUtil.isValidLongitude(patientLongitude)) {
            throw new ValidationException("Invalid patient location coordinates");
        }

        List<Branch> allBranches = branchDAO.findAll();
        List<BranchRecommendation> recommendations = new ArrayList<>();

        for (Branch branch : allBranches) {
            if (branch.getLatitude() == null || branch.getLongitude() == null) {
                continue;
            }

            double distance = LocationUtil.calculateDistance(
                    patientLatitude, patientLongitude,
                    branch.getLatitude(), branch.getLongitude()
            );

            int doctorCount = countAvailableDoctors(branch.getBranchId(), specializationIds);

            if (doctorCount > 0) {
                recommendations.add(new BranchRecommendation(branch, distance, doctorCount));
            }
        }

        recommendations.sort(Comparator.comparingDouble(BranchRecommendation::getDistanceKm)
                .thenComparingInt(BranchRecommendation::getAvailableDoctorCount).reversed());

        return recommendations;
    }

    /**
     * Count available doctors at a branch with specific specializations.
     *
     * @param branchId Branch ID
     * @param specializationIds List of specialization IDs to match (ANY match
     * counts)
     * @return Number of available doctors
     */
    private int countAvailableDoctors(int branchId, List<Integer> specializationIds) {
        try {
            List<Doctor> doctors;
            if (specializationIds == null || specializationIds.isEmpty()) {
                doctors = doctorDAO.findByBranchId(branchId);
            } else {
                doctors = doctorDAO.findByBranchAndSpecializations(branchId, specializationIds);
            }

            return (int) doctors.stream()
                    .filter(d -> d.getAvailabilityStatus() == DoctorAvailabilityStatus.AVAILABLE)
                    .count();
        } catch (Exception e) {
            System.err.println("Error counting doctors at branch " + branchId + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get available doctors at a specific branch with specific specializations.
     *
     * @param branchId Branch ID
     * @param specializationIds List of specialization IDs to match
     * @return List of available doctors
     */
    public List<Doctor> getAvailableDoctorsAtBranch(int branchId, List<Integer> specializationIds) {
        List<Doctor> doctors;
        if (specializationIds == null || specializationIds.isEmpty()) {
            doctors = doctorDAO.findByBranchId(branchId);
        } else {
            doctors = doctorDAO.findByBranchAndSpecializations(branchId, specializationIds);
        }

        List<Doctor> availableDoctors = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if (doctor.getAvailabilityStatus() == DoctorAvailabilityStatus.AVAILABLE) {
                availableDoctors.add(doctor);
            }
        }
        return availableDoctors;
    }
}
