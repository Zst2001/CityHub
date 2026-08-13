package com.cityhub.consultant.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * DashScope's Qwen streaming tool-call chunks repeat a complete tool-call ID.
 * The OpenAI-compatible model therefore must not concatenate the IDs.
 */
@Configuration
public class QwenModelConfig {

    private final String baseUrl;
    private final String apiKey;
    private final String modelName;

    public QwenModelConfig(
            @Value("${LLM_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
            @Value("${ALIYUNCS_API_KEY}") String apiKey,
            @Value("${LLM_MODEL_NAME:qwen3.7-flash}") String modelName) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    @Bean("openAiChatModel")
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .customParameters(Map.of("enable_thinking", false))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean("openAiStreamingChatModel")
    public OpenAiStreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .customParameters(Map.of("enable_thinking", false))
                .accumulateToolCallId(false)
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
