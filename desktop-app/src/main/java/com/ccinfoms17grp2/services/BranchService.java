package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.models.Branch;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BranchService {

    private final BranchDAO branchDAO;

    public BranchService(BranchDAO branchDAO) {
        this.branchDAO = Objects.requireNonNull(branchDAO);
    }

    public List<Branch> listBranches() {
        return branchDAO.findAll();
    }

    public Optional<Branch> getBranchById(int branchId) {
        if (branchId <= 0) {
            return Optional.empty();
        }
        return branchDAO.findById(branchId);
    }

    public Branch createBranch(Branch branch) {
        validate(branch, false);
        if (branchDAO.existsByName(branch.getBranchName())) {
            throw new ValidationException("A branch with that name already exists.");
        }
        return branchDAO.create(branch);
    }

    public Branch updateBranch(Branch branch) {
        validate(branch, true);
        Optional<Branch> existing = branchDAO.findById(branch.getBranchId());
        if (existing.isEmpty()) {
            throw new ValidationException("Branch could not be found.");
        }
        if (!existing.get().getBranchName().equalsIgnoreCase(branch.getBranchName()) && branchDAO.existsByName(branch.getBranchName())) {
            throw new ValidationException("Another branch already uses that name.");
        }
        boolean updated = branchDAO.update(branch);
        if (!updated) {
            throw new ValidationException("Branch record could not be updated.");
        }
        return branchDAO.findById(branch.getBranchId()).orElse(branch);
    }

    public void deleteBranch(int branchId) {
        boolean deleted = branchDAO.delete(branchId);
        if (!deleted) {
            throw new ValidationException("Branch could not be deleted. Ensure no doctors or appointments are linked.");
        }
    }

    private void validate(Branch branch, boolean requireId) {
        if (branch == null) {
            throw new ValidationException("Branch information is required.");
        }
        if (requireId && branch.getBranchId() <= 0) {
            throw new ValidationException("Branch ID is invalid.");
        }
        if (branch.getBranchName() == null || branch.getBranchName().trim().isEmpty()) {
            throw new ValidationException("Branch name is required.");
        }
        if (branch.getAddress() == null || branch.getAddress().trim().isEmpty()) {
            throw new ValidationException("Branch address is required.");
        }
        if (branch.getCapacity() <= 0) {
            throw new ValidationException("Capacity must be greater than zero.");
        }
        branch.setBranchName(branch.getBranchName().trim());
        branch.setAddress(branch.getAddress().trim());
    }
}
