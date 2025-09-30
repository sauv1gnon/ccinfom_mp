package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.dao.PatientDAO;
import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.dao.impl.BranchJdbcDao;
import com.ccinfoms17grp2.dao.impl.DoctorJdbcDao;
import com.ccinfoms17grp2.dao.impl.PatientJdbcDao;
import com.ccinfoms17grp2.dao.impl.SpecializationJdbcDao;

public class ServiceRegistry {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SpecializationService specializationService;
    private final BranchService branchService;

    public ServiceRegistry() {
        this(new PatientJdbcDao(), new DoctorJdbcDao(), new SpecializationJdbcDao(), new BranchJdbcDao());
    }

    public ServiceRegistry(PatientDAO patientDAO, DoctorDAO doctorDAO, SpecializationDAO specializationDAO, BranchDAO branchDAO) {
        SpecializationService specializationService = new SpecializationService(specializationDAO);
        this.specializationService = specializationService;
        this.patientService = new PatientService(patientDAO);
        this.branchService = new BranchService(branchDAO);
        this.doctorService = new DoctorService(doctorDAO, specializationDAO);
    }

    public PatientService getPatientService() {
        return patientService;
    }

    public DoctorService getDoctorService() {
        return doctorService;
    }

    public SpecializationService getSpecializationService() {
        return specializationService;
    }

    public BranchService getBranchService() {
        return branchService;
    }
}
