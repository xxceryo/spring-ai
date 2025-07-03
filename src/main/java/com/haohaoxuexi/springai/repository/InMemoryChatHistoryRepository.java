package com.haohaoxuexi.springai.repository;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {

    // 保存在内存中 可以优化保存到数据库
    private final Map<String, Set<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String chatId) {
        chatHistory.computeIfAbsent(type, k -> new HashSet<>()).add(chatId);
    }

    @Override
    public Set<String> getChatIds(String type) {
        return chatHistory.getOrDefault(type, Collections.emptySet());
    }
}
