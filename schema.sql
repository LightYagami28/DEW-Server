CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(512) NOT NULL,
    token VARCHAR(128),
    expiry INTEGER DEFAULT 0
);

CREATE TABLE messages (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,

);