/*
 * AgentScope 快速通关系列 · 第 3 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentStartEvent;
import io.agentscope.core.event.ModelCallStartEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import java.util.List;

public class StreamingTicketAgent {

    // 启动时配置一次；请求期间只 build()
    private static final ReActAgent.Builder AGENT_BUILDER = createBuilder();

    private static ReActAgent.Builder createBuilder() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new OrderTools());
        return ReActAgent.builder()
                .name("ticket-stream-agent")
                .sysPrompt("你是工单助手。请用中文回答，简洁友好。查询订单物流必须用工具。")
                .model("dashscope:qwen-plus")
                .toolkit(toolkit);
    }

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("stream-demo")
                .userId("alice")
                .build();

        // 事件是冷流：订阅（blockLast）发生的那一刻才真正执行 Agent
        try (ReActAgent agent = AGENT_BUILDER.build()) {
            agent.streamEvents(List.of(new UserMessage("帮我查一下订单 10086 的物流状态")), ctx)
                    .doOnNext(StreamingTicketAgent::handleEvent)
                    .blockLast();
        }
    }

    // 官方推荐 instanceof 模式：编译期直接拿到类型化字段，无需强转
    private static void handleEvent(AgentEvent event) {
        if (event instanceof AgentStartEvent e) {
            System.out.printf("[AGENT_START] replyId=%s%n", e.getReplyId());
        } else if (event instanceof ModelCallStartEvent e) {
            System.out.printf("[MODEL_CALL_START] replyId=%s%n", e.getReplyId());
        } else if (event instanceof TextBlockDeltaEvent e) {
            System.out.print(e.getDelta()); // 打字机效果
        } else if (event instanceof ToolCallStartEvent e) {
            System.out.printf("%n[TOOL_CALL] tool=%s callId=%s%n",
                    e.getToolCallName(), e.getToolCallId());
        } else if (event instanceof ToolResultEndEvent e) {
            System.out.printf("[TOOL_RESULT_END] callId=%s state=%s%n",
                    e.getToolCallId(), e.getState());
        } else if (event instanceof AgentEndEvent e) {
            System.out.printf("%n[AGENT_END] replyId=%s%n", e.getReplyId());
        }
        // 其余事件按需处理；生产环境建议至少记录 event.getType() 便于排查
    }

    public static class OrderTools {

        @Tool(description = "查询订单的物流状态")
        public String queryOrder(
                @ToolParam(name = "orderId", description = "订单号") String orderId) {
            return "订单 " + orderId + " 已发货，预计明天送达。";
        }
    }
}
