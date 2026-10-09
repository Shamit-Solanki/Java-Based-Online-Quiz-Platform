-- =====================================================================
--  QuizMania - database schema (MySQL 8+)
--  Run this first, then database/sample-data.sql
-- =====================================================================
CREATE DATABASE IF NOT EXISTS online_quiz
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_quiz;

-- Drop in dependency order so the script can be re-run safely
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS quiz_attempts;
DROP TABLE IF EXISTS reminders;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS quizzes;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------
-- Users: one table for all three roles
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,              -- PBKDF2 hash, never plain text
    role          ENUM('ADMIN','CREATOR','PARTICIPANT') NOT NULL,
    active        TINYINT(1) NOT NULL DEFAULT 1,      -- admins can disable accounts
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Quizzes and questions
-- status flow: DRAFT -> PENDING -> APPROVED | REJECTED
-- ---------------------------------------------------------------------
CREATE TABLE quizzes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    description      VARCHAR(500),
    duration_minutes INT NOT NULL,
    creator_id       INT NOT NULL,
    status           ENUM('DRAFT','PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'DRAFT',
    review_note      VARCHAR(500),                    -- admin's reason when rejecting
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_quiz_status (status)
) ENGINE=InnoDB;

CREATE TABLE questions (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id        INT NOT NULL,
    question_text  VARCHAR(500) NOT NULL,
    option_a       VARCHAR(255) NOT NULL,
    option_b       VARCHAR(255) NOT NULL,
    option_c       VARCHAR(255) NOT NULL,
    option_d       VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    explanation    VARCHAR(500),                      -- shown in the performance report
    CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Attempts and answers
-- started_at is used for the server-enforced timer.
-- score = auto_score unless the creator adjusts it while reviewing.
-- ---------------------------------------------------------------------
CREATE TABLE quiz_attempts (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id        INT NOT NULL,
    participant_id INT NOT NULL,
    score          INT NOT NULL DEFAULT 0,
    auto_score     INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL,
    started_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at   DATETIME NULL,
    feedback       VARCHAR(1000) NULL,
    graded_by      INT NULL,
    graded_at      DATETIME NULL,
    CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_user FOREIGN KEY (participant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_grader FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_attempt_lookup (participant_id, quiz_id)
) ENGINE=InnoDB;

CREATE TABLE answers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id      INT NOT NULL,
    question_id     INT NOT NULL,
    selected_option CHAR(1) NULL,
    is_correct      TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uq_answer (attempt_id, question_id),
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Interaction: messages (creator <-> participant), notifications, reminders
-- ---------------------------------------------------------------------
CREATE TABLE messages (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sender_id    INT NOT NULL,
    receiver_id  INT NOT NULL,
    message_text VARCHAR(1000) NOT NULL,
    is_read      TINYINT(1) NOT NULL DEFAULT 0,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_msg_pair (sender_id, receiver_id)
) ENGINE=InnoDB;

-- For admins these rows are the "System Alerts" panel
CREATE TABLE notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    level      ENUM('INFO','SUCCESS','WARNING','CRITICAL') NOT NULL DEFAULT 'INFO',
    message    VARCHAR(500) NOT NULL,
    link       VARCHAR(200) NULL,
    is_read    TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id, is_read)
) ENGINE=InnoDB;

CREATE TABLE reminders (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    participant_id INT NOT NULL,
    quiz_id        INT NOT NULL,
    remind_at      DATETIME NOT NULL,
    note           VARCHAR(200),
    notified       TINYINT(1) NOT NULL DEFAULT 0,    -- set by the background scheduler
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rem_user FOREIGN KEY (participant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rem_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_rem_due (notified, remind_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- System-wide settings (key/value)
-- ---------------------------------------------------------------------
CREATE TABLE system_settings (
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(500) NOT NULL
) ENGINE=InnoDB;
