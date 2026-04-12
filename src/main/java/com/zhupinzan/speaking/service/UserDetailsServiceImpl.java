package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义的用户详情服务实现，用于 Spring Security 加载用户认证信息。
 *
 * <p>该服务实现了 {@link UserDetailsService} 接口，是 Spring Security 认证流程中的关键组件。
 * 它的主要职责是根据用户提供的用户名（在此应用中，可以是实际的用户名或邮箱）从数据源中查找用户，
 * 并返回一个 {@link UserDetails} 对象，供 Spring Security 进行后续的密码比对和权限验证。</p>
 *
 * <h3>设计理念</h3>
 * <p>解耦用户认证信息的存储与 Spring Security 框架。Spring Security 只依赖 {@code UserDetails} 接口，
 * 而不关心具体的存储方式，这使得底层数据源可以灵活切换。</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li><b>{@code loadUserByUsername(String identifier)}</b>:
 *     <p>这是 {@code UserDetailsService} 接口的核心方法。它接收一个字符串作为用户标识符（username/email），
 *     然后尝试在数据库中查找对应的用户账户。
 *     如果找到，它将返回一个实现了 {@code UserDetails} 接口的 {@code UserAccount} 实例。
 *     如果找不到用户，则抛出 {@code UsernameNotFoundException}。</p>
 *     <p><b>查找策略</b>:
 *       <ol>
 *         <li>首先尝试通过邮箱查找用户。</li>
 *         <li>如果未找到，则尝试通过用户名查找用户。</li>
 *         <li>如果两种方式都未找到，则认为用户不存在。</li>
 *       </ol>
 *     </p>
 *   </li>
 * </ul>
 *
 * <h3>与 {@code UserAccount} 实体的关系</h3>
 * <p>{@code UserAccount} 实体本身就实现了 {@code UserDetails} 接口，因此可以直接作为 {@code UserDetails} 对象返回。
 * 这简化了代码，避免了额外的数据转换层。</p>
 *
 * <h3>错误处理</h3>
 * <p>当用户未找到时，会抛出 {@code UsernameNotFoundException}。这个异常会被 Spring Security 捕获，
 * 并通常转换为一个 401 Unauthorized 响应。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>传统用户名/密码登录认证流程。</li>
 *   <li>Spring Security 需要验证用户身份的任何场景。</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    /**
     * 根据用户名或邮箱加载用户详情。
     * <p>
     * 该方法首先尝试将传入的标识符作为邮箱进行查找。如果未找到，
     * 则尝试将其作为用户名进行查找。这支持用户通过邮箱或用户名进行登录。
     * </p>
     *
     * @param identifier 用户的用户名或邮箱。
     * @return 查找到的用户详情对象 {@link UserDetails}。
     * @throws UsernameNotFoundException 如果通过邮箱和用户名都未能找到用户。
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userAccountRepository.findByEmail(identifier)
            .or(() -> userAccountRepository.findByUsername(identifier))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
    }
}
