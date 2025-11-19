CREATE DATABASE IF NOT EXISTS primary_db;
USE primary_db;

CREATE TABLE IF NOT EXISTS patient_records (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(15),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctor_records (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255),
    specializations_list JSON,
    availability_status ENUM('available', 'unavailable') DEFAULT 'available',
    availability_datetime_ranges JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS specialization_records (
    specialization_id INT AUTO_INCREMENT PRIMARY KEY,
    specialization_name VARCHAR(100) NOT NULL,
    specialization_code VARCHAR(50) UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS branch_records (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    capacity INT,
    contact_number VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS doctor_branch_assignment (
    doctor_id INT NOT NULL,
    branch_id INT NOT NULL,
    PRIMARY KEY (doctor_id, branch_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_records(doctor_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branch_records(branch_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointment_records (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    branch_id INT NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    status ENUM('scheduled', 'completed', 'canceled', 'no_show') DEFAULT 'scheduled',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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

CREATE TABLE IF NOT EXISTS user_roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO user_roles (role_name, description) VALUES 
('PATIENT', 'Patient role for booking appointments and managing records'),
('DOCTOR', 'Doctor role for managing appointments and consultations'),
('ADMIN', 'Administrator role with full system access');

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    person_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_type_person (user_type, person_id),
    INDEX idx_users_active (is_active)
);

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(128) PRIMARY KEY,
    user_id INT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_active (is_active),
    INDEX idx_sessions_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_reset_tokens_user (user_id),
    INDEX idx_reset_tokens_hash (token_hash),
    INDEX idx_reset_tokens_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_attempts_email (email),
    INDEX idx_attempts_ip (ip_address),
    INDEX idx_attempts_time (attempted_at)
);

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    person_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_type_person (user_type, person_id),
    INDEX idx_users_active (is_active)
);

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(128) PRIMARY KEY,
    user_id INT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_active (is_active),
    INDEX idx_sessions_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_reset_tokens_user (user_id),
    INDEX idx_reset_tokens_hash (token_hash),
    INDEX idx_reset_tokens_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_attempts_email (email),
    INDEX idx_attempts_ip (ip_address),
    INDEX idx_attempts_time (attempted_at)
);

USE primary_db;

DELETE FROM cancelled_appointment_records;
DELETE FROM recommendation_records;
DELETE FROM consultation_records;
DELETE FROM appointment_records;
DELETE FROM queue_records;
DELETE FROM doctor_branch_assignment;
DELETE FROM doctor_records;
DELETE FROM patient_records;
DELETE FROM branch_records;
DELETE FROM specialization_records;
DELETE FROM users WHERE user_id > 0;

INSERT INTO specialization_records (specialization_id, specialization_name, specialization_code, description) VALUES
(1,'Cardiology','CARD','Heart and vascular care'),
(2,'Pediatrics','PED','Child health'),
(3,'General Practice','GP','Primary care and family medicine'),
(4,'Dermatology','DERM','Skin conditions'),
(5,'Orthopedics','ORTHO','Bone and joint care');

INSERT INTO branch_records (branch_id, branch_name, address, latitude, longitude, capacity, contact_number) VALUES
(1,'Makati Medical Hub','Ayala Avenue, Makati City',14.554729,121.024445,80,'+63-2-8888-8999'),
(2,'Quezon City Health Center','Commonwealth Avenue, Quezon City',14.676041,121.057625,60,'+63-2-8123-4567'),
(3,'Manila Bay Clinic','Roxas Boulevard, Manila',14.572326,120.982437,50,'+63-2-8555-1234'),
(4,'BGC Medical Plaza','Bonifacio Global City, Taguig',14.550566,121.047668,70,'+63-2-8777-9999'),
(5,'Pasig Wellness Center','Ortigas Avenue, Pasig City',14.581404,121.064760,55,'+63-2-8333-2222'),
(6,'Alabang Medical Tower','Alabang-Zapote Road, Muntinlupa',14.430881,121.039810,65,'+63-2-8444-5555');

INSERT INTO patient_records (patient_id,last_name,first_name,contact_number,email,password_hash,created_at,updated_at) VALUES
(1,'Cruz','Maria','+63-917-123-4567','maria.cruz@patient.test',SHA2('patient123',256),NOW(),NOW()),
(2,'Santos','Juan','+63-918-234-5678','juan.santos@patient.test',SHA2('patient123',256),NOW(),NOW());

INSERT INTO doctor_records (doctor_id,last_name,first_name,email,password_hash,specializations_list,availability_status,availability_datetime_ranges,created_at,updated_at) VALUES
(1,'Reyes','Elena','elena.reyes@doctor.test',SHA2('doctor123',256),JSON_ARRAY(1),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','08:00','end_time','17:00')
),NOW(),NOW()),
(2,'Garcia','Roberto','roberto.garcia@doctor.test',SHA2('doctor123',256),JSON_ARRAY(2),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','09:00','end_time','18:00')
),NOW(),NOW()),
(3,'Mendoza','Sofia','sofia.mendoza@doctor.test',SHA2('doctor123',256),JSON_ARRAY(3),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','07:00','end_time','16:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','07:00','end_time','16:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','07:00','end_time','16:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','07:00','end_time','16:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','07:00','end_time','16:00')
),NOW(),NOW()),
(4,'Dela Cruz','Antonio','antonio.delacruz@doctor.test',SHA2('doctor123',256),JSON_ARRAY(4),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','10:00','end_time','19:00')
),NOW(),NOW()),
(5,'Ramos','Patricia','patricia.ramos@doctor.test',SHA2('doctor123',256),JSON_ARRAY(1,3),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','08:00','end_time','17:00')
),NOW(),NOW()),
(6,'Aquino','Miguel','miguel.aquino@doctor.test',SHA2('doctor123',256),JSON_ARRAY(5),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','09:00','end_time','18:00')
),NOW(),NOW()),
(7,'Torres','Carmen','carmen.torres@doctor.test',SHA2('doctor123',256),JSON_ARRAY(2,3),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','08:00','end_time','17:00')
),NOW(),NOW()),
(8,'Bautista','Rafael','rafael.bautista@doctor.test',SHA2('doctor123',256),JSON_ARRAY(1),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','10:00','end_time','19:00')
),NOW(),NOW());

