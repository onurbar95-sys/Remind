# 驻留待办 (FocusReminder) 📱

> **一款专注防拖延、强制确认的 Android 原生强提醒待办应用。**  
> 每日定时发送**锁定常驻通知（用户无法手动划掉）**，只有进入 App 并在应用内**停留满可调时间**，通知才会自动消除！

---

## 🌟 核心设计理念

很多人手机里的待办提醒之所以无效，是因为：
1. **一划即忘**：通知一响，顺手右划清除，转头就忘。
2. **假装看过**：点进应用一秒退出来，根本没有认真规划和阅读。

**「驻留待办」彻底改变了这个习惯：**
- 🔒 **常驻通知锁定**：基于 Android 系统级 `setOngoing(true)` 特性，通知栏锁定，无法手动划掉，也无法被“全部清除”清掉。
- ⏳ **应用内停留消除机制**：点击通知进入 App 后，顶部触发停留倒计时（时长在设置中自由调节，如 30 秒、1 分钟、3 分钟、5 分钟）。
- ⏸️ **后台暂停防作弊**：若倒计时未完成就切出应用或锁屏，倒计时立即**自动暂停**；只有回到 App 停留满设定时长，系统才主动注销通知并震动祝贺！

---

## 🚀 核心功能一览

1. **待办事项管理 (To-Do Management)**
   - 添加、编辑、删除待办事项。
   - 支持设置任务优先级（常规、重点、紧急重要）。
   - 快捷勾选打卡与一键清空已完成事项。
   - 采用 Android 官方推荐的 Room 本地数据库，离线可用、数据永不丢失。

2. **系统级强提醒常驻通知 (Ongoing Notification)**
   - 每日自定义定时触发（如早上 08:30）。
   - 聚合展示今日未完成的核心任务。
   - 锁屏和状态栏全局可见。

3. **停留倒计时与生命周期监听 (Dwell Countdown)**
   - 醒目大字钟表与动态进度条。
   - 基于 Android 架构组件 `LifecycleObserver`，精准监听前台/后台状态。
   - 倒计时归零后，触发震动提示并自动调用 `NotificationManager.cancel()` 消除常驻通知。

4. **高度可调的偏好设置 (Customizable Settings)**
   - **停留消除时长调节**：10 秒 ～ 10 分钟任意滑动调节，附带 15秒、30秒、1分钟、2分钟、3分钟、5分钟快捷档位。
   - **每日提醒时间设定**：内置 24 小时制时间选择器。
   - **一键测试体验**：内置“立即发送测试通知”按钮，不用等到明天，随时可以体验常驻通知与停留消除全流程。
   - **紧急手动清除**：提供手动消除兜底按键。

---

## 🛠️ 技术架构

- **开发语言**：Kotlin 2.0+
- **UI 框架**：Jetpack Compose + Material 3 (动态配色、自适应暗色模式)
- **架构模式**：MVVM + Clean Architecture (ViewModel + StateFlow + Repository)
- **本地存储**：Room Database + KSP + SharedPreferences / DataStore
- **通知与后台调度**：
  - `NotificationCompat.Builder.setOngoing(true)`
  - `AlarmManager.setAlarmClock()` (即使在 Doze 深度休眠下也能准时唤醒)
  - `BroadcastReceiver` + `RECEIVE_BOOT_COMPLETED` (开机自启恢复闹钟)

---

## 📦 如何打包成 APK 安装到手机

### 方式一：GitHub Actions 自动云端打包（⭐ 最省心，无需本地配置环境）

本项目已内置完整的自动化构建工作流：`.github/workflows/build-apk.yml`。

1. 将当前工程上传/推送到你的 GitHub 仓库（无论是公开还是私有仓库）：
   ```bash
   git init
   git add .
   git commit -m "feat: initial commit of FocusReminder app"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/<你的仓库名>.git
   git push -u origin main
   ```
2. 打开 GitHub 仓库页面的 **Actions** 标签栏。
3. GitHub 将自动启动云端 Ubuntu 服务器进行编译构建（约 2~3 分钟）。
4. 构建完成后，在 **Artifacts (构建产物)** 下直接下载 `FocusReminder-Debug-APK` 解压即可得到可直接安装到安卓手机的 `.apk` 文件！

---

### 方式二：使用 Android Studio 本地运行/打包

1. 电脑下载并安装 [Android Studio](https://developer.android.google.cn/studio)。
2. 打开 Android Studio，点击 **Open**，选择本项目根目录：`e:\files\workspace\app`。
3. 等待 Gradle 同步完成后：
   - **USB 真机调试**：开启手机开发者选项与 USB 调试，用数据线连接电脑，点击顶部绿色三角形 **Run** 即可直接安装到手机。
   - **导出 APK 文件**：点击菜单栏 **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**，完成后点击右下角 `locate` 即可拿到 `app-debug.apk`。

---

### 方式三：命令行一键编译

在项目根目录下打开终端（PowerShell），执行：
```powershell
.\gradlew.bat assembleDebug
```
生成的 APK 路径为：
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 安卓各品牌手机保活与权限设置建议

为了确保每日定时强提醒能 100% 准时弹出，建议在手机上做如下设置：

| 手机品牌 | 推荐设置路径 |
| :--- | :--- |
| **小米 / Redmi (HyperOS / MIUI)** | 设置 -> 应用设置 -> 应用管理 -> 驻留待办 -> 开启「自启动」；省电策略选择「无限制」；通知管理开启「悬浮通知」和「锁屏通知」。 |
| **华为 / 荣耀 (HarmonyOS / MagicOS)** | 设置 -> 应用 -> 应用启动管理 -> 找到驻留待办 -> 关闭自动管理，开启「允许自启动」、「允许后台活动」。 |
| **OPPO / 一加 / realme (ColorOS)** | 设置 -> 应用 -> 应用自启动 -> 允许驻留待办；电池 -> 后台耗电管理 -> 允许后台高耗电。 |
| **vivo / iQOO (OriginOS)** | 设置 -> 电池 -> 后台高耗电 -> 允许驻留待办后台高耗电；权限管理 -> 自启动 -> 开启。 |

---

## 📄 许可证
本项目采用 MIT 许可证，自由开源使用。
