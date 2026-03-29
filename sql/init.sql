CREATE DATABASE IF NOT EXISTS wordsprint
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE wordsprint;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    last_login_at DATETIME DEFAULT NULL COMMENT '最近登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_username (username)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS study_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    daily_target_count INT NOT NULL DEFAULT 20 COMMENT '每日目标题数',
    review_target_count INT NOT NULL DEFAULT 10 COMMENT '每日复习目标题数',
    reminder_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启提醒',
    reminder_time TIME DEFAULT NULL COMMENT '提醒时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_study_plan_user_id (user_id),
    KEY idx_study_plan_user_id (user_id)
) COMMENT='学习计划表';

CREATE TABLE IF NOT EXISTS word_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    word VARCHAR(100) NOT NULL COMMENT '单词',
    phonetic VARCHAR(100) DEFAULT NULL COMMENT '音标',
    meaning VARCHAR(255) NOT NULL COMMENT '词义',
    example_sentence VARCHAR(500) DEFAULT NULL COMMENT '例句',
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
    source_type VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '来源类型：PRIVATE/PUBLIC/IMPORT',
    familiarity_level INT NOT NULL DEFAULT 0 COMMENT '熟练度 0-5',
    memory_status VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT '记忆状态：NEW/LEARNING/REVIEWING/MASTERED',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '累计错误次数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '累计正确次数',
    last_studied_at DATETIME DEFAULT NULL COMMENT '最近学习时间',
    is_public TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_word_card_user_id (user_id),
    KEY idx_word_card_user_status (user_id, memory_status),
    KEY idx_word_card_user_familiarity (user_id, familiarity_level),
    KEY idx_word_card_user_deleted (user_id, is_deleted),
    KEY idx_word_card_word (word)
) COMMENT='单词卡表';

CREATE TABLE IF NOT EXISTS study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    word_card_id BIGINT NOT NULL COMMENT '单词卡ID',
    study_mode VARCHAR(20) NOT NULL COMMENT '模式：WORD_TO_MEANING/MEANING_TO_WORD/CHOICE/WRONG_REVIEW',
    answer_content VARCHAR(255) DEFAULT NULL COMMENT '用户答案',
    correct_answer VARCHAR(255) DEFAULT NULL COMMENT '正确答案快照',
    is_correct TINYINT NOT NULL COMMENT '是否正确',
    duration_seconds INT NOT NULL DEFAULT 0 COMMENT '答题耗时秒数',
    studied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
    KEY idx_study_record_user_time (user_id, studied_at),
    KEY idx_study_record_word_id (word_card_id),
    KEY idx_study_record_user_correct (user_id, is_correct)
) COMMENT='学习记录表';

CREATE TABLE IF NOT EXISTS wrong_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    word_card_id BIGINT NOT NULL COMMENT '单词卡ID',
    wrong_count INT NOT NULL DEFAULT 1 COMMENT '错题累计次数',
    resolved_correct_streak INT NOT NULL DEFAULT 0 COMMENT '纠正连续答对次数',
    last_wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近答错时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RESOLVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_wrong_word_user_word (user_id, word_card_id),
    KEY idx_wrong_word_user_status (user_id, status),
    KEY idx_wrong_word_last_wrong_at (last_wrong_at)
) COMMENT='错题表';

CREATE TABLE IF NOT EXISTS daily_checkin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    study_count INT NOT NULL DEFAULT 0 COMMENT '当日学习题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '当日正确题数',
    total_duration_seconds INT NOT NULL DEFAULT 0 COMMENT '当日总学习时长秒数',
    points_earned INT NOT NULL DEFAULT 0 COMMENT '当日获得积分',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_daily_checkin_user_date (user_id, checkin_date),
    KEY idx_daily_checkin_user_id (user_id),
    KEY idx_daily_checkin_date (checkin_date)
) COMMENT='每日打卡表';

CREATE TABLE IF NOT EXISTS user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points INT NOT NULL DEFAULT 0 COMMENT '总积分',
    streak_days INT NOT NULL DEFAULT 0 COMMENT '连续学习天数',
    max_streak_days INT NOT NULL DEFAULT 0 COMMENT '最高连续学习天数',
    total_studied INT NOT NULL DEFAULT 0 COMMENT '总学习题数',
    total_correct INT NOT NULL DEFAULT 0 COMMENT '总正确题数',
    total_duration_seconds INT NOT NULL DEFAULT 0 COMMENT '总学习时长秒数',
    last_checkin_date DATE DEFAULT NULL COMMENT '最近打卡日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_points_user_id (user_id)
) COMMENT='用户积分汇总表';

CREATE TABLE IF NOT EXISTS public_word_library (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    word VARCHAR(100) NOT NULL COMMENT '单词',
    phonetic VARCHAR(100) DEFAULT NULL COMMENT '音标',
    meaning VARCHAR(255) NOT NULL COMMENT '词义',
    example_sentence VARCHAR(500) DEFAULT NULL COMMENT '例句',
    level_tag VARCHAR(50) DEFAULT NULL COMMENT '难度标签，如 CET4/CET6/IELTS',
    source_name VARCHAR(100) DEFAULT NULL COMMENT '来源名',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_public_word_library_word_level (word, level_tag),
    KEY idx_public_word_library_level_tag (level_tag),
    KEY idx_public_word_library_status (status)
) COMMENT='公共词库表';

INSERT INTO user (username, password, nickname, role)
VALUES ('demo', '$2a$10$replace_with_bcrypt_hash', 'DemoUser', 'USER');

INSERT INTO study_plan (user_id, daily_target_count, review_target_count)
VALUES (1, 20, 10);

INSERT INTO user_points (user_id, total_points, streak_days, max_streak_days)
VALUES (1, 0, 0, 0);

INSERT INTO word_card (user_id, word, phonetic, meaning, example_sentence, tags)
VALUES
(1, 'abandon', '/əˈbændən/', '放弃', 'He had to abandon the plan.', 'cet4,verb'),
(1, 'benefit', '/ˈbenɪfɪt/', '好处; 使受益', 'Regular exercise will benefit your health.', 'cet4,noun'),
(1, 'capture', '/ˈkæptʃər/', '捕获; 记录', 'The app can capture user feedback.', 'cet4,verb');
