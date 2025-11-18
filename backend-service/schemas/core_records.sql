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