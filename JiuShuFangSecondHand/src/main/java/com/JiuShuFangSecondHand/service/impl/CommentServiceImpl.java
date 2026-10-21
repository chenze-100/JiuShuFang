package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.domain.dto.CommentDto;
import com.JiuShuFangSecondHand.domain.dto.MqCommentScoreDto;
import com.JiuShuFangSecondHand.domain.query.CommentQuery;
import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.CommentUserVO;
import com.JiuShuFangSecondHand.domain.vo.CommentVO;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.service.ICommentService;
import com.JiuShuFangSecondHand.util.BeanUtils;
import com.JiuShuFangSecondHand.util.CollUtils;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.JiuShuFangSecondHand.domain.pojo.Comment;
import com.JiuShuFangSecondHand.domain.pojo.Goods;
import com.JiuShuFangSecondHand.domain.pojo.User;
import com.JiuShuFangSecondHand.mapper.CommentMapper;
import com.JiuShuFangSecondHand.mapper.GoodsMapper;
import com.JiuShuFangSecondHand.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.JiuShuFangSecondHand.constants.MqConstant.Comment.COMMENT_DIRECT_EXCHANGE;
import static com.JiuShuFangSecondHand.constants.MqConstant.Comment.COMMENT_ROUTING_KEY;
import static com.JiuShuFangSecondHand.constants.MqConstant.Order.*;

