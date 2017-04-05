DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS user_flag;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS projects;
DROP SEQUENCE IF EXISTS user_seq;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');

CREATE SEQUENCE user_seq START 100000;

CREATE TABLE users (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT NOT NULL,
  email     TEXT NOT NULL,
  flag      user_flag NOT NULL
);

CREATE UNIQUE INDEX email_idx ON users (email);

CREATE TABLE cities (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  name TEXT NOT NULL
);

CREATE UNIQUE INDEX city_idx ON cities (name);

CREATE TABLE projects (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  description TEXT NOT NULL
);

CREATE UNIQUE INDEX project_idx ON projects (description);

CREATE TABLE groups (
  project_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  type TEXT NOT NULL,
  FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);