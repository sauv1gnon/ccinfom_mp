package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.AppointmentDAO;
import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.BranchWithDoctors;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.utils.AvailabilityParser;
import com.ccinfoms17grp2.utils.LocationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnhancedBranchSearchService {

    private final BranchDAO branchDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final RoutingService routingService;

    public EnhancedBranchSearchService(BranchDAO branchDAO, DoctorDAO doctorDAO, 
                                       AppointmentDAO appointmentDAO, RoutingService routingService) {
        this.branchDAO = Objects.requireNonNull(branchDAO);
        this.doctorDAO = Objects.requireNonNull(doctorDAO);
        this.appointmentDAO = Objects.requireNonNull(appointmentDAO);
        this.routingService = Objects.requireNonNull(routingService);
    }

    public List<BranchWithDoctors> searchBranches(double patientLat, double patientLon, 
                                                   int specializationId, 
                                                   LocalDateTime preferredSchedule,
                                                   int maxBranches) {
        System.out.println("[EnhancedBranchSearch] Starting search: specializationId=" + specializationId + 
            ", schedule=" + preferredSchedule + ", maxBranches=" + maxBranches);
        
        List<Branch> allBranches = branchDAO.findAll();
        System.out.println("[EnhancedBranchSearch] Found " + allBranches.size() + " branches total");
        
        List<BranchWithDoctors> candidateBranches = new ArrayList<>();

        for (Branch branch : allBranches) {
            try {
                System.out.println("[EnhancedBranchSearch] Processing branch: " + branch.getBranchName() + 
                    " (id=" + branch.getBranchId() + ")");
                
                if (branch.getLatitude() == null || branch.getLongitude() == null) {
                    System.out.println("[EnhancedBranchSearch] Skipping branch (no coordinates)");
                    continue;
                }

                double distance = LocationUtil.calculateDistance(
                    patientLat, patientLon, 
                    branch.getLatitude(), branch.getLongitude()
                );
                System.out.println("[EnhancedBranchSearch] Distance: " + distance + " km");

                System.out.println("[EnhancedBranchSearch] Querying doctors for branch " + branch.getBranchId() + 
                    ", specializationId=" + specializationId);
                
                List<Doctor> doctors = specializationId > 0 
                    ? doctorDAO.findByBranchAndSpecializations(branch.getBranchId(), 
                        java.util.Collections.singletonList(specializationId))
                    : doctorDAO.findByBranchId(branch.getBranchId());

                System.out.println("[EnhancedBranchSearch] Branch " + branch.getBranchName() + 
                    " (id=" + branch.getBranchId() + ") has " + doctors.size() + " doctors");

                if (doctors.isEmpty()) {
                    continue;
                }

                BranchWithDoctors branchWithDoctors = new BranchWithDoctors(branch, distance);

                for (Doctor doctor : doctors) {
                    BranchWithDoctors.AvailabilityColor color = determineAvailabilityColor(
                        doctor, specializationId, preferredSchedule
                    );
                    branchWithDoctors.addDoctor(new BranchWithDoctors.DoctorAvailabilityInfo(doctor, color));
                }

                if (!branchWithDoctors.getDoctors().isEmpty()) {
                    candidateBranches.add(branchWithDoctors);
                }
            } catch (Exception e) {
                System.err.println("[EnhancedBranchSearch] ERROR processing branch " + 
                    branch.getBranchName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("[EnhancedBranchSearch] Found " + candidateBranches.size() + " candidate branches with doctors");

        candidateBranches.sort(Comparator
            .comparingDouble(BranchWithDoctors::getDistanceKm)
            .thenComparing((b1, b2) -> {
                long green1 = b1.getDoctors().stream()
                    .filter(d -> d.getColor() == BranchWithDoctors.AvailabilityColor.GREEN)
                    .count();
                long green2 = b2.getDoctors().stream()
                    .filter(d -> d.getColor() == BranchWithDoctors.AvailabilityColor.GREEN)
                    .count();
                return Long.compare(green2, green1);
            }));

        List<BranchWithDoctors> results = candidateBranches.stream().limit(maxBranches).collect(Collectors.toList());
        System.out.println("[EnhancedBranchSearch] Returning " + results.size() + " branches");
        return results;
    }

    private BranchWithDoctors.AvailabilityColor determineAvailabilityColor(
            Doctor doctor, int specializationId, LocalDateTime preferredSchedule) {
        
        if (doctor.getAvailabilityStatus() != DoctorAvailabilityStatus.AVAILABLE) {
            return BranchWithDoctors.AvailabilityColor.RED;
        }

        boolean matchesSpecialization = specializationId <= 0 || 
            (doctor.getSpecializationIds() != null && 
             doctor.getSpecializationIds().contains(specializationId));

        if (preferredSchedule == null) {
            return matchesSpecialization 
                ? BranchWithDoctors.AvailabilityColor.GREEN 
                : BranchWithDoctors.AvailabilityColor.YELLOW;
        }

        boolean hasAppointmentAtTime = checkIfDoctorHasAppointment(doctor.getDoctorId(), preferredSchedule);
        if (hasAppointmentAtTime) {
            return BranchWithDoctors.AvailabilityColor.RED;
        }

        boolean matchesSchedule = checkIfDoctorAvailableAtTime(doctor, preferredSchedule);

        if (matchesSpecialization && matchesSchedule) {
            return BranchWithDoctors.AvailabilityColor.GREEN;
        } else if (matchesSpecialization || matchesSchedule) {
            return BranchWithDoctors.AvailabilityColor.YELLOW;
        } else {
            return BranchWithDoctors.AvailabilityColor.RED;
        }
    }

    private boolean checkIfDoctorHasAppointment(int doctorId, LocalDateTime dateTime) {
        try {
            LocalDateTime startWindow = dateTime.minusMinutes(30);
            LocalDateTime endWindow = dateTime.plusMinutes(30);
            
            List<Appointment> appointments = appointmentDAO.findByDoctorAndDateRange(
                doctorId, startWindow, endWindow
            );
            
            return !appointments.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkIfDoctorAvailableAtTime(Doctor doctor, LocalDateTime dateTime) {
        if (dateTime == null) {
            return true;
        }

        String availabilityJson = doctor.getAvailabilityDatetimeRanges();
        if (availabilityJson == null || availabilityJson.trim().isEmpty()) {
            return true;
        }

        return AvailabilityParser.isAvailableAt(availabilityJson, dateTime);
    }
}
