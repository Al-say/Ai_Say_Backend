package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.model.entity.Device;
import com.zhupinzan.speaking.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 评估服务类，用于处理文本评估请求，调用AI服务并保存评估记录
 */
@Service
@RequiredArgsConstructor
public class EvalService {

    /** DeepSeek评估服务，用于调用AI评估 */
    private final DeepSeekEvalService deepSeekEvalService;
    /** 评估数据服务，用于保存记录 */
    private final AssessmentService assessmentService;
    /** 设备数据访问层 */
    private final DeviceRepository deviceRepository;

    /**
     * 评估方法，处理文本评估请求
     * @param req 评估请求DTO
     * @param persona 用户画像
     * @return 评估响应DTO
     * @throws Exception 如果评估失败
     */
    public EvalDTO.TextEvalResp evaluate(EvalDTO.TextEvalReq req, UserPersona persona) throws Exception {

        // 步骤1: 并发安全的 Device 注册/保活
        deviceRepository.upsertTouch(req.getDeviceId());

        // 步骤2: 调用 DeepSeek 获取评估结果
        EvalDTO.TextEvalResp resp = deepSeekEvalService.evaluate(req.getPrompt(), req.getUserText(), persona);

        // 步骤3: 解析 Mode (判空处理)
        AssessmentMode mode = null;
        if (req.getMode() != null && !req.getMode().trim().isEmpty()) {
            try {
                mode = AssessmentMode.valueOf(req.getMode().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果mode无效，使用默认推断逻辑
                mode = null;
            }
        }

        // 步骤4: 保存评估记录到数据库
        assessmentService.saveAttempt(
            req.getDeviceId(), // 从请求中获取设备ID
            persona,
            req.getPrompt(),
            req.getUserText(),
            req.getAudioUrl(),
            resp,
            mode // 传递解析后的mode，如果为null则在service中自动推断
        );

        // 步骤5: 设置返回字段
        resp.setUserText(req.getUserText());

        return resp;
    }
}