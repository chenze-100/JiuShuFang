package com.JiuShuFangSecondHand.listener;

import com.JiuShuFangSecondHand.domain.dto.MqCommentScoreDto;
import com.JiuShuFangSecondHand.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.JiuShuFangSecondHand.constants.MqConstant.Comment.*;

@RequiredArgsConstructor
@Component
public class CommentListener {

    private final ICommentService commentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = COMMENT_QUEUE, durable = "true"),
            exchange = @Exchange(value = COMMENT_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = COMMENT_ROUTING_KEY
    ))
    public void setCommentScoreAndAverageScore(MqCommentScoreDto dto) {
        if(dto.getGoodsId() == null){
            return;
        }
        commentService.setCommentAvgScore(dto.getGoodsId(), dto.getScore(), dto.getPlus());
    }

}
