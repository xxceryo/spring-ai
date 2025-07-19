package com.haohaoxuexi.springai.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPdfFileRepository implements FileRepository {

    private final VectorStore vectorStore;

    // 会话id与文件名的对应关系
    private final Properties properties = new Properties();

    @Override
    public Boolean save(String chatId, Resource resource) {
        // 保存 这里选择本地磁盘
        String filename = resource.getFilename();
        File file = new File(Objects.requireNonNull(filename));
        if (!file.exists()) {
            try {
                Files.copy(resource.getInputStream(), file.toPath());
            } catch (Exception e) {
                log.error("文件保存错误 错误信息 {}", e.getMessage());
                return false;
            }
        }
        properties.setProperty(chatId, filename);
        return true;
    }

    @Override
    public Resource load(String chatId) {
        return new FileSystemResource(Objects.requireNonNull(properties.getProperty(chatId)));
    }

    @PostConstruct
    public void init() {
        FileSystemResource pdfResource = new FileSystemResource("chat-pdf.properties");
        if (pdfResource.exists()) {
            try {
                properties.load(new BufferedReader(new InputStreamReader(pdfResource.getInputStream(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileSystemResource vectorResource = new FileSystemResource("chat-pdf.json");
        if (vectorResource.exists()) {
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.load(vectorResource);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            properties.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.save(new File("chat-pdf.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
