# AgentScope 快速通关系列

> Java 后端工程师的 Agent 生产落地闯关手册。每一关一个可运行 Demo，闯完直接上生产。

本仓库是公众号系列《AgentScope 快速通关系列》的配套代码与文章源文件。

## 目录结构

```text
articles/
  level-00/   第 0 关：选型、版本口径与系列地图
  level-01/   第 1 关：10 分钟跑通 Hello AgentScope

ticket-agent/   贯穿实战项目：智能工单助手 TicketAgent
```

## 系列地图（18 关）

| 篇章 | 关卡 | 主题 |
|---|---|---|
| 入门篇 | 0 ~ 4 | 从零到能调用工具 |
| 进阶篇 | 5 ~ 12 | 工程化与安全 |
| 生产篇 | 13 ~ 17 | 部署、集成与复盘 |

详细关卡表见 [构思设计](../agentscope-quick-pass-series-design.md)（上级目录）。

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

