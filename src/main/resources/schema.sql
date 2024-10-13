CREATE TABLE IF NOT EXISTS USERS (
    USERNAME VARCHAR(50) NOT NULL PRIMARY KEY,
    PASSWORD VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS USER_ROLE (
    USERNAME VARCHAR(50) NOT NULL,
    ROLENAME VARCHAR(50) NOT NULL,
    PRIMARY KEY (USERNAME, ROLENAME)
);