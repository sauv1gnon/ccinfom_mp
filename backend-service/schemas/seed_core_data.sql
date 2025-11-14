USE primary_db;

INSERT IGNORE INTO specialization_records (specialization_id, specialization_name, specialization_code, description) VALUES
(1,'Cardiology','CARD','Heart and vascular care'),
(2,'Pediatrics','PED','Child health'),
(3,'General Practice','GP','Primary care and family medicine'),
(4,'Dermatology','DERM','Skin conditions');

INSERT IGNORE INTO branch_records (branch_id, branch_name, address, latitude, longitude, capacity, contact_number) VALUES
(1,'Central Clinic','123 Main St, Cityville',40.712776,-74.005974,50,'+1-555-0100'),
(2,'Northside Health Center','456 North Ave, Cityville',40.730610,-73.935242,30,'+1-555-0101');

INSERT INTO patient_records (patient_id,last_name,first_name,contact_number,email,password_hash,created_at,updated_at) VALUES
(1001,'Admin','System','+1-555-0000','admin@example.test',SHA2('AdminPass123',256),NOW(),NOW()),
(1002,'The','Damselette','+1-555-0002','columbina@patient.test',SHA2('s4ndr0neWasHere',256),NOW(),NOW()),
(1003,'Focalor','Furina','+1-555-0003','furina.focalor@patient.test',SHA2('n3uv1llette',256),NOW(),NOW());

INSERT INTO doctor_records (doctor_id,last_name,first_name,email,password_hash,specializations_list,availability_status,availability_datetime_ranges,created_at,updated_at) VALUES
(2001,'House','Gregory','greg.house@doctor.test',SHA2('HousePass123',256),JSON_ARRAY('Cardiology'),'available',JSON_ARRAY(JSON_OBJECT('from','2025-11-17 09:00:00','to','2025-11-17 17:00:00')),NOW(),NOW()),
(2002,'Wilson','James','james.wilson@doctor.test',SHA2('WilsonPass123',256),JSON_ARRAY('Pediatrics','General Practice'),'available',JSON_ARRAY(JSON_OBJECT('from','2025-11-18 10:00:00','to','2025-11-18 16:00:00')),NOW(),NOW());

INSERT INTO patient_records (patient_id,last_name,first_name,contact_number,email,password_hash,created_at,updated_at) VALUES
(2001,'House','Gregory','+1-555-0004','greg.house@doctor.test',SHA2('HousePass123',256),NOW(),NOW()),
(2002,'Wilson','James','+1-555-0005','james.wilson@doctor.test',SHA2('WilsonPass123',256),NOW(),NOW());

INSERT INTO doctor_records (doctor_id,last_name,first_name,email,password_hash,specializations_list,availability_status,availability_datetime_ranges,created_at,updated_at) VALUES
(1001,'Admin','System','admin@example.test',SHA2('AdminPass123',256),JSON_ARRAY('General Practice'),'available',JSON_ARRAY(JSON_OBJECT('from','2025-11-17 09:00:00','to','2025-11-17 17:00:00')),NOW(),NOW()),
(1002,'The','Damselette','columbina@patient.test',SHA2('s4ndr0neWasHere',256),JSON_ARRAY('General Practice'),'available',JSON_ARRAY(JSON_OBJECT('from','2025-11-17 09:00:00','to','2025-11-17 17:00:00')),NOW(),NOW());

INSERT IGNORE INTO user_roles (role_name, description) VALUES
('PATIENT','Patient role for booking appointments and managing records'),
('DOCTOR','Doctor role for managing appointments and consultations'),
('ADMIN','Administrator role with full system access');

INSERT INTO users (user_id,email,password_hash,user_type,person_id,is_active,created_at,updated_at) VALUES
(1,'admin@example.test',SHA2('AdminPass123',256),'ADMIN',1001,1,NOW(),NOW()),
(2,'columbina@patient.test',SHA2('s4ndr0neWasHere',256),'PATIENT',1002,1,NOW(),NOW()),
(3,'greg.house@doctor.test',SHA2('HousePass123',256),'DOCTOR',2001,1,NOW(),NOW());
