package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.dao.PatientDAO;
import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.dao.UserDAO;
import com.ccinfoms17grp2.dao.impl.BranchJdbcDao;
import com.ccinfoms17grp2.dao.impl.DoctorJdbcDao;
import com.ccinfoms17grp2.dao.impl.PatientJdbcDao;
import com.ccinfoms17grp2.dao.impl.SpecializationJdbcDao;
import com.ccinfoms17grp2.dao.impl.UserJdbcDao;

public class ServiceRegistry {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SpecializationService specializationService;
    private final BranchService branchService;
    private final UserService userService;
    private final AuthService authService;

    public ServiceRegistry() {
        this(new PatientJdbcDao(), new DoctorJdbcDao(), new SpecializationJdbcDao(),
             new BranchJdbcDao(), new UserJdbcDao());
    }

    public ServiceRegistry(PatientDAO patientDAO, DoctorDAO doctorDAO,
                          SpecializationDAO specializationDAO, BranchDAO branchDAO,
                          UserDAO userDAO) {
        SpecializationService specializationService = new SpecializationService(specializationDAO);
        this.specializationService = specializationService;
        this.patientService = new PatientService(patientDAO);
        this.branchService = new BranchService(branchDAO);
        this.doctorService = new DoctorService(doctorDAO, specializationDAO);
        this.userService = new UserService(userDAO);
        this.authService = new AuthService(userDAO);
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

    public UserService getUserService() {
        return userService;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
