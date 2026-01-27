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
     * 根据Apple订阅标识查询用户账户
     *
     * <p>方法说明：
     * 此方法用于通过Apple身份验证返回的sub（subject）字段来查找对应的用户账户。
     * 这是Apple认证流程中的关键查询，用于确认用户身份并建立会话。
     *
     * <p>参数详细说明：
     * @param appleSub Apple身份验证服务返回的用户唯一标识符，格式为UUID字符串
     *                 这是Apple生态系统中的用户身份标识，在应用生命周期内保持不变
     *
     * <p>返回值说明：
     * @return Optional<UserAccount> 包含用户账户的可选容器
     *         - 如果找到匹配的用户账户，返回包含UserAccount对象的Optional
     *         - 如果未找到匹配的用户，返回空的Optional
     *         使用Optional避免空指针异常，符合最佳实践
     *
     * <p>业务场景说明：
     * 1. 用户登录流程：Apple认证成功后，使用sub查询本地用户账户
     * 2. 用户注册流程：如果查询结果为空，说明是新用户，需要创建新账户
     * 3. 会话管理：在Spring Security中用于Principal的识别和授权
     *
     * <p>性能优化建议：
     * - 确保appleSub字段在数据库中有唯一索引
     * - 考虑appleSub字段的长度，适当限制数据库字段大小
     * - 在高并发场景下，考虑使用缓存存储常用用户账户
     *
     * <p>异常处理：
     * - 方法本身不直接抛出异常，由Spring Data JPA处理数据库异常
     * - 调用方需要处理Optional.get()可能抛出的NoSuchElementException
     */
    Optional<UserAccount> findByAppleSub(String appleSub);
}
