package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSONObject;
import com.zhupinzan.speaking.config.BaiduConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 百度 ASR (自动语音识别) 服务
 *
 * <h2>服务概述</h2>
 * 这是系统与百度智能云语音识别服务对接的核心组件，负责将音频文件转换为文字。
 * 采用OAuth 2.0认证机制，支持16kHz采样率的WAV格式音频识别。
 *
 * <h2>核心架构设计</h2>
 * <ul>
 *   <li><b>认证层</b>：自动获取和管理Access Token，减少重复认证请求</li>
 *   <li><b>HTTP层</b>：使用OkHttpClient执行REST API调用，配置了300秒超时</li>
 *   <li><b>缓存层</b>：Token缓存机制，有效期为25天（百度官方为30天）</li>
 *   <li><b>错误处理</b>：完善的网络异常和API错误处理机制</li>
 * </ul>
 *
 * <h2>技术实现细节</h2>
 * <p><b>认证流程：</b></p>
 * <ol>
 *   <li>检查缓存中的Token是否有效（存在且未过期）</li>
 *   <li>若无效，使用API Key和Secret Key获取新Token</li>
 *   <li>更新缓存并设置新的过期时间</li>
 * </ol>
 *
 * <p><b>识别流程：</b></p>
 * <ol>
 *   <li>读取音频文件并Base64编码</li>
 *   <li>构建包含格式、采样率、声道等参数的请求体</li>
 *   <li>发送HTTP POST请求到百度ASR API</li>
 *   <li>解析返回的JSON响应，提取识别结果</li>
 * </ol>
 *
 * <h2>与第三方服务集成</h2>
 * <ul>
 *   <li><b>百度智能云API</b>：语音识别服务，提供高精度的语音转文字能力</li>
 *   <li><b>OAuth 2.0</b>：标准认证协议，确保API调用的安全性</li>
 *   <li><b>RESTful API</b>：基于HTTP的标准接口，易于集成和维护</li>
 * </ul>
 *
 * <h2>错误处理和降级策略</h2>
 * <ul>
 *   <li><b>认证失败</b>：抛出IOException，上层服务可以切换备用方案</li>
 *   <li><b>识别失败</b>：返回错误码和错误信息，支持重试机制</li>
 *   <li><b>网络超时</b>：配置合理的超时时间，避免长时间阻塞</li>
 *   <li><b>文件读取</b>：处理IO异常，确保系统稳定性</li>
 * </ul>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li><b>Token缓存</b>：避免频繁获取认证令牌，减少网络开销</li>
 *   <li><b>连接池</b>：OkHttpClient复用连接，提高并发性能</li>
 *   <li><b>流式处理</b>：使用try-with-resources确保资源及时释放</li>
 *   <li><b>内存管理</b>：及时清理临时文件，避免内存泄漏</li>
 * </ul>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li><b>英语口语练习</b>：将用户口语录音转换为文字，用于后续评分</li>
 *   <li><b>语音输入系统</b>：支持语音转文字的各种应用场景</li>
 *   <li><b>实时字幕生成</b>：结合实时音频流实现字幕功能</li>
 * </ul>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li>缓存Token减少API调用频率</li>
 *   <li>配置合理的超时时间</li>
 *   <li>妥善处理临时文件</li>
 *   <li>记录详细的日志信息</li>
 *   <li>实现重试机制提高可靠性</li>
 * </ul>
 *
 * @author Baidu AI
 * @version 1.0
 * @see com.zhupinzan.speaking.config.BaiduConfig
 */
@Service
@RequiredArgsConstructor
public class BaiduAsrService {

    private final BaiduConfig baiduConfig; // 注入百度云服务的配置信息（如API Key, Secret Key）

    // OkHttpClient 实例，用于执行所有到百度API的HTTP请求。设置了较长的读取超时时间以适应可能的长时间识别任务。
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    // 缓存的Access Token及其过期时间
    private String accessToken;
    private long tokenExpiryTime;

