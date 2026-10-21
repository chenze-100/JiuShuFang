package com.JiuShuFangSecondHand.controller;


import com.JiuShuFangSecondHand.domain.pojo.ChatList;
import com.JiuShuFangSecondHand.domain.pojo.ChatMemory;
import com.JiuShuFangSecondHand.service.IChatListService;
import com.JiuShuFangSecondHand.service.IChatMemoryService;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @since 2026-04-21
 */
@Tag(name = "聊天记忆", description = "聊天记忆相关接口")
@RestController
@RequestMapping("/chat-memory")
@RequiredArgsConstructor
public class ChatMemoryController {

    private final IChatMemoryService chatMemoryService;

    private final IChatListService chatListService;

    /**
     * 获取用户所有会话列表
     */
    @GetMapping("/list")
    public Result<List<ChatList>> getConversationList() {
        List<ChatList> list = chatListService.getUserConversationList();
        return Result.success(list);
    }

    /**
     * 获取会话的所有消息
     */
    @GetMapping("/{conversationId}")
    public Result<List<ChatMemory>> getMessages(@PathVariable String conversationId) {
        List<ChatMemory> messages = chatMemoryService.getConversationMessages(conversationId);
        return Result.success(messages);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable String conversationId) {
        chatMemoryService.deleteConversation(conversationId);
        return Result.success();
    }

}
