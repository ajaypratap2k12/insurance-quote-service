-- Schema initialization for Insurance Quote System
-- This script runs when the PostgreSQL container starts
-- Temporal will create its own schemas (temporal, temporal_visibility) automatically

-- Connect to insurance_db
\c insurance_db;

-- Create application schema for business data
CREATE SCHEMA IF NOT EXISTS insurance;

-- Grant privileges to postgres user
GRANT ALL ON SCHEMA insurance TO postgres;
GRANT ALL ON SCHEMA public TO postgres;

-- Grant usage on all schemas
GRANT USAGE ON SCHEMA insurance TO postgres;
GRANT USAGE ON SCHEMA public TO postgres;

-- Grant create privileges
GRANT CREATE ON SCHEMA insurance TO postgres;
GRANT CREATE ON SCHEMA public TO postgres;

-- Set default search path for the database
ALTER DATABASE insurance_db SET search_path TO insurance, public;
