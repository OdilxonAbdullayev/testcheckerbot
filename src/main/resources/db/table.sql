CREATE TABLE IF NOT EXISTS tariffs
(
    id            INT AUTO_INCREMENT primary key,
    name          VARCHAR(255) NOT NULL,
    student_count INT          NOT NULL,
    price         BIGINT       NOT NULL,
    ordering      BIGINT       NOT NULL,
    created_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS user_statuses
(
    id           INT AUTO_INCREMENT primary key,
    name         VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS users
(
    id               bigint primary key,
    step             VARCHAR(1000),
    balance          BIGINT    DEFAULT 0,
    tariff_id        INT NOT NULL,
    status_id        INT NOT NULL,
    tariff_date_from TIMESTAMP,
    tariff_date_to   TIMESTAMP,
    created_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    updated_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (tariff_id) REFERENCES tariffs (id),
    FOREIGN KEY (status_id) REFERENCES user_statuses (id)
);

CREATE TABLE IF NOT EXISTS school
(
    id           BIGINT AUTO_INCREMENT primary key,
    name         VARCHAR(250) NOT NULL,
    users_id     BIGINT       NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (users_id) REFERENCES users (id) ON delete CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS classes
(
    id           BIGINT AUTO_INCREMENT primary key,
    name         VARCHAR(250) NOT NULL,
    school_id    BIGINT       NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (school_id) REFERENCES school (id) ON delete CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS students
(
    id                 BIGINT AUTO_INCREMENT primary key,
    fio                VARCHAR(250) NOT NULL,
    login              VARCHAR(100) NOT NULL,
    password           VARCHAR(100) NOT NULL,
    class_id           BIGINT       NOT NULL,
    last_login_date    TIMESTAMP,
    last_response_msg  VARCHAR(1000),
    last_response_date TIMESTAMP,
    progressing        INT,
    updated_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (class_id) REFERENCES classes (id) ON delete CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS channel
(
    id               BIGINT primary key,
    created_users_id BIGINT,
    created_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (created_users_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS sender
(
    id              BIGINT(20) NOT NULL AUTO_INCREMENT,
    sendStatus      VARCHAR(50),
    startTime       VARCHAR(50),
    sendCount       BIGINT(30),
    sendLimitCount  BIGINT(30),
    sendUser        BIGINT(30),
    notSendUser     BIGINT(30),
    messageId       BIGINT(30),
    admin_id        BIGINT(30),
    admin_messageId BIGINT(30),
    PRIMARY KEY (id)
);

