-- ==========================================
-- 最终版 DDL (PostgreSQL)
-- 包含设备表和核心流水表，带完整的外键约束和索引
-- ==========================================

-- 1. 设备表 (Device Identity)
CREATE TABLE IF NOT EXISTS device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE, -- 唯一索引，支持 ON CONFLICT
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    meta JSONB NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS idx_device_last_seen ON device(last_seen_at DESC);

-- 2. 评估记录表 (Core Fact Table)
CREATE TABLE IF NOT EXISTS assessment_record (
    id BIGSERIAL PRIMARY KEY,

    device_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- ✅ 补齐

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
    metrics JSONB NOT NULL DEFAULT '{}'::jsonb,
    feedback JSONB NOT NULL DEFAULT '{}'::jsonb,

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