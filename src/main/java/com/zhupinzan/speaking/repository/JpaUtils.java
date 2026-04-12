package com.zhupinzan.speaking.repository;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

/**
 * JPA 工具方法，供多个 Repository 共用，避免代码重复。
 */
public final class JpaUtils {

    private JpaUtils() {
    }

    /**
     * 判断当前数据库是否为 PostgreSQL。
     * 优先从 hibernate.dialect 属性判断，其次通过 JDBC 元数据兜底。
     */
    public static boolean isPostgres(EntityManager entityManager) {
        Object dialect = entityManager.getEntityManagerFactory()
                .getProperties()
                .get("hibernate.dialect");
        if (dialect != null && dialect.toString().toLowerCase().contains("postgres")) {
            return true;
        }
        try {
            return entityManager.unwrap(Session.class)
                    .doReturningWork(conn -> conn.getMetaData().getDatabaseProductName())
                    .toLowerCase()
                    .contains("postgres");
        } catch (Exception e) {
            return false;
        }
    }
}
