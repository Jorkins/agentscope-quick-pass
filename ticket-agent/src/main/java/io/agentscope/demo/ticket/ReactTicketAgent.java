/*
 * AgentScope 快速通关系列 · 第 2 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import java.util.List;

public class ReactTicketAgent {

    // 应用启动时配置一次；请求期间只调用 build()
    private static final ReActAgent.Builder AGENT_BUILDER = ReActAgent.builder()
            .name("ticket-assistant")
            .sysPrompt("你是工单助手。请用中文回答，简洁友好。")
            .model("dashscope:qwen-plus")
            .maxIters(10);

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo")
                .userId("alice")
                .build();

        // 第一轮：普通问答，阻塞等待最终结果
        try (ReActAgent agent = AGENT_BUILDER.build()) {
            Msg reply = agent.call(List.of(new UserMessage("你好，订单 10086 的物流到哪了？")), ctx).block();
            System.out.println("Assistant: " + reply.getContent());
        }

        // 第二轮：新实例，流式事件，看实时推理与工具调用过程
        try (ReActAgent agent = AGENT_BUILDER.build()) {
            agent.streamEvents(List.of(new UserMessage("帮我催一下这个订单")), ctx)
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
