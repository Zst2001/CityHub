package com.cityhub.consultant.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,//手动装配
        chatModel = "openAiChatModel",//指定模型
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        tools = "activityTool"
)
//@AiService
public interface ConsultantService {
    @SystemMessage(fromResource = "system.txt")
    public Flux<String> chat(/*@V("msg")*/@MemoryId String memoryId, @UserMessage String message);
}
