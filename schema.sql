-- ==========================================
-- 最终版 DDL (PostgreSQL)
-- 包含设备表和核心流水表，带完整的外键约束和索引
-- ==========================================

-- 1. 设备表 (Device Identity)
CREATE TABLE IF NOT EXISTS device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE, -- 唯一索引，支持 ON CONFLICT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    meta TEXT NOT NULL DEFAULT '{}'
);
CREATE INDEX IF NOT EXISTS idx_device_last_seen ON device(last_seen_at DESC);

-- 2. 评估记录表 (Core Fact Table)
CREATE TABLE IF NOT EXISTS assessment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    device_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- ✅ 补齐

    mode VARCHAR(16) NOT NULL,
    persona VARCHAR(32) NOT NULL,
    scene VARCHAR(32) NOT NULL DEFAULT 'practice', -- ✅ 补齐

    prompt TEXT,
    overall_score NUMERIC(5,2),

    -- 扁平列 (带范围约束)
    fluency NUMERIC(5,2),
    completeness NUMERIC(5,2),
    relevance NUMERIC(5,2),

    -- 扩展字段
    metrics TEXT NOT NULL DEFAULT '{}',
    feedback TEXT NOT NULL DEFAULT '{}',

    audio_url TEXT,
    transcript TEXT,

    -- 约束
    CONSTRAINT fk_ar_device FOREIGN KEY (device_id) REFERENCES device(device_id) ON DELETE CASCADE,

    -- ✅ 严谨的数值约束
    CONSTRAINT ck_overall_score_range CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
    CONSTRAINT ck_fluency_range CHECK (fluency IS NULL OR (fluency >= 0 AND fluency <= 100)),
    CONSTRAINT ck_completeness_range CHECK (completeness IS NULL OR (completeness >= 0 AND completeness <= 100)),
    CONSTRAINT ck_relevance_range CHECK (relevance IS NULL OR (relevance >= 0 AND relevance <= 100))
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_ar_device_persona_time
    ON assessment_record(device_id, persona, created_at DESC);

-- 覆盖成长历史列表常用字段，提升 index-only 扫描概率
CREATE INDEX IF NOT EXISTS idx_ar_device_persona_time_cover
    ON assessment_record(device_id, persona, created_at DESC)
    INCLUDE (overall_score, fluency, completeness, relevance, scene);

-- 3. 用户账号表 (Apple 登录)
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apple_sub VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(256),
    email_verified BOOLEAN,
    display_name VARCHAR(128),
    avatar_url VARCHAR(512),
    phone VARCHAR(32),
    bio VARCHAR(512),
    device_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_account_device ON user_account(device_id);

-- 4. 每日挑战题目表 (AI Generated Topics Cache)
CREATE TABLE IF NOT EXISTS daily_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(512) NOT NULL,
    description TEXT,
    target_persona VARCHAR(32) NOT NULL,
    for_date DATE NOT NULL,
    ai_suggestions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    persona VARCHAR(32),
    prompt TEXT,
    image_url VARCHAR(512),
    payload JSONB NOT NULL DEFAULT '{}',
    UNIQUE(for_date, persona)
);
CREATE INDEX IF NOT EXISTS idx_daily_topics_date_persona ON daily_topics(for_date, persona);

-- 5. 异步评估任务表 (Asynchronous Evaluation Tasks)
CREATE TABLE IF NOT EXISTS evaluation_tasks (
    id VARCHAR(36) PRIMARY KEY,
    user_identity VARCHAR(255) NOT NULL,
    owner_user_id BIGINT,
    persona VARCHAR(32) NOT NULL,
    scene VARCHAR(255) NOT NULL,
    transcript TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    progress INTEGER,
    error_message TEXT,
    result JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 索引，用于快速查找某个用户的所有任务或某个任务的状态
CREATE INDEX IF NOT EXISTS idx_evaluation_tasks_user_id ON evaluation_tasks(user_identity);
CREATE INDEX IF NOT EXISTS idx_evaluation_tasks_owner_user_id ON evaluation_tasks(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_tasks_status ON evaluation_tasks(status);
CREATE INDEX IF NOT EXISTS idx_evaluation_tasks_created_at ON evaluation_tasks(created_at DESC);
