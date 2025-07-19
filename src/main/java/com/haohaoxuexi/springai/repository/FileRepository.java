package com.haohaoxuexi.springai.repository;

import org.springframework.core.io.Resource;

public interface FileRepository {
    /**
     * 保存文件,还要记录chatId与文件的映射关系
     * @param chatId 会话id
     * @param resource 文件
     * @return 传成功，返回true； 否则返回false
     */
    Boolean save(String chatId, Resource resource);

    /**
     * 根据会话id获取文件
     * @param chatId 会话id
     * @return 找到的文件
     */
    Resource load(String chatId);
}
