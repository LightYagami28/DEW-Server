CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(256) NOT NULL,
    authToken VARCHAR(128),
    expiry INTEGER DEFAULT 0,
    createdAt INTEGER
);

CREATE TABLE chats (
    id INTEGER PRIMARY KEY,
    title VARCHAR(64),
    joinHash VARCHAR(32)
);

CREATE TABLE members (
    chatId INTEGER NOT NULL,
    userId INTEGER NOT NULL,
    roleId INTEGER NOT NULL
);

CREATE TABLE roles (
    id INTEGER NOT NULL,
    name VARCHAR(256) NOT NULL
)

CREATE TABLE chat_messages (
    id INTEGER,
    chatId INTEGER,
    msgId INTEGER
);

CREATE TABLE messages (
    id INTEGER PRIMARY KEY,
    userId INTEGER,
    text VARCHAR(1024),
    createdAt INTEGER
);

CREATE INDEX idx_users_username ON users (username);

INSERT INTO roles (id, name) VALUES (0, 'Founder');
INSERT INTO roles (id, name) VALUES (1, 'Administrator');
INSERT INTO roles (id, name) VALUES (2, 'Moderator');
INSERT INTO roles (id, name) VALUES (3, 'User');