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
