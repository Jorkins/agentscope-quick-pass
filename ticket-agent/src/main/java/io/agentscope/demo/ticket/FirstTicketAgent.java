/*
 * AgentScope 快速通关系列 · 第 1 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import java.nio.file.Paths;

public class FirstTicketAgent {

    // 应用启动时配置一次；请求期间只调用 build()
    private static final HarnessAgent.Builder AGENT_BUILDER = HarnessAgent.builder()
            .name("ticket-assistant")
            .agentId("ticket-assistant")
            .sysPrompt("你是工单助手。请用中文回答，简洁友好。")
            .model("dashscope:qwen-plus")
            .workspace(Paths.get(".agentscope/workspace"));

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo")
                .userId("alice")
                .build();

        // 第一轮：普通问答
        try (HarnessAgent agent = AGENT_BUILDER.build()) {
            agent.call(new UserMessage("你好，介绍一下你自己"), ctx).block();
        }

        // 第二轮：新实例，但同一个 sessionId，会续上上一轮对话
        try (HarnessAgent agent = AGENT_BUILDER.build()) {
            agent.streamEvents(new UserMessage("我刚才问了什么？"), ctx)
                    .doOnNext(event -> {
                        switch (event.getType()) {
                            case TEXT_BLOCK_DELTA -> System.out.print(
                                    ((TextBlockDeltaEvent) event).getDelta());
                            case TOOL_CALL_START -> System.out.println(
                                    "\n[tool] " + ((ToolCallStartEvent) event).getToolCallName());
                            default -> {
                            }
                        }
                    })
                    .blockLast();
        }
    }
}
