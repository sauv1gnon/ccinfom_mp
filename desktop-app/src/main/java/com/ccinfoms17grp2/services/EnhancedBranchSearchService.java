package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.AppointmentDAO;
import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.BranchWithDoctors;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        List<Branch> allBranches = branchDAO.findAll();
        List<BranchWithDoctors> results = new ArrayList<>();

        for (Branch branch : allBranches) {
            if (branch.getLatitude() == null || branch.getLongitude() == null) {
                continue;
            }

            double distance;
            try {
                RoutingService.RouteResult route = routingService.calculateRoute(
                    patientLat, patientLon, 
                    branch.getLatitude(), branch.getLongitude()
                );
                distance = route != null ? route.getDistanceKm() : calculateStraightLineDistance(
                    patientLat, patientLon, branch.getLatitude(), branch.getLongitude()
                );
            } catch (Exception e) {
                distance = calculateStraightLineDistance(
                    patientLat, patientLon, branch.getLatitude(), branch.getLongitude()
                );
            }

            BranchWithDoctors branchWithDoctors = new BranchWithDoctors(branch, distance);
            
            List<Doctor> doctors = specializationId > 0 
                ? doctorDAO.findByBranchAndSpecializations(branch.getBranchId(), 
                    java.util.Collections.singletonList(specializationId))
                : doctorDAO.findByBranchId(branch.getBranchId());

            for (Doctor doctor : doctors) {
                BranchWithDoctors.AvailabilityColor color = determineAvailabilityColor(
                    doctor, specializationId, preferredSchedule
                );
                branchWithDoctors.addDoctor(new BranchWithDoctors.DoctorAvailabilityInfo(doctor, color));
            }

            if (!branchWithDoctors.getDoctors().isEmpty()) {
                results.add(branchWithDoctors);
            }
        }

        results.sort(Comparator.comparingDouble(BranchWithDoctors::getDistanceKm));

        return results.stream().limit(maxBranches).collect(Collectors.toList());
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
        LocalDateTime startWindow = dateTime.minusMinutes(30);
        LocalDateTime endWindow = dateTime.plusMinutes(30);
        
        List<Appointment> appointments = appointmentDAO.findByDoctorAndDateRange(
            doctorId, startWindow, endWindow
        );
        
        return !appointments.isEmpty();
    }

    private boolean checkIfDoctorAvailableAtTime(Doctor doctor, LocalDateTime dateTime) {
        return true;
    }

    private double calculateStraightLineDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
