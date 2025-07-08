package com.haohaoxuexi.springai.controller;

import com.haohaoxuexi.springai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class GameController {
    private final ChatClient gameChatClient;


    @RequestMapping(value = "/game", produces = "text/html;charset=utf-8")
    public Flux<String> chatStream(String prompt, String chatId) {
        return gameChatClient
                .prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
