USE primary_db;

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
    password_hash VARCHAR(64) NOT NULL, -- SHA256 hashing cryptography shiii
    user_type ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    person_id INT NOT NULL, -- References either patient_id or doctor_id
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint based on user_type
    CONSTRAINT fk_users_patient FOREIGN KEY (person_id) 
        REFERENCES patient_records(patient_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    CONSTRAINT fk_users_doctor FOREIGN KEY (person_id) 
        REFERENCES doctor_records(doctor_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_users_email (email),
    INDEX idx_users_type_person (user_type, person_id),
    INDEX idx_users_active (is_active)
);

-- User sessions for tracking active sessions
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

-- Password reset tokens
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

-- Login attempt tracking for security
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