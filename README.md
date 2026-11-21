# AgentScope 2.0 Java 快速通关系列

> Java 后端工程师的 Agent 生产落地闯关手册。每一关在同一个工程里累加实现，闯完直接上生产。

本仓库是公众号系列《AgentScope 快速通关系列》的配套源码仓库。

## 工程约定

- **一个工程、一个分支**：全程只有一个 `ticket-agent` 工程，只用 `main` 分支。
- **逐关累加**：每关在上一关基础上增加能力，不打 tag、不建分支。
- 当前已实现到第 1 关：共享 Builder、`RuntimeContext`、`call` / `streamEvents`。

## 目录结构

```text
ticket-agent/
  pom.xml
  src/main/java/io/agentscope/demo/ticket/FirstTicketAgent.java
  docs/images/   公众号引导图
```

## 系列地图（18 关）

| 篇章 | 关卡 | 主题 |
|---|---|---|
| 入门篇 | 0 ~ 4 | 从零到能调用工具 |
| 进阶篇 | 5 ~ 12 | 工程化与安全 |
| 生产篇 | 13 ~ 17 | 部署、集成与复盘 |

### 完整关卡表（18 关）

| 关卡 | 标题 | 核心内容 |
|---|---|---|
| 0 | 出生点：选型、版本口径与系列地图 | 定位、三大 Key Design、2.x 版本口径 |
| 1 | 10 分钟跑通 Hello AgentScope | Maven 依赖、Builder、`call`/`streamEvents`、`RuntimeContext` |
| 2 | ReAct 内核与消息模型 | `ReActAgent`、`Msg`、`ContentBlock`、角色校验、`AgentRun` |
| 3 | 32 种事件与流式渲染 | `AgentEventType` 32 事件、start/delta/end、`streamEvents`、SSE/WebFlux |
| 4 | 工具、ToolGroup 与 MCP | `@Tool`、`Toolkit`、`ToolGroup`、MCP stdio/SSE/Streamable HTTP |
| 5 | 权限与 HITL | ALLOW/ASK/DENY、五模式、审批中断恢复 |
| 6 | Middleware 与可观测 | 六/七阶段 hook、洋葱模式、`OtelTracingMiddleware` |
| 7 | 模型扩展与路由 | 5 家 provider、`ModelRegistry`、重试与回退 |
| 8 | 无状态 Agent 与多租户会话 | `(userId, sessionId)` 寻址、`AgentState`、Redis 多副本 |
| 9 | Harness 双层架构总览 | ReAct 小内核 + Harness 工程外壳、能力叠加、共享对象 |
| 10 | Workspace：记忆与技能 | `AGENTS.md`、`MEMORY.md`、日流水账、Skill 自沉淀 |
| 11 | 上下文压缩与状态持久化 | compaction、超大结果落盘、checkpoint、存储后端 |
| 12 | 子智能体编排 | `agent_spawn`/`agent_send`、同步/后台、事件转发 |
| 13 | Plan Mode + Durable（前瞻） | 读写分离计划态；Durable 的 Session/Thread/Turn/Run/Step，**明确标注 dev/research** |
| 14 | 沙箱与安全执行 | 本地/Docker/K8s/E2B、文件与网络隔离、快照恢复 |
| 15 | RAG、协议与 Channel | v2 应用层 RAG、A2A/AG-UI、钉钉/飞书/企微/微信 Channel 合规 |
| 16 | Spring Boot 与分布式生产部署 | 14 个 starter、多租户隔离、跨副本恢复、状态后端 |
| 17 | 评测、复盘与通关地图 | OTel 回顾、JEV（dev 前瞻）、全系列串讲、FAQ |

## 版本口径（重要）

| 口径 | 版本 |
|---|---:|
| README 快速开始 | 2.0.3 |
| 稳定文档 `docs/v2/` | 2.0.4 |
| 源码 / dev 文档 | 2.1.0-SNAPSHOT |

本系列文章默认使用发布版 `2.0.3`；涉及 Durable、JEV 等 dev 能力时会显式标注。

## 红线

- 仓库根 `SKILL.md` 已过时（1.0.12 / v1 Hook），不作为 2.x 依据。
- 事件数量以 `AgentEventType` 为准：32 种。
- `StreamableAgent.stream()` 已废弃，使用 `streamEvents()`。
- Durable 为 dev/research，非 GA。

## License

文章与 Demo 代码仅供系列学习使用；AgentScope 框架为 Apache-2.0。

## 关注作者


如果你对 AI 及本项目感兴趣，欢迎关注我的微信公众号 **码上栈记**，将持续带来更多高质量文章和干货。

<p align="center">
  <img src="ticket-agent/docs/images/wechat-follow-banner@2x.png" alt="微信搜一搜：码上栈记" width="480">
</p>

