package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.ChatSessionVO;
import com.JiuShuFangSecondHand.service.IChatSessionService;
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
@Tag(name = "聊天会话", description = "聊天会话相关接口")
@RestController
@RequestMapping("/chat/session")
@RequiredArgsConstructor
public class ChatSessionController {

    private final IChatSessionService chatSessionService;

    /**
     * 分页查询聊天会话
     * @param query 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageDTO<ChatSessionVO>> getChatSessions(@Valid PageQuery query) {
        PageDTO<ChatSessionVO> result = chatSessionService.getChatSessions(query);
        return Result.success(result);
    }
}

