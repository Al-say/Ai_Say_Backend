package com.zhupinzan.speaking.model;

/**
 * 应用通用常量类，定义系统通用的配置常量。
 *
 * <h3>设计原则</h3>
 * <p>本常量类采用以下设计原则：
 * <ul>
 *   <li><b>全局统一</b>：所有系统通用配置集中管理，避免分散定义</li>
 *   <li><b>分类清晰</b>：按功能模块分类，便于查找和维护</li>
 *   <li><b>命名规范</b>：使用有意义的命名，符合Java命名规范</li>
 *   <li><b>文档完整</b>：每个常量都有详细的说明和使用场景</li>
 *   <li><b>易于扩展</b>：新增配置项只需在相应分类下添加</li>
 * </ul>
 * </p>
 *
 * <h3>常量分类</h3>
 * <p>常量按功能分为以下几类：
 * <ul>
 *   <li><b>系统配置常量</b>：应用运行的基本配置参数</li>
 *   <li><b>分页配置常量</b>：数据分页的相关配置</li>
 *   <li><b>文件配置常量</b>：文件处理和存储的相关配置</li>
 *   <li><b>网络配置常量</b>：网络请求和API调用的相关配置</li>
 *   <li><b>安全配置常量</b>：系统安全和认证的相关配置</li>
 *   <li><b>业务配置常量</b>：业务逻辑相关的配置参数</li>
 *   <li><b>图片资源常量</b>：系统支持的图片资源URL</li>
 * </ul>
 * </p>
 *
 * <h3>设计模式</h3>
 * <p>本常量类采用以下设计模式：
 * <ul>
 *   <li><b>工具类模式</b>：提供静态方法便于使用</li>
 *   <li><b>工厂方法模式</b>：通过工厂方法创建配置对象</li>
 *   <li><b>配置组合模式</b>：将相关配置组合成配置对象</li>
 * </ul>
 * </p>
 *
 * <h3>使用场景</h3>
 * <p>本常量类广泛应用于以下场景：
 * <ul>
 *   <li><b>配置初始化</b>：系统启动时加载配置</li>
 *   <li><b>参数校验</b>：验证输入参数的有效性</li>
 *   <li><b>业务逻辑</b>：根据配置执行不同的业务逻辑</li>
 *   <li><b>界面展示</b>：生成展示用的配置信息</li>
 *   <li><b>数据迁移</b>：批量处理配置相关数据</li>
 * </ul>
 * </p>
 *
 * <h3>维护指南</h3>
 * <p>在添加新配置时的注意事项：
 * <ul>
 *   <li><b>分类合理</b>：将配置项放在合适的分类下</li>
 *   <li><b>命名规范</b>：使用有意义的英文命名，常量全大写</li>
 *   <li><b>文档完整</b>：每个配置项都要有详细的文档说明</li>
 *   <li><b>默认值设置</b>：设置合理的默认值</li>
 *   <li><b>版本控制</li>：重要配置变更需要更新版本号</li>
 * </ul>
 * </p>
 *
 * <h3>国际化考虑</h3>
 * <p>本常量类支持国际化扩展：
 * <ul>
 *   <li>所有显示文本应从常量中获取，避免硬编码</li>
 *   <li>可考虑创建资源文件管理多语言版本</li>
 *   <li>错误消息支持多语言</li>
 *   <li>提示信息本地化</li>
 * </ul>
 * </p>
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public final class AppConstants {

    // 防止实例化
    private AppConstants() {}

    // ========== 版本信息 ==========

    /**
     * 应用版本号
     *
     * <p>格式：主版本号.次版本号.修订号（如：1.0.0）</p>
     */
    public static final String APP_VERSION = "1.0.0";

    /**
     * 应用名称
     */
    public static final String APP_NAME = "AI Say";

    /**
     * 应用描述
     */
    public static final String APP_DESCRIPTION = "智能英语口语练习平台";

    /**
     * 应用标识符
     */
    public static final String APP_ID = "ai.speak.backend";

    // ========== 系统配置常量 ==========

    /**
     * API版本号
     */
    public static final String API_VERSION = "v1";

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 默认时区
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    /**
     * 默认语言
     */
    public static final String DEFAULT_LOCALE = "zh_CN";

    /**
     * 系统默认超时时间（毫秒）
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;

    /**
     * 系统最大并发请求数
     */
    public static final int MAX_CONCURRENT_REQUESTS = 100;

    /**
     * 系统支持的最大文件大小（字节）- 10MB
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 系统支持的文件类型
     */
    public static final String[] SUPPORTED_FILE_TYPES = {
        "audio/mpeg",      // MP3
        "audio/wav",       // WAV
        "audio/ogg",       // OGG
        "audio/mp4",       // M4A
        "audio/webm"       // WebM
    };

    /**
     * 检查文件类型是否支持
     *
     * @param contentType 文件类型
     * @return true 如果支持，false 否则
     */
    public static boolean isSupportedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        for (String type : SUPPORTED_FILE_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    // ========== 分页配置常量 ==========

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认页面大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大页面大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE_NORMAL = 20;

    /**
     * 移动端分页大小
     */
    public static final int PAGE_SIZE_MOBILE = 10;

    /**
     * 获取分页大小
     *
     * @param isMobile 是否移动端
     * @return 分页大小
     */
    public static int getPageSize(boolean isMobile) {
        return isMobile ? PAGE_SIZE_MOBILE : DEFAULT_PAGE_SIZE_NORMAL;
    }

    /**
     * 分页查询配置类
     */
    public static class PaginationConfig {
        private final int page;
        private final int size;
        private final int offset;

        public PaginationConfig(int page, int size) {
            this.page = Math.max(1, page);
            this.size = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
            this.offset = (this.page - 1) * this.size;
        }

        // Getters
        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getOffset() { return offset; }

        /**
         * 创建默认分页配置
         *
         * @return 默认分页配置
         */
        public static PaginationConfig createDefault() {
            return new PaginationConfig(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
        }

        /**
         * 创建移动端分页配置
         *
         * @return 移动端分页配置
         */
        public static PaginationConfig createMobile() {
            return new PaginationConfig(DEFAULT_PAGE, PAGE_SIZE_MOBILE);
        }
    }

    // ========== 文件配置常量 ==========

    /**
     * 默认文件存储路径
     */
    public static final String DEFAULT_STORAGE_PATH = "/storage";

    /**
     * 音频文件存储路径
     */
    public static final String AUDIO_STORAGE_PATH = "/storage/audio";

    /**
     * 图片文件存储路径
     */
    public static final String IMAGE_STORAGE_PATH = "/storage/images";

    /**
     * 用户文件存储路径前缀
     */
    public static final String USER_STORAGE_PREFIX = "/storage/user";

    /**
     * 音频文件名前缀
     */
    public static final String AUDIO_FILE_PREFIX = "audio_";

    /**
     * 用户头像文件名前缀
     */
    public static final String AVATAR_FILE_PREFIX = "avatar_";

    /**
     * 音频文件扩展名
     */
    public static final String AUDIO_FILE_EXTENSION = ".mp3";

    /**
     * 图片文件扩展名
     */
    public static final String IMAGE_FILE_EXTENSION = ".jpg";

    /**
     * 音频文件格式配置
     */
    public static class AudioFormatConfig {
        private final String format;
        private final String extension;
        private final String mimeType;
        private final int sampleRate;
        private final int channels;
        private final int bitRate;

        public AudioFormatConfig(String format, String extension, String mimeType,
                               int sampleRate, int channels, int bitRate) {
            this.format = format;
            this.extension = extension;
            this.mimeType = mimeType;
            this.sampleRate = sampleRate;
            this.channels = channels;
            this.bitRate = bitRate;
        }

        // Getters
        public String getFormat() { return format; }
        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
        public int getSampleRate() { return sampleRate; }
        public int getChannels() { return channels; }
        public int getBitRate() { return bitRate; }
    }

    /**
     * 获取音频格式配置
     *
     * @return 音频格式配置数组
     */
    public static AudioFormatConfig[] getAudioFormatConfigs() {
        return new AudioFormatConfig[]{
            new AudioFormatConfig("mp3", ".mp3", "audio/mpeg", 44100, 2, 128),
            new AudioFormatConfig("wav", ".wav", "audio/wav", 44100, 2, 16),
            new AudioFormatConfig("ogg", ".ogg", "audio/ogg", 44100, 2, 192),
            new AudioFormatConfig("webm", ".webm", "audio/webm", 48000, 2, 128)
        };
    }

    // ========== 网络配置常量 ==========

    /**
     * 默认请求头 Content-Type
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    /**
     * 默认字符集
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认读取超时时间（毫秒）
     */
    public static final int DEFAULT_READ_TIMEOUT = 30000;

    /**
     * 默认重试次数
     */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /**
     * 默认重试间隔（毫秒）
     */
    public static final int DEFAULT_RETRY_INTERVAL = 1000;

    /**
     * 网络请求配置类
     */
    public static class NetworkConfig {
        private final int connectTimeout;
        private final int readTimeout;
        private final int retryCount;
        private final int retryInterval;
        private final String userAgent;

        public NetworkConfig(int connectTimeout, int readTimeout, int retryCount,
                           int retryInterval, String userAgent) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.retryCount = retryCount;
            this.retryInterval = retryInterval;
            this.userAgent = userAgent;
        }

        // Getters
        public int getConnectTimeout() { return connectTimeout; }
        public int getReadTimeout() { return readTimeout; }
        public int getRetryCount() { return retryCount; }
        public int getRetryInterval() { return retryInterval; }
        public String getUserAgent() { return userAgent; }

        /**
         * 创建默认网络配置
         *
         * @return 默认网络配置
         */
        public static NetworkConfig createDefault() {
            return new NetworkConfig(
                DEFAULT_CONNECT_TIMEOUT,
                DEFAULT_READ_TIMEOUT,
                DEFAULT_RETRY_COUNT,
                DEFAULT_RETRY_INTERVAL,
                APP_NAME + "/" + APP_VERSION
            );
        }
    }

    // ========== 安全配置常量 ==========

    /**
     * 默认密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * 默认密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 64;

    /**
     * 默认token过期时间（秒）- 24小时
     */
    public static final int DEFAULT_TOKEN_EXPIRE_SECONDS = 24 * 3600;

    /**
     * 默认refresh token过期时间（秒）- 7天
     */
    public static final int DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS = 7 * 24 * 3600;

    /**
     * 默认登录失败锁定时间（秒）- 15分钟
     */
    public static final int DEFAULT_LOCK_TIME_SECONDS = 15 * 60;

    /**
     * 默认登录失败最大次数
     */
    public static final int MAX_LOGIN_FAILURE_COUNT = 5;

    /**
     * 密码配置类
     */
    public static class PasswordConfig {
        private final int minLength;
        private final int maxLength;
        private final boolean requireUppercase;
        private final boolean requireLowercase;
        private final boolean requireNumbers;
        private final boolean requireSpecialChars;

        public PasswordConfig(int minLength, int maxLength, boolean requireUppercase,
                           boolean requireLowercase, boolean requireNumbers,
                           boolean requireSpecialChars) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.requireUppercase = requireUppercase;
            this.requireLowercase = requireLowercase;
            this.requireNumbers = requireNumbers;
            this.requireSpecialChars = requireSpecialChars;
        }

        // Getters
        public int getMinLength() { return minLength; }
        public int getMaxLength() { return maxLength; }
        public boolean isRequireUppercase() { return requireUppercase; }
        public boolean isRequireLowercase() { return requireLowercase; }
        public boolean isRequireNumbers() { return requireNumbers; }
        public boolean isRequireSpecialChars() { return requireSpecialChars; }

        /**
         * 创建默认密码配置
         *
         * @return 默认密码配置
         */
        public static PasswordConfig createDefault() {
            return new PasswordConfig(
                PASSWORD_MIN_LENGTH,
                PASSWORD_MAX_LENGTH,
                true,
                true,
                true,
                false
            );
        }
    }

    // ========== 业务配置常量 ==========

    /**
     * 默认评估时长限制（秒）- 30秒
     */
    public static final int DEFAULT_EVALUATION_DURATION_LIMIT = 30;

    /**
     * 最大评估时长限制（秒）- 120秒
     */
    public static final int MAX_EVALUATION_DURATION_LIMIT = 120;

    /**
     * 默认每日练习次数限制
     */
    public static final int DEFAULT_DAILY_LIMIT = 20;

    /**
     * 最大每日练习次数限制
     */
    public static final int MAX_DAILY_LIMIT = 50;

    /**
     * 默认学习连续打卡天数
     */
    public static final int DEFAULT_STREAK_DAYS = 7;

    /**
     * 最大学习连续打卡天数
     */
    public static final int MAX_STREAK_DAYS = 365;

    /**
     * 评估结果缓存时间（秒）- 1小时
     */
    public static final int EVALUATION_CACHE_TTL = 3600;

    /**
     * 练习记录保留时间（天）- 365天
     */
    public static final int PRACTICE_RECORD_RETENTION_DAYS = 365;

    /**
     * 业务配置类
     */
    public static class BusinessConfig {
        private final int evaluationDurationLimit;
        private final int dailyLimit;
        private final int streakDays;
        private final int evaluationCacheTtl;
        private final int practiceRecordRetentionDays;

        public BusinessConfig(int evaluationDurationLimit, int dailyLimit,
                           int streakDays, int evaluationCacheTtl,
                           int practiceRecordRetentionDays) {
            this.evaluationDurationLimit = evaluationDurationLimit;
            this.dailyLimit = dailyLimit;
            this.streakDays = streakDays;
            this.evaluationCacheTtl = evaluationCacheTtl;
            this.practiceRecordRetentionDays = practiceRecordRetentionDays;
        }

        // Getters
        public int getEvaluationDurationLimit() { return evaluationDurationLimit; }
        public int getDailyLimit() { return dailyLimit; }
        public int getStreakDays() { return streakDays; }
        public int getEvaluationCacheTtl() { return evaluationCacheTtl; }
        public int getPracticeRecordRetentionDays() { return practiceRecordRetentionDays; }

        /**
         * 创建默认业务配置
         *
         * @return 默认业务配置
         */
        public static BusinessConfig createDefault() {
            return new BusinessConfig(
                DEFAULT_EVALUATION_DURATION_LIMIT,
                DEFAULT_DAILY_LIMIT,
                DEFAULT_STREAK_DAYS,
                EVALUATION_CACHE_TTL,
                PRACTICE_RECORD_RETENTION_DAYS
            );
        }
    }

    // ========== 图片资源常量 ==========

    /**
     * 默认用户头像URL
     */
    public static final String DEFAULT_AVATAR_URL =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/default-avatar.jpg";

    /**
     * 系统Logo URL
     */
    public static final String LOGO_URL =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/logo.png";

    /**
     * 系统Banner URL
     */
    public static final String BANNER_URL =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/banner.jpg";

    /**
     * 默认场景图片URL
     */
    public static final String DEFAULT_SCENE_IMAGE_URL =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/scene-default.jpg";

    /**
     * 社交分享图片URL
     */
    public static final String SHARE_IMAGE_URL =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/share.jpg";

    /**
     * 成就勋章图片URL前缀
     */
    public static final String ACHIEVEMENT_IMAGE_PREFIX =
        "https://cdn.jsdelivr.net/gh/your-repo/ai-say-backend/resources/images/achievements/";

    /**
     * 图片资源配置类
     */
    public static class ImageConfig {
        private final String defaultAvatar;
        private final String logo;
        private final String banner;
        private final String defaultScene;
        private final String share;
        private final String achievementPrefix;
        private final String[] availableFormats;

        public ImageConfig(String defaultAvatar, String logo, String banner,
                         String defaultScene, String share, String achievementPrefix,
                         String[] availableFormats) {
            this.defaultAvatar = defaultAvatar;
            this.logo = logo;
            this.banner = banner;
            this.defaultScene = defaultScene;
            this.share = share;
            this.achievementPrefix = achievementPrefix;
            this.availableFormats = availableFormats;
        }

        // Getters
        public String getDefaultAvatar() { return defaultAvatar; }
        public String getLogo() { return logo; }
        public String getBanner() { return banner; }
        public String getDefaultScene() { return defaultScene; }
        public String getShare() { return share; }
        public String getAchievementPrefix() { return achievementPrefix; }
        public String[] getAvailableFormats() { return availableFormats; }

        /**
         * 创建图片URL
         *
         * @param type 图片类型
         * @param filename 文件名（带扩展名）
         * @return 完整的图片URL
         */
        public String getImageUrl(String type, String filename) {
            return switch (type) {
                case "achievement" -> achievementPrefix + filename;
                case "avatar" -> defaultAvatar.equals(filename) ? defaultAvatar : achievementPrefix + filename;
                case "logo" -> logo;
                case "banner" -> banner;
                case "scene" -> defaultScene.equals(filename) ? defaultScene : achievementPrefix + filename;
                default -> defaultScene;
            };
        }

        /**
         * 创建默认图片配置
         *
         * @return 默认图片配置
         */
        public static ImageConfig createDefault() {
            return new ImageConfig(
                DEFAULT_AVATAR_URL,
                LOGO_URL,
                BANNER_URL,
                DEFAULT_SCENE_IMAGE_URL,
                SHARE_IMAGE_URL,
                ACHIEVEMENT_IMAGE_PREFIX,
                new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"}
            );
        }
    }

    // ========== 状态码常量 ==========

    /**
     * 默认成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 默认错误状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 参数错误状态码
     */
    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 资源未找到状态码
     */
    public static final int NOT_FOUND_CODE = 404;

    /**
     * 资源冲突状态码
     */
    public static final int CONFLICT_CODE = 409;

    /**
     * 请求太多状态码
     */
    public static final int TOO_MANY_REQUESTS_CODE = 429;

    /**
     * 服务不可用状态码
     */
    public static final int SERVICE_UNAVAILABLE_CODE = 503;

    /**
     * 获取状态码消息
     *
     * @param statusCode 状态码
     * @return 状态码对应的消息
     */
    public static String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case SUCCESS_CODE -> "请求成功";
            case BAD_REQUEST_CODE -> "请求参数错误";
            case UNAUTHORIZED_CODE -> "未授权访问";
            case FORBIDDEN_CODE -> "禁止访问";
            case NOT_FOUND_CODE -> "资源未找到";
            case CONFLICT_CODE -> "资源冲突";
            case TOO_MANY_REQUESTS_CODE -> "请求过于频繁";
            case SERVICE_UNAVAILABLE_CODE -> "服务不可用";
            case ERROR_CODE -> "服务器内部错误";
            default -> "未知错误";
        };
    }

    // ========== 常用正则表达式常量 ==========

    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 用户名正则表达式
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";

    /**
     * 密码正则表达式（简单版本）
     */
    public static final String PASSWORD_REGEX_SIMPLE = "^[a-zA-Z0-9_]{8,64}$";

    /**
     * URL正则表达式
     */
    public static final String URL_REGEX = "^https?://[\\w\\-]+(\\.[\\w\\-]+)+[\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-@?^=%&/~\\+#]$";

    /**
     * 检查字符串是否匹配正则表达式
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return true 如果匹配，false 否则
     */
    public static boolean matchesRegex(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return input.matches(regex);
    }
}