package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.dto.CommentDto;
import com.JiuShuFangSecondHand.domain.query.CommentQuery;
import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.CommentUserVO;
import com.JiuShuFangSecondHand.domain.vo.CommentVO;
import com.JiuShuFangSecondHand.service.ICommentService;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 * 商品评论表 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@Tag(name = "评论", description = "评论相关接口")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 获取商品评论列表
     * @param query
     * @return
     */
    @GetMapping
    public Result<PageDTO<CommentVO>> getCommentList(@Valid CommentQuery query) {
        PageDTO<CommentVO> result = commentService.getCommentList(query);
        return Result.success(result);
    }


    /**
     * 添加商品评论
     * @param dto
     * @return
     */
    @PostMapping
    public Result<String> addComment(@Valid @RequestBody CommentDto dto) {
        commentService.addComment(dto);
        return Result.success("评论发表成功");
    }


    /**
     * 获取商品平均评分
     * @param goodsId
     * @return
     */
    @GetMapping("/score/{goodsId}")
    public Result<Double> getAverageScore(@PathVariable Long goodsId) {
        Double averageScore = commentService.getAverageScore(goodsId);
        return Result.success(averageScore);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    /**
     * 根据商品 ID 查询用户对该商品的评论
     * @param goodsId 商品 ID
     * @return 评论信息
     */
    @GetMapping("/user/{goodsId}/{orderId}")
    public Result<CommentVO> getCommentByUserAndGoods(@PathVariable Long goodsId, @PathVariable Long orderId) {
        CommentVO commentVO = commentService.getCommentByUserAndGoods(goodsId, orderId);
        return Result.success(commentVO);
    }

    /**
     * 分页查询我的评价
     * @param query 分页查询参数
     * @return 我的评价列表
     */
    @GetMapping("/user/page")
    public Result<PageDTO<CommentUserVO>> getMyComments(PageQuery query) {
        PageDTO<CommentUserVO> result = commentService.getMyComments(query);
        return Result.success(result);
    }

    /**
     * 修改评论
     * @param dto 评论 DTO
     * @return 修改结果
     */
    @PutMapping("/set")
    public Result<String> updateComment(@Valid @RequestBody CommentDto dto) {
        commentService.updateComment(dto);
        return Result.success("评论修改成功");
    }
}
