package com.haohaoxuexi.springai.repository;

import java.util.List;
import java.util.Set;

public interface ChatHistoryRepository {


    /**
     * 保存会话记录
     *
     * @param type 业务类型 chat service pdf
     * @param chatId 会话ID
     */
    void save(String type, String chatId);

    /**
     * 获取回话ID列表
     *
     * @param type 业务类型
     * @return 会话ID列表
     */
    Set<String> getChatIds(String type);
}
