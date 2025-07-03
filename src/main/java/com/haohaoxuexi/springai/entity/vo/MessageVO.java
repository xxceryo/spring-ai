package com.haohaoxuexi.springai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@Data
@NoArgsConstructor
public class MessageVO {
    private String role;
    private String content;
    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER ->
                    role = "user";
            case SYSTEM ->
                    role = "system";
            case ASSISTANT ->
                    role = "assistant";
            case TOOL ->
                    role = "function";
            default ->
                    role = "unknown";
        }
        this.content = message.getText();
    }
}
