package com.zhupinzan.speaking.model;

/**
 * 用户角色画像枚举，定义系统中支持的不同用户类型及其对应的行为特征。
 */
public enum UserPersona {
    /**
     * 备考党用户画像 (Exam Preparation)
     * - 角色: Exam Candidate - 考试备考者
     * - 风格: Strict & Academic - 严格学术型
     */
    EXAM_PREP,

    /**
     * 职场发展用户画像 (Career Growth)
     * - 角色: Business Professional - 商务专业人士
     * - 风格: Pragmatic & Concise - 务实简洁型
     */
    CAREER_GROWTH;
}
