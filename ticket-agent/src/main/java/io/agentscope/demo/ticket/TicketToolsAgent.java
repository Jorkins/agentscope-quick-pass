/*
 * AgentScope 快速通关系列 · 第 4 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import java.util.List;

public class TicketToolsAgent {

    private static final ReActAgent AGENT;
    private static final Toolkit TOOLKIT = new Toolkit();

    static {
        // 创建工单操作分组（初始非激活）
        TOOLKIT.createToolGroup("ticket_ops", "工单核心操作", false);
        TOOLKIT.registration().tool(new TicketTools()).group("ticket_ops").apply();

        AGENT = ReActAgent.builder()
                .name("ticket-assistant")
                .sysPrompt("你是工单助手。请用中文回答，简洁友好。需要查询或修改订单时，优先使用 ticket_ops 分组的工具。")
                .model("dashscope:qwen-plus")
                .toolkit(TOOLKIT)
                .enableMetaTool(true)
                .maxIters(10)
                .build();
    }

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo")
                .userId("alice")
                .build();

        try (ReActAgent agent = AGENT) {
            agent.streamEvents(
                    List.of(new UserMessage("帮我查一下订单 10086 的物流到哪了")), ctx)
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

    /** 工单工具：查订单、查物流、改工单状态 */
    public static class TicketTools {

        @Tool(
                name = "query_order",
                description = "查询订单详情，支持订单号查询",
                readOnly = true,
                concurrencySafe = true)
        public String queryOrder(
                @ToolParam(name = "orderId", description = "订单号，如 10086") String orderId) {
            return "订单 " + orderId + " 状态：已发货，预计 3 天内送达。";
        }

        @Tool(
                name = "query_logistics",
                description = "查询订单物流信息",
                readOnly = true,
                concurrencySafe = true)
        public String queryLogistics(
                @ToolParam(name = "orderId", description = "订单号，如 10086") String orderId) {
            return "订单 " + orderId + " 物流：已到达目的地派件站，正在派送中。";
        }

        @Tool(
                name = "update_order_status",
                description = "修改订单状态（需审批）",
                readOnly = false,
                concurrencySafe = false)
        public String updateOrderStatus(
                @ToolParam(name = "orderId", description = "订单号") String orderId,
                @ToolParam(name = "status", description = "目标状态，如 已完成/已取消") String status) {
            return "订单 " + orderId + " 已申请变更为 " + status + "，等待审批。";
        }
    }
}
