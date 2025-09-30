package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.PatientDAO;
import com.ccinfoms17grp2.models.Patient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientService {

    private static final Pattern CONTACT_PATTERN = Pattern.compile("[+0-9\\- ]{7,15}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^$|^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final PatientDAO patientDAO;

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = Objects.requireNonNull(patientDAO);
    }

    public List<Patient> listPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listPatients();
        }
        return patientDAO.searchByName(keyword.trim());
    }

    public Patient createPatient(Patient patient) {
        validate(patient, false);
        return patientDAO.create(patient);
    }

    public Patient updatePatient(Patient patient) {
        validate(patient, true);
        boolean updated = patientDAO.update(patient);
        if (!updated) {
            throw new ValidationException("Patient record could not be updated. It may have been removed by another user.");
        }
        Optional<Patient> refreshed = patientDAO.findById(patient.getPatientId());
        return refreshed.orElse(patient);
    }

    public void deletePatient(int patientId) {
        boolean deleted = patientDAO.delete(patientId);
        if (!deleted) {
            throw new ValidationException("Patient record could not be deleted. It may have pending appointments.");
        }
    }

    private void validate(Patient patient, boolean requireId) {
        if (patient == null) {
            throw new ValidationException("Patient information is required.");
        }
        if (requireId && patient.getPatientId() <= 0) {
            throw new ValidationException("Patient ID is invalid.");
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.");
        }
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.");
        }
        if (patient.getContactNumber() == null || !CONTACT_PATTERN.matcher(patient.getContactNumber().trim()).matches()) {
            throw new ValidationException("Contact number must contain 7-15 digits and may include + or - characters.");
        }
        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty() && !EMAIL_PATTERN.matcher(patient.getEmail().trim()).matches()) {
            throw new ValidationException("Email address is not in a valid format.");
        }
    }
}
