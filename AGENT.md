# 项目说明
本项目为基于 Neoforge1.21.1 开发的 Modern-industrialization 附属模组项目，使用的主要语言为 kotlin
本项目采用 vibecoding 此前的聊天记录存储在 .refer/.ignored/neoforge-1-21-1-modern-industrialization-deepseek-v4-flash.json 与 .refer/.ignored/neoforge-1-21-1-modern-industrialization-gpt-5.6.md

# .refer 文件夹说明
.refer/Create-mc1.21.1-dev为机械动力源码参考，主要为本模组中的多方块旋转动画部分提供源码参考
.refer/CTMI为新式MI主要风格贴图集，里面也包含了athena连接材质的配置，部分需要制作连接纹理的方块需要在这里参考格式和纹理结构
.refer/GregTech-Modern-1.21为格雷科技源码参考
.refer/Modern-Industrialization-1.21.x为主模组源码参考
.refer/Extended-Industrialization-1.21.1为官方附属源码参考
.refer/MI-Tweaks-1.21.1为官方调整工具源码参考
.refer/Mekanism-1.21.x为Mekanism部分纹理物品信息的源码参考
.refer/Industrialization-Overdrive-master为Industrialization-Overdrive模组源码参考
.refer/tesseract-neoforge-1.21.1为MI前置源码参考
.refer/miviewer多方块预览功能源码参考
.refer/LDLib2-1.21为我正在进行创新的UI库源码参考，Agent入口点为.refer/LDLib2-1.21/doc/ui/agent_guide.md，目前优先采用Kotlin Dsl

请勿参考MI原模组贴图纹理（Maven缓存）
请勿拆解MI原模组参考纹理（Maven缓存）

# Agent 验证与上下文约束

代码修改后的自动验证仅允许执行 `./gradlew build`（Windows 环境为 `./gradlew.bat build`），用于确认项目能够编译构建且不存在语法错误。禁止 Agent 启动、操作或以任何方式执行 `runClient`；游戏内运行与灰度测试由用户自行完成。

若任务因缓存清理或上下文压缩而续接，续接提示必须明文要求 Agent 优先完整阅读本 `AGENT.md`，再继续分析或修改项目。
