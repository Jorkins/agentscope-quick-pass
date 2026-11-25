/*
 * AgentScope 快速通关系列 · 第 5 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.GenerateReason;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketPermissionAgent {

    private static final ReActAgent AGENT;
    private static final Toolkit TOOLKIT = new Toolkit();

    static {
        TOOLKIT.createToolGroup("ticket_ops", "工单核心操作", false);
        TOOLKIT.registration().tool(new TicketTools()).group("ticket_ops").apply();

        // 权限：查询自动放行，改状态需审批
        PermissionContextState permCtx =
                PermissionContextState.builder()
                        .mode(PermissionMode.DEFAULT)
                        .addAllowRule(
                                "query_order",
                                new PermissionRule("query_order", null, PermissionBehavior.ALLOW, "policy"))
                        .addAllowRule(
                                "query_logistics",
                                new PermissionRule("query_logistics", null, PermissionBehavior.ALLOW, "policy"))
                        .addAskRule(
                                "update_order_status",
                                new PermissionRule("update_order_status", null, PermissionBehavior.ASK, "policy"))
                        .build();

        AGENT = ReActAgent.builder()
                .name("ticket-assistant")
                .sysPrompt("你是工单助手。请用中文回答，简洁友好。需要查询或修改订单时，优先使用 ticket_ops 分组的工具。")
                .model("dashscope:qwen-plus")
                .toolkit(TOOLKIT)
                .permissionContext(permCtx)
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
            // 第一轮：改工单，会触发 ASK → 中断
            Msg result = agent.call(
                    List.of(new UserMessage("把订单 10086 改为已完成")), ctx).block();

            if (result != null && result.getGenerateReason() == GenerateReason.PERMISSION_ASKING) {
                List<ToolUseBlock> askingTools = result.getContent().stream()
                        .filter(ToolUseBlock.class::isInstance)
                        .map(ToolUseBlock.class::cast)
                        .filter(t -> t.getState() == ToolCallState.ASKING)
                        .toList();

                System.out.println("[HITL] 检测到待审批工具：" + askingTools.size());
                askingTools.forEach(t ->
                        System.out.println("       " + t.getName() + " -> " + t.getInput()));

                // Demo 自动模拟"批准"（生产中应由真实用户交互决定）
                List<ConfirmResult> confirmResults = askingTools.stream()
                        .map(t -> new ConfirmResult(true, t))
                        .toList();

                Map<String, Object> meta = new HashMap<>();
                meta.put(Msg.METADATA_CONFIRM_RESULTS, confirmResults);
                Msg resumeMsg = Msg.builder()
                        .name("user")
                        .role(MsgRole.USER)
                        .textContent("approved")
                        .metadata(meta)
                        .build();

                Msg finalResult = agent.call(List.of(resumeMsg), ctx).block();
                System.out.println("Assistant: " + finalResult.getContent());
            } else {
                System.out.println("Assistant: " + result.getContent());
            }
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
            return "订单 " + orderId + " 已变更为 " + status + "。";
        }
    }
}
