package com.zhupinzan.speaking.service.storage;

public interface StorageService {

    /**
     * 上传字节数组
     * @return 返回可供前端访问的完整 URL
     */
    String uploadAudio(byte[] data, String deviceId, String extension);
}
