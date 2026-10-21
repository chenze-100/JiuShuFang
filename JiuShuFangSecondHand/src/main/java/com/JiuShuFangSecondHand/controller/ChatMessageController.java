package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.query.ChatMessageQuery;
import com.JiuShuFangSecondHand.domain.vo.ChatMessageVO;
import com.JiuShuFangSecondHand.service.IChatMessageService;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @since 2026-04-18
 */
@Tag(name = "聊天消息", description = "聊天消息相关接口")
@RestController
@RequestMapping("/chat/message")
@RequiredArgsConstructor
public class ChatMessageController {

    private final IChatMessageService chatMessageService;

    /**
     * 分页查询聊天消息
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageDTO<ChatMessageVO>> getChatMessages(@Valid ChatMessageQuery query) {
        PageDTO<ChatMessageVO> result = chatMessageService.getChatMessages(query);
        return Result.success(result);
    }
}

