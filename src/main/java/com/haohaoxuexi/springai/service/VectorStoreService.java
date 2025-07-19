package com.haohaoxuexi.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

public interface VectorStoreService {
    /**
     * 保存文件到向量数据库
     * @param resource 文件
     */
    void writeToVectorStore(Resource resource);
}
