package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户账户数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责用户账户实体的数据库访问操作，采用JPA规范实现数据持久化。
 * 主要管理通过Apple ID认证的用户账户信息，作为用户身份验证和授权的数据基础。
 *
 * <p>数据完整性保证：
 * - 基于JPA注解确保数据约束
 * - 遵循Spring Data JPA的事务管理机制
 * - 采用Optional类型避免空指针异常
 *
 * <p>性能考虑：
 * - 继承JpaRepository自动提供CRUD优化
 * - 使用findBy*方法自动生成查询
 * - 合理利用索引优化查询性能
 *
 * <p>与业务逻辑的关联：
 * - 与UserAuthenticationService配合完成用户认证
 * - 为AssessmentRecord提供用户画像关联
 * - 支持后续扩展的用户个性化功能
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * 根据用户名查询用户账户
     *
     * <p>方法说明：
     * 此方法用于通过用户名查找对应的用户账户。
     * 用于传统用户名密码登录的身份验证。
     *
     * @param username 用户名
     * @return Optional<UserAccount> 包含用户账户的可选容器
     */
    Optional<UserAccount> findByUsername(String username);

    /**
     * 根据邮箱查询用户账户
     *
     * <p>方法说明：
     * 此方法用于通过邮箱地址查找对应的用户账户。
     * 用于邮箱唯一性验证和可能的邮箱登录功能。
     *
     * @param email 邮箱地址
     * @return Optional<UserAccount> 包含用户账户的可选容器
     */
    Optional<UserAccount> findByEmail(String email);

    /**
     * 根据Apple用户标识符查询用户账户
     *
     * <p>方法说明：
     * 此方法用于通过Apple ID的sub字段查找对应的用户账户。
     * 用于Apple Sign-In认证的身份验证。
     *
     * @param appleSub Apple用户标识符
     * @return Optional<UserAccount> 包含用户账户的可选容器
     */
    Optional<UserAccount> findByAppleSub(String appleSub);
}
