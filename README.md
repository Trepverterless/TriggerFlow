# 自动化助手 (TriggerFlow)

<div align="center">

![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg) ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg) ![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg) ![License](https://img.shields.io/badge/License-MIT-orange.svg)

**一款功能强大的 Android 自动化工具，让您的手机更智能**

[功能特性](#功能特性) • [快速开始](#快速开始) • [使用指南](#使用指南) • [权限说明](#权限说明)

</div>

---

## 📱 项目简介

**TriggerFlow** 是一款类似 Macrodroid 的 Android 自动化应用，通过简单的图形界面配置触发器和动作，实现手机的智能化操作。无需编程知识，即可创建复杂的自动化任务。

### 核心特性

- 🎯 **简单易用** - 图形化界面，无需编程知识
- ⚡ **即时触发** - 毫秒级响应，精准执行
- 🔒 **隐私安全** - 数据本地存储，不上传云端
- 🎨 **中文界面** - 完全中文化，易于理解
- 📊 **触发记录** - 详细的执行日志，便于调试

---

## ✨ 功能特性

### 🔔 触发器 (Triggers)

| 触发器类型 | 描述 | 状态 |
|-----------|------|------|
| ⏰ 时间触发器 | 在特定时间触发，支持多日期选择 | ✅ 已实现 |
| 📱 短信触发器 | 收到特定号码/内容的短信时触发 | ✅ 已实现 |
| 📍 位置触发器 | 进入或离开特定位置时触发 | 🚧 待完善 |
| 🔋 电池触发器 | 电池电量变化或充电状态改变时触发 | 🚧 待完善 |
| 📶 WiFi触发器 | 连接或断开特定WiFi时触发 | 🚧 待完善 |
| 📡 蓝牙触发器 | 连接或断开蓝牙设备时触发 | 🚧 待完善 |
| 🖥️ 屏幕触发器 | 屏幕开关或解锁时触发 | 🚧 待完善 |

### ⚙️ 动作 (Actions)

| 动作类型 | 描述 | 状态 |
|---------|------|------|
| 📢 发送通知 | 显示自定义通知 | ✅ 已实现 |
| 📶 控制WiFi | 启用/禁用/切换WiFi | ✅ 已实现 |
| 📡 控制蓝牙 | 启用/禁用/切换蓝牙 | ✅ 已实现 |
| 🔊 调整音量 | 设置铃声/媒体/闹钟/通知音量 | ✅ 已实现 |
| 💡 调整亮度 | 设置屏幕亮度 | ✅ 已实现 |
| 📳 振动 | 触发设备振动（多种模式） | ✅ 已实现 |
| 🚀 启动应用 | 打开指定应用 | ✅ 已实现 |
| 🖥️ 控制屏幕 | 打开/关闭/锁定屏幕 | ✅ 已实现 |
| 📊 控制数据 | 启用/禁用/切换移动数据 | ✅ 已实现 |
| 📸 截图 | 截取屏幕 | ✅ 已实现 |
| 💬 发送短信 | 发送短信消息 | ✅ 已实现 |
| 🔔 播放警示音 | 播放系统提示音 | ✅ 已实现 |

### 🎯 约束条件 (Constraints)

- ⏰ 时间约束：仅在特定时间段执行
- 📅 日期约束：仅在特定日期执行
- 📍 位置约束：仅在特定位置执行
- 📶 WiFi约束：仅连接特定WiFi时执行
- 🔋 电源约束：仅特定电源状态下执行

---

## 🏗️ 技术架构

### 技术栈

| 类别 | 技术 |
|------|------|
| **开发语言** | Kotlin 1.9.22 |
| **最低版本** | Android 7.0 (API 24) |
| **目标版本** | Android 14 (API 34) |
| **架构模式** | MVVM |
| **UI框架** | Material Design Components |
| **数据存储** | SharedPreferences + Kotlinx Serialization |
| **异步处理** | Kotlin Coroutines + StateFlow |
| **视图绑定** | ViewBinding |
| **序列化** | Kotlinx Serialization + Parcelize |

---

## 📂 项目结构

```
app/
├── src/main/
│   ├── java/com/example/macrodroid/
│   │   ├── data/                    # 数据层
│   │   │   ├── Macro.kt             # 宏数据模型
│   │   │   ├── Trigger.kt           # 触发器模型
│   │   │   ├── Action.kt            # 动作模型
│   │   │   ├── Constraint.kt        # 约束条件模型
│   │   │   ├── TriggerLog.kt        # 触发日志模型
│   │   │   ├── MacroRepository.kt   # 宏数据仓库
│   │   │   └── TriggerLogRepository.kt  # 日志仓库
│   │   │
│   │   ├── domain/                  # 业务逻辑层
│   │   │   ├── MacroService.kt      # 核心服务
│   │   │   └── BootReceiver.kt      # 开机启动接收器
│   │   │
│   │   ├── trigger/                 # 触发器实现
│   │   │   ├── TimeTriggerReceiver.kt    # 时间触发器
│   │   │   ├── SmsTriggerReceiver.kt     # 短信触发器
│   │   │   ├── LocationTriggerReceiver.kt
│   │   │   └── BatteryTriggerReceiver.kt
│   │   │
│   │   ├── action/                  # 动作执行器
│   │   │   └── ActionExecutor.kt    # 统一动作执行
│   │   │
│   │   └── ui/                      # 用户界面
│   │       ├── MainActivity.kt      # 主界面
│   │       ├── MacroDetailActivity.kt    # 宏详情
│   │       ├── MacroAdapter.kt      # 宏列表适配器
│   │       ├── TriggerSelectionActivity.kt
│   │       ├── ActionSelectionActivity.kt
│   │       ├── TriggerLogActivity.kt     # 触发记录
│   │       ├── TriggerLogAdapter.kt
│   │       └── XiaomiPermissionGuideActivity.kt
│   │
│   ├── res/                         # 资源文件
│   │   ├── layout/                  # 布局文件
│   │   ├── values/                  # 字符串和样式
│   │   ├── drawable/                # 图标
│   │   └── menu/                    # 菜单
│   │
│   └── AndroidManifest.xml          # 清单文件
│
├── build.gradle                     # 构建配置
├── macrodroid.keystore              # 签名密钥
└── proguard-rules.pro              # 混淆规则
```

---

## 🚀 快速开始

### 前置要求

- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK API 24+ (Android 7.0+)
- Gradle 7.0+

### 安装步骤

#### 方法一：直接安装 APK

1. 下载最新的 APK 文件
2. 在 Android 设备上安装
3. 授予必要的权限
4. 开始使用

#### 方法二：从源码构建

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/automacro.git
cd automacro

# 2. 构建调试版 APK
./gradlew assembleDebug

# 3. 安装到设备
./gradlew installDebug

# 或者构建发布版 APK
./gradlew assembleRelease
```

#### 方法三：使用构建脚本（Windows）

```bash
# 双击运行
build-apk.bat
```

生成的 APK 位置：
- 调试版：`app\build\outputs\apk\debug\app-debug.apk`
- 发布版：`app\build\outputs\apk\release\app-release.apk`

---

## 📖 使用指南

### 创建第一个宏

1. **打开应用**
   - 启动应用后，自动启动后台服务
   - 通知栏显示"宏自动化服务运行中"

2. **创建新宏**
   - 点击右下角的 ➕ 按钮
   - 输入宏名称和描述

3. **添加触发器**
   - 点击"添加触发器"
   - 选择触发器类型（如：时间触发器）
   - 配置触发条件（如：每天 8:00）

4. **添加动作**
   - 点击"添加动作"
   - 选择动作类型（如：发送通知）
   - 配置动作参数（如：标题"早安提醒"）

5. **保存并启用**
   - 点击"保存宏"
   - 确保开关处于启用状态

### 示例场景

#### 场景1：工作日早间提醒

```
触发器：时间触发器
  - 时间：07:00
  - 日期：周一至周五

动作：
  1. 发送通知
     - 标题："起床时间到了！"
     - 内容："今天也要加油哦~"
  
  2. 振动
     - 模式：长震动
```

#### 场景2：验证码自动提醒

```
触发器：短信触发器
  - 号码：（留空，任意号码）
  - 关键词：验证码

动作：
  1. 发送通知
     - 标题："收到验证码"
     - 内容：（自定义）
  
  2. 振动
     - 模式：短震动
```

#### 场景3：省电模式

```
触发器：电池触发器
  - 类型：电量低于
  - 电量：20%

动作：
  1. 控制WiFi
     - 操作：禁用
  
  2. 调整亮度
     - 级别：30%
  
  3. 发送通知
     - 标题："低电量提醒"
     - 内容："已自动开启省电模式"
```

### 触发记录查看

1. 点击底部导航栏的"触发记录"
2. 查看所有宏的执行历史
3. 包括触发时间、宏名称、执行的动作
4. 可以清空历史记录

---

## 🔐 权限说明

### 必需权限

| 权限 | 用途 |
|------|------|
| `RECEIVE_SMS` | 接收短信触发器 |
| `READ_SMS` | 读取短信内容（用于短信触发器） |
| `RECEIVE_BOOT_COMPLETED` | 开机自启动服务 |
| `FOREGROUND_SERVICE` | 前台服务 |
| `SCHEDULE_EXACT_ALARM` | 精确闹钟调度 |
| `POST_NOTIFICATIONS` | 显示通知 |
| `VIBRATE` | 振动功能 |
| `ACCESS_FINE_LOCATION` | 位置触发器 |
| `BLUETOOTH_CONNECT` | 蓝牙控制 |
| `CHANGE_WIFI_STATE` | WiFi控制 |
| `MODIFY_AUDIO_SETTINGS` | 音量控制 |
| `WRITE_SETTINGS` | 系统设置修改 |

> ⚠️ **重要提示**：
> - **短信权限**：需要同时授予 `RECEIVE_SMS` 和 `READ_SMS` 权限才能使用短信触发器功能
> - **Android 6.0+**：需要运行时动态申请权限，应用会在使用相关功能时自动请求
> - **Android 13+**：需要额外授予通知权限

### 权限请求

应用会在首次使用相关功能时请求权限：
- **短信权限**：首次添加短信触发器时请求 `RECEIVE_SMS` 和 `READ_SMS`
- **位置权限**：首次添加位置触发器时请求
- **通知权限**：Android 13+ 首次启动时请求

### 小米设备特殊权限

小米/红米设备需要额外配置：

1. **自启动权限**
   - 设置 → 应用管理 → 自动化助手 → 权限 → 自启动

2. **后台弹出界面**
   - 设置 → 应用管理 → 自动化助手 → 权限 → 后台弹出界面

3. **省电策略**
   - 设置 → 应用管理 → 自动化助手 → 省电策略 → 无限制

4. **震动权限**（重要）
   - 设置 → 声音与震动 → 震动 → 确保全部开启
   - **关键**：设置 → 声音与震动 → 点按震动（触觉反馈）→ 开启
   - 检查勿扰模式设置

5. **短信权限**（重要）
   - 设置 → 应用管理 → 自动化助手 → 权限 → 短信 → 允许
   - 需要同时授予"接收短信"和"读取短信"权限

> 🔔 **小米设备震动特别说明**：
> 小米/MIUI系统需要在系统设置中**开启触觉反馈**才能正常使用震动功能：
> - 路径：设置 → 声音与震动 → 点按震动（或"触觉反馈"）
> - 确保该选项已开启，否则应用内的震动动作将不生效
> - 部分机型可能路径略有不同，请在"声音与震动"设置中查找相关选项

详细设置指南请查看应用内的"小米权限设置"页面。

---

## 🛠️ 开发指南

### 环境配置

```bash
# 检查 Gradle 版本
./gradlew --version

# 清理项目
./gradlew clean

# 构建项目
./gradlew build

# 运行测试
./gradlew test
```

### 代码规范

- 使用 Kotlin 官方代码风格
- 遵循 MVVM 架构模式
- 使用 ViewBinding 替代 findViewById
- 使用 Kotlin Coroutines 处理异步
- 使用 StateFlow 实现 MVVM 数据绑定

### 添加新的触发器

1. 在 `Trigger.kt` 中添加新的触发器类型
2. 在 `MacroRepository.kt` 中添加序列化/反序列化逻辑
3. 创建对应的 `XXXTriggerReceiver.kt`
4. 在 `TriggerSelectionActivity.kt` 中添加UI入口
5. 在 `MacroService.kt` 中注册和监听触发器

### 添加新的动作

1. 在 `Action.kt` 中添加新的动作类型
2. 在 `MacroRepository.kt` 中添加序列化/反序列化逻辑
3. 在 `ActionExecutor.kt` 中实现执行逻辑
4. 在 `ActionSelectionActivity.kt` 中添加UI入口

### 调试技巧

```bash
# 查看服务日志
adb logcat -s MacroService:D

# 查看动作执行日志
adb logcat -s ActionExecutor:D

# 查看触发器日志
adb logcat -s TimeTriggerReceiver:D SmsTriggerReceiver:D

# 清除应用数据
adb shell pm clear com.example.macrodroid
```

---

## ⚠️ 已知问题

### 高优先级

1. **部分小米设备震动不生效**
   - 原因：MIUI/澎湃OS的后台震动限制，需要在系统设置中开启"触觉反馈"
   - 解决：
     - 设置 → 声音与震动 → 点按震动（触觉反馈）→ 开启
     - 按照权限设置指南配置其他权限
   - 参考：应用内"小米权限设置"页面

2. **短信触发器权限问题**
   - 原因：需要同时授予 `RECEIVE_SMS` 和 `READ_SMS` 权限
   - 解决：在应用权限设置中确认两项权限都已授予
   - 注意：部分国产ROM可能需要额外权限设置

3. **Android 14 后台服务限制**
   - 原因：Android 14 对前台服务的限制
   - 解决：已配置 `foregroundServiceType="specialUse"`

### 中等优先级

3. **位置触发器精度问题**
   - 原因：GPS精度和耗电平衡
   - 状态：待优化

4. **WiFi/蓝牙触发器偶发失效**
   - 原因：系统广播延迟
   - 解决：建议使用其他触发器

### 低优先级

5. **截图功能兼容性**
   - 部分设备需要辅助功能权限
   - Android 11+ 推荐使用 MediaStore API

6. **移动数据控制**
   - 需要系统权限或 root
   - 部分设备可能不生效

---

## 🗓️ 更新日志

### v1.0.0 (2024-01-XX)

#### ✨ 新功能
- ✅ 完整的宏管理系统（创建、编辑、删除）
- ✅ 时间触发器（支持多日期选择）
- ✅ 短信触发器（号码和内容关键词过滤）
- ✅ 12种动作类型全部实现
- ✅ 触发记录查看功能
- ✅ 小米设备权限设置指南
- ✅ 完全中文化界面

#### 🔧 修复
- 🐛 修复开关状态不同步问题
- 🐛 修复触发器和动作无法添加的问题
- 🐛 修复 Parcelable 序列化问题
- 🐛 修复时间触发器调度问题

#### ⚡ 优化
- 🚀 优化后台服务稳定性
- 🚀 改进触发器匹配算法
- 🚀 优化UI响应速度
- 🚀 减少内存占用

---

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 如何贡献

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 报告问题

- 使用 GitHub Issues 报告 Bug
- 请提供详细的复现步骤
- 附上设备型号和系统版本

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

```
MIT License

Copyright (c) 2024 AutoMacro

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

---

## 🙏 致谢

感谢以下开源项目的启发：

- [MacroDroid](https://www.macrodroidforum.com/) - 自动化应用灵感来源
- [Tasker](https://tasker.joaoapps.com/) - Android 自动化先驱
- [Automate](https://llamalab.com/automate/) - 流程自动化设计

---

<div align="center">

**如果这个项目对您有帮助，请给一个 ⭐️ Star！**

Made with ❤️ by AutoMacro Team

</div>