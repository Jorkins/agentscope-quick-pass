/*
 * AgentScope 快速通关系列 · 第 7 关 Demo
 * 发布版依赖口径：agentscope 2.0.3
 */
package io.agentscope.demo.ticket;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import io.agentscope.extensions.model.dashscope.formatter.DashScopeChatFormatter;
import java.time.Duration;
import java.util.List;

public class TicketModelRouterAgent {

    /** 方式 A：字符串 model id，由 ModelRegistry 自动解析（最轻量） */
    private static final ReActAgent STRING_ID_AGENT = ReActAgent.builder()
            .name("ticket-assistant-string-id")
            .sysPrompt("你是工单助手。请用中文回答，简洁友好。")
            .model("dashscope:qwen-plus")
            .maxIters(10)
            .build();

    /** 方式 B：显式 Model builder，自定义 API key、base URL、超时、生成参数 */
    private static final DashScopeChatModel EXPLICIT_MODEL =
            DashScopeChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-plus")
                    .stream(true)
                    .formatter(new DashScopeChatFormatter())
                    .defaultOptions(
                            GenerateOptions.builder()
                                    .temperature(0.2)
                                    .executionConfig(
                                            ExecutionConfig.builder()
                                                    .timeout(Duration.ofSeconds(30))
                                                    .build())
                                    .build())
                    .build();

    private static final ReActAgent EXPLICIT_MODEL_AGENT = ReActAgent.builder()
            .name("ticket-assistant-explicit")
            .sysPrompt("你是工单助手。请用中文回答，简洁友好。")
            .model(EXPLICIT_MODEL)
            .maxIters(10)
            .build();

    public static void main(String[] args) {
        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId("demo")
                .userId("alice")
                .build();

        // 启动时检查模型提供商可用性（不实际创建模型）
        boolean dashscopeAvailable = ModelRegistry.canResolve("dashscope:qwen-plus");
        System.out.println("dashscope:qwen-plus resolvable=" + dashscopeAvailable);

        if (!dashscopeAvailable) {
            System.out.println("跳过运行：未检测到 DashScope 扩展模块或 DASHSCOPE_API_KEY。");
            return;
        }

        try (ReActAgent agent = STRING_ID_AGENT) {
            Msg reply = agent.call(
                    List.of(new UserMessage("你好，订单 10086 的物流到哪了？")), ctx).block();
            System.out.println("Assistant (string-id): " + reply.getTextContent());
        }

        try (ReActAgent agent = EXPLICIT_MODEL_AGENT) {
            agent.streamEvents(
                    List.of(new UserMessage("帮我催一下这个订单")), ctx)
                    .doOnNext(event -> {
                        switch (event.getType()) {
                            case TEXT_BLOCK_DELTA -> System.out.print(
                                    ((TextBlockDeltaEvent) event).getDelta());
                            default -> {
                            }
                        }
                    })
                    .blockLast();
        }
    }
}
