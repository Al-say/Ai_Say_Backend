# API总览

## 背景与目的
本项目API总览，列出所有端点、鉴权、分页、错误码、版本策略。

## 范围与不包含
- 包含：所有公开API端点
- 不包含：内部服务API

## 核心概念与名词表
- JWT：JSON Web Token，用于认证
- 分页：使用page和size参数
- 错误码：见ErrorCode枚举

## 关键流程
除 `/api/auth/**` 外，其它API都需要Bearer token认证。

## 关键接口或关键文件位置
- 控制器：controller/ 目录
- 错误码：ErrorCode.java

## 常见失败模式与排障
- 401 Unauthorized：token无效
- 403 Forbidden：权限不足

## API端点列表

### 认证模块 (/api/auth)
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户名密码登录
- POST /api/auth/apple - Apple ID登录
- GET /api/auth/me - 获取当前用户信息
- POST /api/auth/bind-device - 绑定设备ID

### 首页模块 (/api/home)
- GET /api/home/daily - 获取每日挑战

### 探索模块 (/api/explore)
- GET /api/explore/scenes - 获取场景列表

### 评估模块 (/api/eval)
- POST /api/eval/text - 文本评估
- POST /api/eval/audio - 音频评估（简化）
- POST /api/eval/audio/full - 完整音频评估

### 成长模块 (/api/growth)
- GET /api/growth/history - 评估历史
- GET /api/growth/analysis - 能力分析
- GET /api/growth/detail/{id} - 评估详情

### 个人模块 (/api/profile)
- GET /api/profile - 个人主页
- GET /api/profile/stats - 统计数据
- POST /api/profile/device - 设备绑定
- GET /api/profile/login-history - 登录历史

### 音频模块 (/api/audio)
- POST /api/audio/upload - 音频上传

### 测试模块 (/test)
- GET /test - 测试端点
[返回导航](../README.md)
