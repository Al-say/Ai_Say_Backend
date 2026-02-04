package com.zhupinzan.speaking.service;

/**
 * 每日挑战题目持久化异常
 * 当数据库操作失败时抛出此异常
 */
public class DailyTopicPersistenceException extends RuntimeException {

    public DailyTopicPersistenceException(String message) {
        super(message);
    }

    public DailyTopicPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}