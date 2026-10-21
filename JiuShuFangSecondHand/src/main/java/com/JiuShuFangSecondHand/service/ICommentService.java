package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.CommentDto;
import com.JiuShuFangSecondHand.domain.query.CommentQuery;
import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.CommentUserVO;
import com.JiuShuFangSecondHand.domain.vo.CommentVO;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.JiuShuFangSecondHand.domain.pojo.Comment;

/**
 * <p>
 * 商品评论表 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 分页查询评论
     *
     * @param query 评论查询参数
     * @return 分页结果
     */
    PageDTO<CommentVO> getCommentList(CommentQuery query);

    /**
     * 发表评论
     *
     * @param dto 评论 DTO
     */
    void addComment(CommentDto dto);

    void setCommentAvgScore(long goodsId, double score, int plus);

    /**
     * 查询书籍评论平均星级
     *
     * @param goodsId 书籍 ID
     * @return 平均星级
     */
    Double getAverageScore(Long goodsId);

    void deleteComment(Long id);


    PageDTO<CommentUserVO> getMyComments(PageQuery query);

    void updateComment(CommentDto dto);

    CommentVO getCommentByUserAndGoods(Long goodsId, Long orderId);
}
