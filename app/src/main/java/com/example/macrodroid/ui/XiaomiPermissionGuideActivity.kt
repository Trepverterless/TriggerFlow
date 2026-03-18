package com.example.macrodroid.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.macrodroid.R
import com.example.macrodroid.databinding.ActivityXiaomiPermissionGuideBinding

class XiaomiPermissionGuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityXiaomiPermissionGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXiaomiPermissionGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "小米设备权限设置指南"

        binding.tvGuide.text = """
在小米/红米设备上，应用需要额外的权限才能正常接收短信触发和执行动作：

【必要权限设置】

1. 自启动权限
   • 路径：设置 → 应用管理 → 自动化助手 → 权限 → 自启动
   • 作用：允许应用在后台运行
   
2. 后台弹出界面权限  
   • 路径：设置 → 应用管理 → 自动化助手 → 权限 → 后台弹出界面
   • 作用：允许应用在后台显示界面

3. 省电策略设置
   • 路径：设置 → 应用管理 → 自动化助手 → 省电策略
   • 选择：无限制
   • 作用：避免系统杀掉后台服务

4. 通知权限
   • 路径：设置 → 应用管理 → 自动化助手 → 通知管理
   • 开启：允许通知

【震动权限 - 重要！】

小米/澎湃OS对震动有严格限制，请按以下步骤检查：

1. 检查系统震动设置
   • 路径：设置 → 声音与震动 → 震动
   • 确保"响铃时震动"和"静音时震动"都已开启
   • 检查震动强度设置，建议调至中等或以上

2. ⚠️ 开启触觉反馈（关键！）
   • 路径：设置 → 声音与震动 → 点按震动（或"触觉反馈"）
   • 必须开启此选项，否则应用内震动动作将不生效！
   • 这是小米/MIUI系统的特殊要求，很多用户忽略了这个设置

3. 检查勿扰模式
   • 路径：设置 → 声音与震动 → 勿扰模式
   • 如果开启，请关闭或设置为允许震动

4. 检查应用震动权限
   • 路径：设置 → 应用管理 → 自动化助手 → 权限
   • 确保没有禁用任何权限

5. 小米/澎湃OS特殊设置
   • 路径：设置 → 应用管理 → 自动化助手 → 其他权限
   • 开启所有可用权限

6. 测试手机震动硬件
   • 在拨号盘输入 *#*#6484#*#* 进入硬件测试
   • 选择"震动测试"确认硬件正常

【短信权限 - 重要！】

短信触发器需要以下权限才能正常工作：

1. 应用权限设置
   • 路径：设置 → 应用管理 → 自动化助手 → 权限 → 短信
   • 选择：允许（需要同时授予接收和读取权限）

2. 确认两项权限都已授予：
   • ✅ 接收短信 (RECEIVE_SMS)
   • ✅ 读取短信 (READ_SMS)
   
3. Android 6.0+ 需要动态授权
   • 首次使用短信触发器时，应用会自动请求权限
   • 如果拒绝了权限，需要手动到设置中开启

4. 权限验证方法：
   • 创建一个短信触发器
   • 发送测试短信
   • 查看"触发记录"是否有记录
   • 如果没有记录，说明权限未正确授予

【注意事项】

1. 在MIUI/澎湃OS系统中，即使获得权限，如果应用被强制停止，需要重新打开应用才能继续工作。

2. 建议在使用前先打开应用一次，确保服务正常运行。

3. 如果触发器仍然不工作，请尝试：
   - 重启手机
   - 重新打开应用
   - 检查是否有其他应用管理短信权限

【测试方法】

设置好权限后，可以通过以下步骤测试震动：

方法一：直接测试
1. 打开应用，确保服务正在运行（通知栏显示"服务运行中"）
2. 创建一个宏，添加任意触发器（如时间触发器）
3. 添加"震动"动作
4. 等待触发或手动触发
5. 观察手机是否震动

方法二：日志检查
1. 在电脑上连接手机，使用adb logcat
2. 过滤标签：adb logcat -s ActionExecutor:D
3. 触发震动动作
4. 查看日志输出，确认震动代码执行

【震动不生效的详细排查】

如果震动动作仍然不生效：

1. 确认服务正在运行
   • 通知栏应该显示"宏自动化服务运行中"
   • 如果没有，请打开应用重新启动服务

2. 确认触发器工作正常
   • 查看"触发记录"页面
   • 如果有记录，说明触发器工作正常，问题在震动
   • 如果没有记录，问题在触发器或权限

3. ⚠️ 再次检查触觉反馈设置
   • 这是最常见的遗漏！
   • 设置 → 声音与震动 → 点按震动 → 必须开启
   • 很多用户忽略了这个设置

4. 检查系统设置
   • 确保手机不在"静音模式"
   • 确保手机音量不是最低
   • 某些ROM在屏幕关闭时会限制后台震动

5. 尝试不同震动模式
   • 创建宏时选择"长震动"模式
   • 长震动更容易被感知

6. 重启应用和服务
   • 强制停止应用
   • 重新打开应用
   • 检查通知栏是否显示服务运行

【短信触发器不生效的排查】

如果短信触发器不工作：

1. 检查权限
   • 确认已授予"接收短信"和"读取短信"两项权限
   • 路径：设置 → 应用管理 → 自动化助手 → 权限

2. 检查默认应用设置
   • 路径：设置 → 应用管理 → 默认应用 → 短信
   • 某些ROM要求设置默认短信应用

3. 检查其他短信应用
   • 如果安装了其他短信应用，可能会拦截短信广播
   • 尝试暂时禁用其他短信应用

4. 查看触发记录
   • 发送测试短信
   • 查看应用内"触发记录"
   • 如果有记录，说明触发器工作正常

【已知的MIUI/澎湃OS限制】

1. 后台震动限制
   • 某些版本的MIUI/澎湃OS会限制后台应用的震动
   • 解决方法：确保应用在前台服务中运行，并开启系统触觉反馈

2. 省电策略
   • MIUI的省电策略可能会阻止后台震动
   • 解决方法：将应用加入省电白名单

3. 权限管理
   • MIUI的权限管理比原生Android更严格
   • 解决方法：手动授予所有可能需要的权限

如果仍然有问题，请检查：
- 应用是否在后台运行
- 通知栏是否显示服务运行中
- 触发记录是否有系统日志
- 手机型号和系统版本
- 是否开启了"触觉反馈"（震动功能必需）
- 短信权限是否完整授予（两项权限都要允许）
        """.trimIndent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}