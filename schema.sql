CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(256) NOT NULL,
    authToken VARCHAR(128),
    expiry INTEGER DEFAULT 0,
    createdAt INTEGER
);

CREATE TABLE messages (
    msgId INTEGER PRIMARY KEY,
    userId INTEGER,
    text VARCHAR(1024),
    createdAt INTEGER
);

CREATE INDEX idx_users_username ON users (username);