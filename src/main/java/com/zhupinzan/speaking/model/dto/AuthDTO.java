package com.zhupinzan.speaking.model.dto;

public class AuthDTO {
    public record AppleLoginReq(
        String idToken,
        String deviceId,
        String displayName
    ) {}

    public record BindDeviceReq(
        String deviceId
    ) {}

    public record AuthUser(
        Long userId,
        String appleSub,
        String email,
        Boolean emailVerified,
        String displayName,
        String deviceId
    ) {}

    public record AuthResp(
        String accessToken,
        long expiresIn,
        AuthUser user
    ) {}
}
