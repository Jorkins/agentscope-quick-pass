/*
 * AgentScope 快速通关系列 · 第 6 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AllToolsDeniedEvent;
import io.agentscope.core.event.RequestStopEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import java.util.List;
import java.util.function.Function;
import reactor.core.publisher.Flux;

public class TicketMiddlewareAgent {

    private static final ReActAgent AGENT;
    private static final Toolkit TOOLKIT = new Toolkit();

    static {
        TOOLKIT.createToolGroup("ticket_ops", "工单核心操作", false);
        TOOLKIT.registration().tool(new TicketTools()).group("ticket_ops").apply();

        // 中间件：请求日志中间件（order=2，最外层）+ 全部工具被拒绝时自动停止（order=1，内层）
        AGENT = ReActAgent.builder()
                .name("ticket-assistant")
                .sysPrompt("你是工单助手。请用中文回答，简洁友好。需要查询或修改订单时，优先使用 ticket_ops 分组的工具。")
                .model("dashscope:qwen-plus")
                .toolkit(TOOLKIT)
                .middlewares(List.of(
                        new RequestLoggingMiddleware(2),
                        new StopOnAllToolsDeniedMiddleware(1)))
                .maxIters(10)
                .build();
    }

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo")
                .userId("alice")
                .build();

        try (ReActAgent agent = AGENT) {
            // 查询订单，命中 ALLOW 规则，正常执行，中间件记录日志
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

    /** 请求日志中间件：onAgent 包裹整次 reply，onActing 包裹每次工具执行 */
    public static class RequestLoggingMiddleware implements MiddlewareBase {

        private final int order;

        public RequestLoggingMiddleware(int order) {
            this.order = order;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public Flux<AgentEvent> onAgent(
                Agent agent, RuntimeContext ctx, AgentInput input,
                Function<AgentInput, Flux<AgentEvent>> next) {
            System.out.printf(
                    "[log] onAgent start user=%s session=%s agent=%s msgs=%d%n",
                    ctx.getUserId(), ctx.getSessionId(), agent.getName(),
                    input.msgs() == null ? 0 : input.msgs().size());
            return next.apply(input)
                    .doOnComplete(() -> System.out.println("[log] onAgent end"));
        }

        @Override
        public Flux<AgentEvent> onActing(
                Agent agent, RuntimeContext ctx, ActingInput input,
                Function<ActingInput, Flux<AgentEvent>> next) {
            input.toolCalls().forEach(t ->
                    System.out.printf("[log] onActing tool=%s input=%s%n",
                            t.getName(), t.getInput()));
            return next.apply(input)
                    .doOnComplete(() -> System.out.println("[log] onActing end"));
        }
    }

    /** 全部工具被拒绝时自动停止 Agent */
    public static class StopOnAllToolsDeniedMiddleware implements MiddlewareBase {

        private final int order;

        public StopOnAllToolsDeniedMiddleware(int order) {
            this.order = order;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public Flux<AgentEvent> onAgent(
                Agent agent, RuntimeContext ctx, AgentInput input,
                Function<AgentInput, Flux<AgentEvent>> next) {
            return next.apply(input)
                    .doOnNext(event -> {
                        if (event instanceof AllToolsDeniedEvent) {
                            System.out.println("[stop] All tools denied, issuing RequestStopEvent");
                        }
                    });
        }
    }

    /** 工单工具 */
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
    }
}