/**
 * <p>
 * 商品评论表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public PageDTO<CommentVO> getCommentList(CommentQuery query) {
        if (query == null) {
            throw new BusinessException("参数错误");
        }
        Page<Comment> page = lambdaQuery().eq(Comment::getGoodsId, query.getGoodsId())
                .eq(query.getScore() != null, Comment::getScore, query.getScore())
                .page(query.toMpPageDefaultSortByCreatedTimeDesc());
        List<Comment> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        Set<Long> userIds = new HashSet<>();
        for (Comment comment : records) {
            userIds.add(comment.getUserId());
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> collect = users.stream().collect(Collectors.toMap(User::getId, k -> k));

        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : records) {
            CommentVO vo = BeanUtil.copyProperties(comment, CommentVO.class);
            String[] split = comment.getPicture().split(",");
            vo.setPicture(Arrays.asList(split));
            User user = collect.get(comment.getUserId());
            if (user == null) {
                vo.setNickName("匿名用户");
            } else {
                vo.setNickName(user.getNickName());
                vo.setAvatar(user.getAvatar());
            }
            voList.add(vo);
        }

        return PageDTO.of(page, voList);
    }

    /**
     * 添加商品评论,并发送mq,并且修改商品状态
     * @param dto 评论 DTO
     */
    @Override
    public void addComment(CommentDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        Comment comment = BeanUtils.copyBean(dto, Comment.class);
        comment.setUserId(userId);
        String picture = String.join(",", dto.getPicture());
        comment.setPicture(picture);
        commentMapper.insert(comment);

        sendToMqToSetCommentScoreAndAvgInRedis(
                new MqCommentScoreDto(comment.getGoodsId(), comment.getScore(), 1)
        );
        dto.setUserId(userId);
        sendToMqToSetOrderStatus(dto);
    }

    private void sendToMqToSetOrderStatus(CommentDto dto) {
        rabbitTemplate.convertAndSend(ORDER_DIRECT_EXCHANGE,
                ORDER_STATUS_ROUTING_KEY, dto);
    }

    private void sendToMqToSetCommentScoreAndAvgInRedis(MqCommentScoreDto dto) {
        rabbitTemplate.convertAndSend(COMMENT_DIRECT_EXCHANGE,
                COMMENT_ROUTING_KEY, dto);
    }

    /**
     * todo 设置商品评分(后置可以优化发送mq信息)
     * @param goodsId
     * @param score
     * @param plus 1 增加 0 减少
     */
    @Override
    public void setCommentAvgScore(long goodsId, double score, int plus) {
        String amountKey = RedisConstant.BOOK_SCORE_AMOUNT + goodsId;
        String averageKey = RedisConstant.BOOK_SCORE_AVERAGE + goodsId;
        String amountStr = redisTemplate.opsForValue().get(amountKey);
        String averageStr = redisTemplate.opsForValue().get(averageKey);

        if (amountStr != null && averageStr != null) {
            int amount = Integer.parseInt(amountStr);
            double average = Double.parseDouble(averageStr);
            int newAmount = amount + plus;
            if (newAmount <= 0) {
                redisTemplate.delete(amountKey);
                redisTemplate.delete(averageKey);
                return;
            }
            double newAverage = new BigDecimal(average * amount + score)
                    .divide(new BigDecimal(newAmount), 2, RoundingMode.HALF_UP)
                    .doubleValue();
            redisTemplate.opsForValue().set(amountKey, String.valueOf(newAmount));
            redisTemplate.opsForValue().set(averageKey, String.valueOf(newAverage));
        }
    }

    @Override
    public Double getAverageScore(Long goodsId) {
        String amountKey = RedisConstant.BOOK_SCORE_AMOUNT + goodsId;
        String averageKey = RedisConstant.BOOK_SCORE_AVERAGE + goodsId;
        String amountStr = redisTemplate.opsForValue().get(amountKey);
        String averageStr = redisTemplate.opsForValue().get(averageKey);
        if (amountStr != null && averageStr != null) {
            redisTemplate.expire(amountKey, 1, TimeUnit.HOURS);
            redisTemplate.expire(averageKey, 1, TimeUnit.HOURS);
            return Double.parseDouble(averageStr);
        }

        List<Comment> comments = lambdaQuery()
                .eq(Comment::getGoodsId, goodsId)
                .eq(Comment::getIsDeleted, 0)
                .select(Comment::getScore)
                .list();
        if (CollUtils.isEmpty(comments)) {
            return 0.0;
        }
        double sum = comments.stream()
                .mapToDouble(Comment::getScore)
                .sum();
        Double average = new BigDecimal(sum)
                .divide(new BigDecimal(comments.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();
        redisTemplate.opsForValue().set(amountKey, String.valueOf(comments.size()), 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(averageKey, average.toString(), 1, TimeUnit.HOURS);

        return average;
    }


    /**
     * 删除商品评论
     * @param id
     */
    @Override
    public void deleteComment(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        Comment comment = getById(id);
        if (ObjectUtil.notEqual(comment.getUserId(), userId)) {
            throw new BusinessException("只能删除自己发表的评论");
        }
        removeById(id);
        sendToMqToSetCommentScoreAndAvgInRedis(
                new MqCommentScoreDto(comment.getGoodsId(), comment.getScore(), -1)
        );
    }


    @Override
    public PageDTO<CommentUserVO> getMyComments(PageQuery query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Page<Comment> page = query.toMpPageDefaultSortByCreatedTimeDesc();
        Page<Comment> resultPage = lambdaQuery()
                .eq(Comment::getUserId, userId)
                .page(page);

        List<Comment> records = resultPage.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(resultPage);
        }

        //获取所有商品 ID
        Set<Long> goodsIds = records.stream()
                .map(Comment::getGoodsId)
                .collect(Collectors.toSet());
        Map<Long, Goods> goodsMap = new HashMap<>();
        if (CollUtils.isNotEmpty(goodsIds)) {
            List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
            goodsMap = goodsList.stream()
                    .collect(Collectors.toMap(Goods::getId, g -> g));
        }
        Map<Long, Goods> finalGoodsMap = goodsMap;

        //组装 VO
        List<CommentUserVO> voList = records.stream().map(comment -> {
            CommentUserVO vo = new CommentUserVO();
            vo.setId(comment.getId());
            vo.setGoodsId(comment.getGoodsId());
            vo.setOrderId(comment.getOrderId());
            vo.setUserId(comment.getUserId());
            //拼接用户信息
            vo.setAvatar(user.getAvatar());
            vo.setNickName(user.getNickName());
            //拼接评论信息
            vo.setContent(comment.getContent());
            if (comment.getPicture() != null) {
                String[] split = comment.getPicture().split(",");
                vo.setPicture(Arrays.asList(split));
            }
            vo.setScore(comment.getScore());
            vo.setCreateTime(comment.getCreateTime());
            //拼接商品信息
            Goods goods = finalGoodsMap.get(comment.getGoodsId());
            if (goods != null) {
                vo.setBookName(goods.getBookName());
                vo.setAuthor(goods.getAuthor());
                vo.setBookImg(goods.getBookImg());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageDTO.of(resultPage, voList);
    }

    @Override
    public void updateComment(CommentDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        //查询评论
        Comment comment = lambdaQuery()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getGoodsId, dto.getGoodsId())
                .one();
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setContent(dto.getContent());
        if (dto.getPicture() != null) {
            String picture = String.join(",", dto.getPicture());
            comment.setPicture(picture);
        }
        Integer preScore = comment.getScore();
        comment.setScore(dto.getScore());
        //更新评论
        updateById(comment);
        //更新redis缓存
        sendToMqToSetCommentScoreAndAvgInRedis(
                new MqCommentScoreDto(
                        comment.getGoodsId(),
                        dto.getScore() - preScore,
                        0)
        );
    }

    @Override
    public CommentVO getCommentByUserAndGoods(Long goodsId, Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        //查询用户的评论
        Comment comment = lambdaQuery()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getOrderId, orderId)
                .eq(Comment::getGoodsId, goodsId)
                .one();

        if (comment == null) {
            return null;
        }
        //组装 VO
        CommentVO vo = BeanUtils.copyBean(comment, CommentVO.class);
        //获取用户信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setAvatar(user.getAvatar());
            vo.setNickName(user.getNickName());
        } else {
            vo.setNickName("匿名用户");
        }
        //处理图片
        if (comment.getPicture() != null) {
            String[] split = comment.getPicture().split(",");
            vo.setPicture(Arrays.asList(split));
        }
        return vo;
    }
}
