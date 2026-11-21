# AgentScope 快速通关系列

> Java 后端工程师与架构师的 Agent 生产落地闯关手册。每一关在同一个工程里累加实现，闯完直接上生产。

公众号系列《AgentScope 快速通关系列》的配套源码仓库。

## 工程约定

- **一个工程、一个分支**：全程只有一个 `ticket-agent` 工程，只用 `main` 分支。
- **逐关累加**：每关在上一关基础上增加能力，不打 tag、不建分支、不删除历史。
- 文章与源码一一对应，当前已实现：

| 关卡 | 文章 | 工程能力 |
|---|---|---|
| 第 0 关 | 出生点：选型、版本口径与系列地图 | 认知准备 |
| 第 1 关 | 10 分钟跑通 Hello AgentScope | 共享 Builder、`RuntimeContext`、`call` / `streamEvents` |

## 目录结构

```text
ticket-agent/
  pom.xml
  src/main/java/io/agentscope/demo/ticket/FirstTicketAgent.java
```

## 运行

环境：JDK 17+、Maven 3.9+，并设置 DashScope API Key。

```bash
export DASHSCOPE_API_KEY=your_api_key
cd ticket-agent
mvn -q clean compile
mvn exec:java
```

Windows PowerShell：

```powershell
$env:DASHSCOPE_API_KEY="your_api_key"
cd ticket-agent
mvn exec:java
```

## 系列地图（18 关）

| 篇章 | 关卡 | 主题 |
|---|---|---|
| 入门篇 | 0 ~ 4 | 从零到能调用工具 |
| 进阶篇 | 5 ~ 12 | 工程化与安全 |
| 生产篇 | 13 ~ 17 | 部署、集成与复盘 |

## 版本口径（重要）

| 口径 | 版本 |
|---|---:|
| README 快速开始 | 2.0.3 |
| 稳定文档 `docs/v2/` | 2.0.4 |
| 源码 / dev 文档 | 2.1.0-SNAPSHOT |

本系列默认使用发布版 `2.0.3`；涉及 Durable、JEV 等 dev 能力时会显式标注。

## 红线

- 仓库根 `SKILL.md` 已过时（1.0.12 / v1 Hook），不作为 2.x 依据。
- 事件数量以 `AgentEventType` 为准：32 种。
- `StreamableAgent.stream()` 已废弃，使用 `streamEvents()`。
- Durable 为 dev/research，非 GA。

## 关注

微信公众号 **码上栈记**：持续分享 AI 工程与 Java 后端的实战内容。

## License

源码仅供系列学习使用；AgentScope 框架为 Apache-2.0。
