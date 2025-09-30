package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.models.Specialization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SpecializationService {

    private final SpecializationDAO specializationDAO;

    public SpecializationService(SpecializationDAO specializationDAO) {
        this.specializationDAO = Objects.requireNonNull(specializationDAO);
    }

    public List<Specialization> listSpecializations() {
        return specializationDAO.findAll();
    }

    public Specialization createSpecialization(Specialization specialization) {
        validate(specialization, false);
        if (specializationDAO.existsByCode(specialization.getSpecializationCode())) {
            throw new ValidationException("Specialization code is already in use.");
        }
        return specializationDAO.create(specialization);
    }

    public Specialization updateSpecialization(Specialization specialization) {
        validate(specialization, true);
        Optional<Specialization> existing = specializationDAO.findById(specialization.getSpecializationId());
        if (existing.isEmpty()) {
            throw new ValidationException("Specialization could not be found.");
        }
        Specialization current = existing.get();
        if (!current.getSpecializationCode().equalsIgnoreCase(specialization.getSpecializationCode())
                && specializationDAO.existsByCode(specialization.getSpecializationCode())) {
            throw new ValidationException("Another specialization already uses that code.");
        }
        boolean updated = specializationDAO.update(specialization);
        if (!updated) {
            throw new ValidationException("Specialization record could not be updated.");
        }
        return specializationDAO.findById(specialization.getSpecializationId()).orElse(specialization);
    }

    public void deleteSpecialization(int id) {
        boolean deleted = specializationDAO.delete(id);
        if (!deleted) {
            throw new ValidationException("Specialization could not be deleted. Remove linked doctors first.");
        }
    }

    private void validate(Specialization specialization, boolean requireId) {
        if (specialization == null) {
            throw new ValidationException("Specialization information is required.");
        }
        if (requireId && specialization.getSpecializationId() <= 0) {
            throw new ValidationException("Specialization ID is invalid.");
        }
        if (specialization.getSpecializationName() == null || specialization.getSpecializationName().trim().isEmpty()) {
            throw new ValidationException("Specialization name is required.");
        }
        if (specialization.getSpecializationCode() == null || specialization.getSpecializationCode().trim().isEmpty()) {
            throw new ValidationException("Specialization code is required.");
        }
        specialization.setSpecializationCode(specialization.getSpecializationCode().trim().toUpperCase());
        specialization.setSpecializationName(specialization.getSpecializationName().trim());
    }
}
