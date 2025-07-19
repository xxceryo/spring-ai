package com.haohaoxuexi.springai.controller;

import com.haohaoxuexi.springai.entity.vo.Result;
import com.haohaoxuexi.springai.repository.ChatHistoryRepository;
import com.haohaoxuexi.springai.repository.FileRepository;
import com.haohaoxuexi.springai.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RestController
@RequestMapping("/ai/file")
@RequiredArgsConstructor
public class FileController {

    private final FileRepository fileRepository;

    private final VectorStoreService vectorStoreService;

    private final ChatClient fileChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/pdf/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        // 1.找到会话文件
        Resource file = fileRepository.load(chatId);

        if (!file.exists()) {
            // 文件不存在
            throw new RuntimeException("会话文件不存在！");
        }
        // 2.保存会话id
        chatHistoryRepository.save("pdf", chatId);

        // 3.请求LLM
        return fileChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .advisors(a -> a.param(
                        FILTER_EXPRESSION,
                        // 找到文件名对应的向量数据
                        "file_name == '" + file.getFilename() + "'"))
                .stream()
                .content();
    }

    /**
     * 文件上传
     */
    @RequestMapping("/pdf/upload/{chatId}")
    public Result upload(@PathVariable("chatId") String chatId, @RequestParam("file") MultipartFile file) {
        try {
            // 校验文件格式是否为pdf
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("当前只支持pdf文件！");
            }

            // 保存文件
            Boolean success = fileRepository.save(chatId, file.getResource());
            if (!success) {
                return Result.fail("文件保存失败！");
            }

            // 写入向量库
            vectorStoreService.writeToVectorStore(file.getResource());

            return Result.ok();
        } catch (Exception e) {
            log.error("pdf上传失败 错误信息 {}", e.getMessage());
            return Result.fail("上传失败");
        }
    }

    /**
     * 提供一个HTTP GET接口，用于根据指定的聊天ID（chatId）下载PDF文件。
     *
     * @param chatId 聊天记录的唯一标识符，作为路径变量传入URL。
     * @return 如果文件存在，则返回一个包含文件资源的ResponseEntity对象，
     *         并设置响应头以指示浏览器将文件作为附件下载；
     *         如果文件不存在，则返回一个404 Not Found的响应。
     */
    @GetMapping("/pdf/download/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) {
        // 根据chatId从文件存储库中加载对应的文件资源
        Resource resource = fileRepository.load(chatId);

        // 检查文件是否存在
        if (!resource.exists()) {
            // 如果文件不存在，返回404 Not Found状态码
            return ResponseEntity.notFound().build();
        }

        // 对文件名进行UTF-8编码，确保文件名在HTTP头中正确传输
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);

        // 构建并返回一个成功的HTTP响应，包含文件内容
        return ResponseEntity.ok()
                // 设置响应内容类型为二进制流，适用于各种文件类型的下载
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                // 添加Content-Disposition头，告知浏览器以附件形式下载文件，并指定下载的文件名
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                // 将文件资源作为响应体返回给客户端
                .body(resource);
    }

}
