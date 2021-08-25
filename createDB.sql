-- Database: restdb
DROP DATABASE IF EXISTS restdb;
CREATE DATABASE restdb
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE ROLE restuser WITH
  LOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  PASSWORD 'restuser';
  
GRANT ALL ON DATABASE restdb TO restuser;

-- *** RECONNECT TO RESTDB ***
\c restdb restuser

-- Schema: apiserver
DROP SCHEMA IF EXISTS apiserver;
CREATE SCHEMA apiserver AUTHORIZATION restuser;

-- Table: apiserver.users
DROP TABLE IF EXISTS apiserver.users;
CREATE TABLE IF NOT EXISTS apiserver.users
(
    id bigserial primary key,
    login character varying(20) NOT NULL unique,
    token character varying(60) NOT NULL,
    balance integer DEFAULT 0
);


