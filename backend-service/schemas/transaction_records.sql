CREATE TABLE IF NOT EXISTS appointment_records (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    branch_id INT NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    status ENUM('scheduled', 'completed', 'canceled', 'no_show') DEFAULT 'scheduled',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patient_records(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_records(doctor_id),
    FOREIGN KEY (branch_id) REFERENCES branch_records(branch_id)
);

CREATE TABLE IF NOT EXISTS queue_records (
    queue_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    branch_id INT NOT NULL,
    queue_number INT NOT NULL,
    status ENUM('waiting', 'called', 'served', 'no_show') DEFAULT 'waiting',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient_records(patient_id),
    FOREIGN KEY (branch_id) REFERENCES branch_records(branch_id)
);

CREATE TABLE IF NOT EXISTS consultation_records (
    consultation_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    diagnosis TEXT,
    treatment_plan TEXT,
    prescription TEXT,
    follow_up_date DATETIME,
    FOREIGN KEY (appointment_id) REFERENCES appointment_records(appointment_id)
);

CREATE TABLE IF NOT EXISTS recommendation_records (
    recommendation_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    specialization_id INT NOT NULL,
    recommended_doctor_ids JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    FOREIGN KEY (patient_id) REFERENCES patient_records(patient_id),
    FOREIGN KEY (specialization_id) REFERENCES specialization_records(specialization_id)
);

CREATE TABLE IF NOT EXISTS cancelled_appointment_records (
    cancelled_appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL,
    canceled_by ENUM('patient', 'doctor', 'system') NOT NULL,
    cancellation_reason TEXT,
    canceled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointment_records(appointment_id)
);