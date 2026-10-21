package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.service.IChatClientService;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 实现ai只能客服的接口
 */
@Tag(name = "AI聊天", description = "AI聊天相关接口")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatClientController {


    private final IChatClientService chatClientService;



    @GetMapping("/chat")
    public Result<String> chat(
            @RequestParam(value = "prompt") String prompt,
            @RequestParam(value = "conversationId") String conversationId,
            @RequestParam(value = "imageUrl" , required = false) String imageUrl) {
        String chat = chatClientService.chat(prompt, conversationId , imageUrl);
        return Result.success(chat);
    }


}
