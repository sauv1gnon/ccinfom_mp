CREATE DATABASE IF NOT EXISTS primary_db;
USE primary_db;
 |
-- Execute all schema files in order
SOURCE core_records.sql;
SOURCE auth_records.sql;
SOURCE transaction_records.sql;
 |
-- Display success message
SELECT 'Database and tables created successfully!' AS status;