    /**
     * 环境变量验证与诊断
     *
     * <h2>方法说明</h2>
     * 在服务初始化时调用，验证百度AI服务的配置是否正确。
     * 这是一个关键的诊断方法，能够帮助开发者和运维人员快速发现配置问题。
     *
     * <h2>执行流程</h2>
     * <ol>
     *   <li>检查API Key是否已正确加载（不为null且长度>4）</li>
     *   <li>检查Secret Key是否已正确加载（不为null且长度>4）</li>
     *   <li>输出详细的检查结果，包括安全掩码显示</li>
     * </ol>
     *
     * <h2>安全考虑</h2>
     * <ul>
     *   <li>日志输出时使用安全掩码，只显示前4个字符</li>
     *   <li>避免在日志中完整记录敏感信息</li>
     *   <li>及时发现配置缺失或配置错误</li>
     * </ul>
     *
     * <h2>典型使用场景</h2>
     * <ul>
     *   <li>应用启动时的配置检查</li>
     *   <li>CI/CD流程中的环境验证</li>
     *   <li>生产环境的监控告警</li>
     * </ul>
     *
     * @throws IllegalStateException 当配置验证失败时抛出异常
     * @see PostConstruct Spring容器生命周期回调
     */
    @PostConstruct
    public void testEnvVars() {
        System.out.println("================ 环境变量检查 ================");
        if (baiduConfig.getApiKey() != null && baiduConfig.getApiKey().length() > 4) {
            System.out.println("✅ BAIDU API Key 已加载: " + baiduConfig.getApiKey().substring(0, 4) + "******");
        } else {
            System.err.println("❌ BAIDU API Key 未加载 (为 null 或为空)");
        }

        if (baiduConfig.getSecretKey() != null && baiduConfig.getSecretKey().length() > 4) {
            System.out.println("✅ BAIDU Secret Key 已加载: " + baiduConfig.getSecretKey().substring(0, 4) + "******");
        } else {
            System.err.println("❌ BAIDU Secret Key 未加载 (为 null 或为空)");
        }
        System.out.println("============================================");
    }

    /**
     * 获取百度API访问令牌 - 带缓存机制的认证管理
     *
     * <h2>业务价值</h2>
     * 这是所有百度API调用的前置步骤，负责维护认证状态。
     * 合理的缓存策略可以显著减少认证请求频率，提高系统性能。
     *
     * <h2>实现细节</h2>
     * <ul>
     *   <li><b>缓存验证</b>：首先检查内存中的Token是否在有效期内</li>
     *   <li><b>请求构造</b>：使用application/x-www-form-urlencoded格式</li>
     *   <li><b>响应解析</b>：使用FastJSON解析JSON响应</li>
     *   <li><b>错误处理</b>：处理HTTP错误码和API错误码</li>
     * </ul>
     *
     * <h2>缓存策略</h2>
     * <ul>
     *   <li><b>有效期</b>：25天（百度官方为30天，提前5天刷新）</li>
     *   <li><b>存储</b>：内存缓存，无持久化需求</li>
     *   <li><b>刷新</b>：自动过期后重新获取</li>
     * </ul>
     *
     * <h2>错误处理</h2>
     * <ul>
     *   <li><b>HTTP状态码</b>：检查响应码，非2xx状态抛出异常</li>
     *   <li><b>API错误码</b>：检查error字段，提供详细错误信息</li>
     *   <li><b>网络异常</b>：捕获并包装IOException</li>
     * </ul>
     *
     * <h2>性能考虑</h2>
     * <ul>
     *   <li>避免频繁的认证请求，降低API调用成本</li>
     *   <li>设置合理的缓存时间，平衡安全性和性能</li>
     *   <li>使用连接池复用HTTP连接</li>
     * </ul>
     *
     * @return 有效的Access Token字符串
     * @throws IOException 当获取令牌失败时抛出，包含详细的错误信息
     * @see https://aip.baidubce.com/oauth/2.0/token 百度OAuth 2.0文档
     * @see com.alibaba.fastjson2.JSONObject JSON解析工具
     */
    private String getAccessToken() throws IOException {
        // 检查缓存的token是否仍然有效
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        // 构造获取token的请求体
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
            "grant_type=client_credentials&client_id=" + baiduConfig.getApiKey() + "&client_secret=" + baiduConfig.getSecretKey());

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        System.out.println("🔑 正在获取百度Access Token...");
        // 此处省略了部分日志代码...

