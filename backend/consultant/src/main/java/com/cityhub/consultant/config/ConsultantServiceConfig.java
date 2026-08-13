package com.cityhub.consultant.config;

import com.cityhub.consultant.aiservice.ConsultantService;
import com.cityhub.consultant.tools.ActivityTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsultantServiceConfig {

    @Bean
    public ConsultantService consultantService(
            OpenAiChatModel openAiChatModel,
            OpenAiStreamingChatModel openAiStreamingChatModel,
            ChatMemoryProvider chatMemoryProvider,
            ActivityTool activityTool) {
        return AiServices.builder(ConsultantService.class)
                .chatModel(openAiChatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(activityTool)
                .build();
    }
}
