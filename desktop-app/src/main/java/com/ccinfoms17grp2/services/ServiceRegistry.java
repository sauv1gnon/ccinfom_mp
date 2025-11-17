package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.AppointmentDAO;
import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.ConsultationDAO;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.dao.PatientDAO;
import com.ccinfoms17grp2.dao.QueueDAO;
import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.dao.UserDAO;
import com.ccinfoms17grp2.dao.impl.AppointmentJdbcDao;
import com.ccinfoms17grp2.dao.impl.BranchJdbcDao;
import com.ccinfoms17grp2.dao.impl.ConsultationJdbcDao;
import com.ccinfoms17grp2.dao.impl.DoctorJdbcDao;
import com.ccinfoms17grp2.dao.impl.PatientJdbcDao;
import com.ccinfoms17grp2.dao.impl.QueueJdbcDao;
import com.ccinfoms17grp2.dao.impl.SpecializationJdbcDao;
import com.ccinfoms17grp2.dao.impl.UserJdbcDao;

public class ServiceRegistry {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SpecializationService specializationService;
    private final BranchService branchService;
    private final UserService userService;
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final QueueService queueService;

    public ServiceRegistry() {
        this(new PatientJdbcDao(), new DoctorJdbcDao(), new SpecializationJdbcDao(),
             new BranchJdbcDao(), new UserJdbcDao(), new AppointmentJdbcDao(), new ConsultationJdbcDao(),
             new QueueJdbcDao());
    }

    public ServiceRegistry(PatientDAO patientDAO, DoctorDAO doctorDAO,
                          SpecializationDAO specializationDAO, BranchDAO branchDAO,
                          UserDAO userDAO, AppointmentDAO appointmentDAO, ConsultationDAO consultationDAO,
                          QueueDAO queueDAO) {
        SpecializationService specializationService = new SpecializationService(specializationDAO);
        this.specializationService = specializationService;
        this.patientService = new PatientService(patientDAO);
        this.branchService = new BranchService(branchDAO);
        this.doctorService = new DoctorService(doctorDAO, specializationDAO);
        this.userService = new UserService(userDAO);
        this.authService = new AuthService(userDAO);
        this.appointmentService = new AppointmentService(appointmentDAO);
        this.consultationService = new ConsultationService(consultationDAO);
        this.queueService = new QueueService(queueDAO);
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

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public ConsultationService getConsultationService() {
        return consultationService;
    }

    public QueueService getQueueService() {
        return queueService;
    }
}