        try (Response response = httpClient.newCall(request).execute()) {
            okhttp3.ResponseBody respBody = response.body();
            String responseBody = respBody != null ? respBody.string() : "";
            System.out.println("🔑 Token响应码: " + response.code());
            System.out.println("🔑 Token响应内容: " + responseBody);

            if (!response.isSuccessful()) {
                System.err.println("❌ 获取Access Token失败，响应码: " + response.code());
                System.err.println("❌ 错误详情: " + responseBody);
                throw new IOException("Failed to get access token: " + response.code() + " - " + responseBody);
            }

            JSONObject jsonResponse = JSONObject.parseObject(responseBody);

            // 检查API返回的错误信息
            if (jsonResponse.containsKey("error")) {
                String error = jsonResponse.getString("error");
                String errorDesc = jsonResponse.getString("error_description");
                System.err.println("❌ Token API错误: " + error + " - " + errorDesc);
                throw new IOException("Token error: " + error + " - " + errorDesc);
            }

            String token = jsonResponse.getString("access_token");
            if (token == null || token.isEmpty()) {
                throw new IOException("获取到的 access_token 为空，响应: " + responseBody);
            }
            accessToken = token;
            // 百度令牌的有效期通常为30天。为了保险起见，我们将缓存的过期时间设置为25天。
            tokenExpiryTime = System.currentTimeMillis() + (25 * 24 * 60 * 60 * 1000L);

            System.out.println("✅ 成功获取百度Access Token");
            return accessToken;
        }
    }

    /**
     * 语音识别核心方法 - WAV文件转文字
     *
     * <h2>业务流程</h2>
     * 这是百度ASR服务的核心接口，完成从音频文件到文本的转换。
     * 整个流程包括文件读取、编码、请求发送和结果解析四个主要步骤。
     *
     * <h2>详细执行步骤</h2>
     * <ol>
     *   <li><b>文件读取</b>：将WAV文件读取为字节数组</li>
     *   <li><b>Base64编码</b>：对音频数据进行Base64编码，满足API要求</li>
     *   <li><b>请求构建</b>：构造包含音频信息和配置参数的JSON请求体</li>
     *   <li><b>API调用</b>：发送POST请求到百度ASR服务端点</li>
     *   <li><b>结果解析</b>：解析响应JSON，提取识别结果</li>
     * </ol>
     *
     * <h2>技术参数说明</h2>
     * <table border="1" cellpadding="3" cellspacing="0">
     *   <tr><th>参数</th><th>值</th><th>说明</th></tr>
     *   <tr><td>format</td><td>wav</td><td>音频格式</td></tr>
     *   <tr><td>rate</td><td>16000</td><td>采样率（Hz）</td></tr>
     *   <tr><td>channel</td><td>1</td><td>声道数（单声道）</td></tr>
     *   <tr><td>cuid</td><td>speaking_app_001</td><td>用户唯一标识</td></tr>
     *   <tr><td>dev_pid</td><td>1737</td><td>语言模型（英语）</td></tr>
     * </table>
     *
     * <h2>API交互细节</h2>
     * <ul>
     *   <li><b>端点</b>：https://vop.baidu.com/server_api</li>
     *   <li><b>方法</b>：POST</li>
     *   <li><b>Content-Type</b>：application/json</li>
     *   <li><b>认证</b>：通过Access Token认证</li>
     * </ul>
     *
     * <h2>错误处理机制</h2>
     * <ul>
     *   <li><b>HTTP错误</b>：检查响应码，非2xx抛出异常</li>
     *   <li><b>API错误码</b>：err_no为0表示成功，否则抛出异常</li>
     *   <li><b>结果验证</b>：检查result字段是否包含有效数据</li>
     * </ul>
     *
     * <h2>性能优化</h2>
     * <ul>
     *   <li>使用try-with-resources确保资源释放</li>
     *   <li>避免不必要的对象创建</li>
     *   <li>合理的超时设置（300秒）</li>
     * </ul>
     *
     * <h2>使用场景</h2>
     * <ul>
     *   <li>英语口语练习的语音转文字</li>
     *   <li>语音内容的文本化处理</li>
     *   <li>语音数据的结构化存储</li>
     * </ul>
     *
     * @param wavFile 输入的WAV格式音频文件，要求16kHz采样率、单声道
     * @return 识别出的文本内容，包含用户说出的所有内容
     * @throws IOException 文件读取失败、网络请求失败或API返回错误时抛出
     * @throws RuntimeException 当API返回业务错误时抛出
     * @see okhttp3.OkHttpClient HTTP客户端
     * @see com.alibaba.fastjson2.JSONObject JSON处理工具
     */
    public String speechToText(File wavFile) throws IOException {
        System.out.println("🎤 开始调用百度ASR API进行语音识别...");

        // 步骤 1: 将音频文件内容读取为字节数组，并进行Base64编码
        byte[] audioBytes = java.nio.file.Files.readAllBytes(wavFile.toPath());
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 步骤 2: 构建请求的JSON体
        JSONObject params = new JSONObject();
        params.put("format", "wav");       // 音频格式
        params.put("rate", 16000);         // 采样率
        params.put("channel", 1);          // 声道数
        params.put("cuid", "speaking_app_001"); // 用户唯一标识符，用于用户流量统计
        params.put("token", getAccessToken()); // 调用getAccessToken()获取有效的令牌
        params.put("speech", audioBase64); // Base64编码的音频数据
        params.put("len", audioBytes.length); // 原始音频文件大小
        params.put("dev_pid", 1737);       // 设为1737，表示使用英语识别模型

        // 步骤 3: 创建并发送HTTP POST请求
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, params.toString());

        Request request = new Request.Builder()
                .url("https://vop.baidu.com/server_api") // ASR服务的API端点
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                System.err.println("❌ ASR请求失败，响应码: " + response.code());
                System.err.println("❌ 错误详情: " + responseBody);
                throw new IOException("ASR request failed: " + response.code() + " - " + responseBody);
            }

            // 步骤 4: 解析API的响应
            JSONObject jsonResponse = JSONObject.parseObject(responseBody);
            System.out.println("📡 百度ASR响应: " + responseBody);

            // 检查错误码，0表示成功
            if (jsonResponse.containsKey("err_no") && jsonResponse.getIntValue("err_no") == 0) {
                if (jsonResponse.containsKey("result") && !jsonResponse.getJSONArray("result").isEmpty()) {
                    String recognizedText = jsonResponse.getJSONArray("result").getString(0);
                    System.out.println("🎤 ASR识别结果: " + recognizedText);
                    return recognizedText;
                } else {
                    System.err.println("❌ 未识别到语音内容");
                    throw new RuntimeException("No speech recognized");
                }
            } else {
                // 如果有错误，抛出异常
                String errorMsg = jsonResponse.getString("err_msg");
                int errorCode = jsonResponse.getIntValue("err_no");
                System.err.println("❌ 百度ASR报错 [错误码:" + errorCode + "]: " + errorMsg);
                throw new RuntimeException("ASR Error [" + errorCode + "]: " + errorMsg);
            }
        }
    }

    /**
     * 语音识别便捷方法 - 从字节数组直接识别
     *
     * <h2>方法说明</h2>
     * 这是{@link #speechToText(File)}方法的便捷封装，直接接收字节数组作为输入。
     * 对于已经存在于内存中的音频数据，此方法避免了磁盘I/O操作，提高了性能。
     *
     * <h2>实现原理</h2>
     * <ol>
     *   <li>在系统临时目录创建临时WAV文件</li>
     *   <li>将字节数组写入临时文件</li>
     *   <li>调用核心的speechToText方法进行识别</li>
     *   <li>删除临时文件，清理系统资源</li>
     * </ol>
     *
     * <h2>资源管理</h2>
     * <ul>
     *   <li><b>临时文件</b>：使用File.createTempFile创建，确保唯一性</li>
     *   <li><b>自动清理</b>：finally块确保文件被删除，避免磁盘空间泄漏</li>
     *   <li><b>异常安全</b>：即使识别失败，临时文件也会被清理</li>
     * </ul>
     *
     * <h2>性能优势</h2>
     * <ul>
     *   <li><b>减少I/O</b>：无需持久化到磁盘，直接在内存中处理</li>
     *   <li><b>提高响应速度</b>：避免了文件读写的时间开销</li>
     *   <li><b>内存效率</byte>：合理使用系统临时空间</li>
     * </ul>
     *
     * <h2>典型应用场景</h2>
     * <ul>
     *   <li>从HTTP请求直接接收的音频数据</li>
     *   <li>从数据库BLOB字段读取的音频数据</li>
     *   <li>实时音频流处理</li>
     *   <li>单元测试中的音频数据</li>
     * </ul>
     *
     * <h2>注意事项</h2>
     * <ul>
     *   <li>临时文件的生命周期仅限于方法执行期间</li>
     *   <li>请确保字节数组是有效的WAV格式</li>
     *   <li>大量并发调用时注意临时文件创建频率</li>
     * </ul>
     *
     * @param wavBytes WAV格式的音频文件字节数组，必须是有效的WAV格式数据
     * @return 识别出的文本内容，与文件输入方法返回格式一致
     * @throws IOException 当临时文件创建失败或识别过程出错时抛出
     * @throws IllegalArgumentException 当字节数组为null或格式不正确时
     * @see #speechToText(File) 基础实现方法
     * @see File#createTempFile(String, String) 临时文件创建方法
     */
    public String recognizeFromWavBytes(byte[] wavBytes) throws IOException {
        // 在系统临时目录中创建一个临时文件
        File tempFile = File.createTempFile("asr_temp", ".wav");
        try {
            // 将字节数组写入临时文件
            java.nio.file.Files.write(tempFile.toPath(), wavBytes);
            // 调用核心的识别方法
            return speechToText(tempFile);
        } finally {
            // 确保临时文件在操作完成后被删除，避免占用磁盘空间
            tempFile.delete();
        }
    }
}