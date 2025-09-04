package com.lwx.lwxmagiccodebackend.ai;

import com.lwx.lwxmagiccodebackend.ai.model.HtmlCodeResult;
import com.lwx.lwxmagiccodebackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiAppNameGeneratorService {
        /**
         * 生成 app名称 代码
         *
         * @param userMessage 用户消息
         * @return 生成的代码结果
         */
        @SystemMessage(fromResource = "prompt/appnamegen-system-prompt.txt")
        String generateAppName(String userMessage);

}