INSERT INTO doctor_branch_assignment (doctor_id, branch_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3), (2, 4),
(3, 2), (3, 5), (3, 6),
(4, 1), (4, 4),
(5, 3), (5, 5),
(6, 4), (6, 6),
(7, 2), (7, 3),
(8, 1), (8, 5), (8, 6);

INSERT IGNORE INTO user_roles (role_name, description) VALUES
('PATIENT','Patient role for booking appointments and managing records'),
('DOCTOR','Doctor role for managing appointments and consultations'),
('ADMIN','Administrator role with full system access');

INSERT INTO users (user_id,email,password_hash,user_type,person_id,is_active,created_at,updated_at) VALUES
(1,'admin@example.test',SHA2('admin123',256),'ADMIN',1,1,NOW(),NOW()),
(2,'maria.cruz@patient.test',SHA2('patient123',256),'PATIENT',1,1,NOW(),NOW()),
(3,'juan.santos@patient.test',SHA2('patient123',256),'PATIENT',2,1,NOW(),NOW()),
(4,'elena.reyes@doctor.test',SHA2('doctor123',256),'DOCTOR',1,1,NOW(),NOW()),
(5,'roberto.garcia@doctor.test',SHA2('doctor123',256),'DOCTOR',2,1,NOW(),NOW()),
(6,'sofia.mendoza@doctor.test',SHA2('doctor123',256),'DOCTOR',3,1,NOW(),NOW()),
(7,'antonio.delacruz@doctor.test',SHA2('doctor123',256),'DOCTOR',4,1,NOW(),NOW()),
(8,'patricia.ramos@doctor.test',SHA2('doctor123',256),'DOCTOR',5,1,NOW(),NOW()),
(9,'miguel.aquino@doctor.test',SHA2('doctor123',256),'DOCTOR',6,1,NOW(),NOW()),
(10,'carmen.torres@doctor.test',SHA2('doctor123',256),'DOCTOR',7,1,NOW(),NOW()),
(11,'rafael.bautista@doctor.test',SHA2('doctor123',256),'DOCTOR',8,1,NOW(),NOW());
