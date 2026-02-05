package com.zhupinzan.speaking.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.zhupinzan.speaking.config.AppleSignInConfig;
import com.zhupinzan.speaking.model.LoginType;
import com.zhupinzan.speaking.model.dto.AuthDTO;
import com.zhupinzan.speaking.model.entity.UserAccount;
import com.zhupinzan.speaking.repository.DeviceRepository;
import com.zhupinzan.speaking.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * "Sign in with Apple" 登录服务。
 * <p>
 * 该服务负责处理通过Apple ID登录的整个认证和授权流程。它包括以下几个关键步骤：
 * 1.  验证从客户端接收到的Apple ID Token的有效性。
 * 2.  根据验证后的用户信息，在系统中创建或更新用户账户。
 * 3.  生成一个本地的、用于后续API访问的Access Token。
 * <p>
 * 它使用了 nimbus-jose-jwt 库来处理JWT（JSON Web Token）的验证和签名。
 */
@Service
public class AppleSignInService {

    private final AppleSignInConfig config; // 注入Apple登录相关的配置信息
    private final UserAccountRepository userAccountRepository; // 注入用户账户的数据仓库
    private final DeviceRepository deviceRepository;
    private final LoginHistoryService loginHistoryService;

    public AppleSignInService(AppleSignInConfig config, UserAccountRepository userAccountRepository, DeviceRepository deviceRepository, LoginHistoryService loginHistoryService) {
        this.config = config;
        this.userAccountRepository = userAccountRepository;
        this.deviceRepository = deviceRepository;
        this.loginHistoryService = loginHistoryService;
    }

    /**
     * 处理Apple登录的核心业务逻辑。
     *
     * @param req 包含从Apple获取的idToken等信息的登录请求DTO。
     * @return 返回一个包含本地AccessToken和用户信息的认证响应DTO。
     * @throws Exception 如果idToken验证失败或AccessToken生成失败。
     */
    public AuthDTO.AuthResp loginWithApple(AuthDTO.AppleLoginReq req) throws Exception {
        // 步骤 1: 校验输入参数
        if (req == null || req.idToken() == null || req.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken 不能为空");
        }

        // 步骤 2: 验证Apple ID Token的合法性
        var claims = verifyAppleIdToken(req.idToken());
        var sub = claims.getSubject(); // 'sub'是Apple用户的唯一标识符
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("Apple token 缺少 sub");
        }

        // 步骤 3: 处理用户信息，创建或更新本地账户
        var email = claims.getStringClaim("email");
        var emailVerified = parseBoolean(claims.getClaim("email_verified"));

        // 尝试通过appleSub查找现有用户，如果不存在则创建一个新用户
        var account = userAccountRepository.findByAppleSub(sub).orElseGet(UserAccount::new);
        account.setAppleSub(sub);
        if (email != null) account.setEmail(email);
        if (emailVerified != null) account.setEmailVerified(emailVerified);
        if (req.displayName() != null && !req.displayName().isBlank()) {
            account.setDisplayName(req.displayName().trim());
        }
        if (req.deviceId() != null && !req.deviceId().isBlank()) {
            var deviceId = req.deviceId().trim();
            deviceRepository.upsertTouch(deviceId);
            account.setDeviceId(deviceId);
        }
        account.setLastLoginAt(java.time.OffsetDateTime.now());
        account = userAccountRepository.save(account);
        loginHistoryService.recordLogin(account, LoginType.APPLE);

        // 步骤 4: 为该用户生成一个本地的Access Token
        var accessToken = generateAccessToken(account);

        // 步骤 5: 构造并返回包含AccessToken和用户信息的响应
        return new AuthDTO.AuthResp(
            accessToken,
            config.getTokenTtlSeconds(),
            new AuthDTO.AuthUser(
                account.getId(),
                account.getAppleSub(),
                account.getEmail(),
                account.getEmailVerified(),
                account.getDisplayName(),
                account.getDeviceId()
            )
        );
    }

    /**
     * 验证从Apple收到的ID Token。
     * <p>
     * 这个过程包括：
     * 1.  从Apple的公钥服务器下载公钥集(JWKS)。
     * 2.  使用公钥验证ID Token的签名是否有效。
     * 3.  检查token的颁发者(issuer)、受众(audience)和过期时间(expiration time)是否符合预期。
     *
     * @param idToken 从客户端传来的经过签名的JWT。
     * @return 返回解析后的JWT声明集(claims)。
     * @throws Exception 如果验证过程中的任何一步失败。
     */
    private JWTClaimsSet verifyAppleIdToken(String idToken) throws Exception {
        var jwtProcessor = new DefaultJWTProcessor<SecurityContext>();
        // Apple的公钥服务器地址，从配置中读取
        var jwkSetUrl = new URL(config.getJwksUrl());
        var keySource = new RemoteJWKSet<SecurityContext>(jwkSetUrl);
        // 配置JWT处理器，使其使用从Apple服务器获取的RS256公钥进行签名验证
        var keySelector = new JWSVerificationKeySelector<SecurityContext>(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);

        // 解析并验证token
        var claims = jwtProcessor.process(idToken, null);

        // 额外的安全校验
        if (!config.getIssuer().equals(claims.getIssuer())) {
            throw new IllegalArgumentException("Apple token issuer 不匹配");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(config.getClientId())) {
            throw new IllegalArgumentException("Apple token aud 不匹配");
        }
        var exp = claims.getExpirationTime();
        if (exp == null || exp.before(new Date())) {
            throw new IllegalArgumentException("Apple token 已过期");
        }
        return claims;
    }

    /**
     * 为已认证的用户生成一个本地的Access Token。
     * <p>
     * 这个token是一个使用HS256算法和预设密钥签名的JWT。
     * 它包含了用户的ID和Apple 'sub'，并设置了过期时间。
     *
     * @param account 用户的账户实体。
     * @return 序列化后的、经过签名的JWT字符串。
     * @throws Exception 如果签名过程中发生错误。
     */
    private String generateAccessToken(UserAccount account) throws Exception {
        // 安全检查：确保token密钥已配置且长度足够
        if (config.getTokenSecret() == null || config.getTokenSecret().isBlank()) {
            throw new IllegalStateException("apple.signin.token-secret 未配置");
        }
        if (config.getTokenSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("apple.signin.token-secret 长度不足，至少 32 字节");
        }
        var now = Instant.now();
        var exp = now.plusSeconds(config.getTokenTtlSeconds());
        // 构建JWT的声明
        var claims = new JWTClaimsSet.Builder()
            .subject(String.valueOf(account.getId())) // subject 设置为本地用户ID
            .claim("appleSub", account.getAppleSub()) // 自定义声明，包含Apple的用户ID
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .build();

        // 使用HS256算法和配置的密钥进行签名
        var signer = new MACSigner(config.getTokenSecret().getBytes(StandardCharsets.UTF_8));
        var signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signed.sign(signer);

        return signed.serialize();
    }

    /**
     * 将一个对象解析为布尔值。
     * 用于处理来自Apple的 "email_verified" 声明，它可能是布尔值或字符串。
     *
     * @param v 待解析的对象。
     * @return 解析后的布尔值，如果无法解析则返回null。
     */
    private Boolean parseBoolean(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return null;
    }
}
