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
(1,'Admin','System','+1-555-0000','admin@example.test',SHA2('AdminPass123',256),NOW(),NOW()),
(2,'The','Damselette','+1-555-0002','columbina@patient.test',SHA2('s4ndr0neWasHere',256),NOW(),NOW()),
(3,'Focalor','Furina','+1-555-0003','furina.focalor@patient.test',SHA2('n3uv1llette',256),NOW(),NOW());

INSERT INTO doctor_records (doctor_id,last_name,first_name,email,password_hash,specializations_list,availability_status,availability_datetime_ranges,created_at,updated_at) VALUES
(1,'Admin','System','admin@example.test',SHA2('AdminPass123',256),JSON_ARRAY(3),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','08:00','end_time','20:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','08:00','end_time','20:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','08:00','end_time','20:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','08:00','end_time','20:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','08:00','end_time','20:00')
),NOW(),NOW()),
(2,'House','Gregory','greg.house@doctor.test',SHA2('HousePass123',256),JSON_ARRAY(1),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Tuesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','08:00','end_time','17:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','08:00','end_time','17:00')
),NOW(),NOW()),
(3,'Wilson','James','james.wilson@doctor.test',SHA2('WilsonPass123',256),JSON_ARRAY(2,3),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Monday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Wednesday','start_time','09:00','end_time','18:00'),
    JSON_OBJECT('day_of_week','Friday','start_time','09:00','end_time','18:00')
),NOW(),NOW()),
(4,'The','Damselette','columbina@patient.test',SHA2('s4ndr0neWasHere',256),JSON_ARRAY(3,4),'available',JSON_ARRAY(
    JSON_OBJECT('day_of_week','Tuesday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Thursday','start_time','10:00','end_time','19:00'),
    JSON_OBJECT('day_of_week','Saturday','start_time','08:00','end_time','14:00')
),NOW(),NOW());

INSERT INTO doctor_branch_assignment (doctor_id, branch_id) VALUES
(1, 1),
(1, 2),
(2, 1),
(3, 2),
(4, 1);

INSERT IGNORE INTO user_roles (role_name, description) VALUES
('PATIENT','Patient role for booking appointments and managing records'),
('DOCTOR','Doctor role for managing appointments and consultations'),
('ADMIN','Administrator role with full system access');

INSERT INTO users (user_id,email,password_hash,user_type,person_id,is_active,created_at,updated_at) VALUES
(1,'admin@example.test',SHA2('AdminPass123',256),'ADMIN',1,1,NOW(),NOW()),
(2,'columbina@patient.test',SHA2('s4ndr0neWasHere',256),'PATIENT',2,1,NOW(),NOW()),
(3,'greg.house@doctor.test',SHA2('HousePass123',256),'DOCTOR',2,1,NOW(),NOW());

