package com.JiuShuFang.goods.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFang.goods.constants.RedisConstant;
import com.JiuShuFang.goods.domain.dto.GoodsUploadDto;
import com.JiuShuFang.goods.domain.pojo.Category;
import com.JiuShuFang.goods.domain.pojo.Order;
import com.JiuShuFang.goods.domain.pojo.OrderItem;
import com.JiuShuFang.goods.domain.query.GoodsQuery;
import com.JiuShuFang.goods.domain.vo.GoodsSellerStatsVO;
import com.JiuShuFang.goods.domain.vo.GoodsToVectorVO;
import com.JiuShuFang.goods.domain.vo.GoodsVO;
import com.JiuShuFang.goods.enums.VectorOperateType;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.goods.exception.UnauthorizedException;
import com.JiuShuFang.goods.mapper.OrderMapper;
// ES 已停用，恢复时取消注释下两行
// import com.JiuShuFang.goods.domain.pojo.GoodsDoc;
// import com.JiuShuFang.goods.repository.GoodsDocRepository;
import com.JiuShuFang.goods.service.ICategoryService;
import com.JiuShuFang.goods.service.IGoodsService;
import com.JiuShuFang.goods.mapper.OrderItemMapper;
import com.JiuShuFang.common.util.BeanUtils;
import com.JiuShuFang.common.util.CollUtils;
import com.JiuShuFang.common.util.PageDTO;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.JiuShuFang.goods.domain.pojo.Goods;
import com.JiuShuFang.goods.mapper.GoodsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
// ES 已停用，恢复时取消注释下列 5 行
// import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
// import org.springframework.data.elasticsearch.core.SearchHit;
// import org.springframework.data.elasticsearch.core.SearchHits;
// import org.springframework.data.elasticsearch.core.query.Criteria;
// import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
// import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品表（二手书） 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ICategoryService categoryService;
    // ES 已停用，恢复时取消注释下列两行
    // private final GoodsDocRepository goodsDocRepository;
    // private final ElasticsearchOperations elasticsearchOperations;

    private final Map<Long, String> categoryMap = new HashMap<>();

    @PostConstruct
    public void initCategoryMap() {
        List<Category> list = categoryService.list();
        categoryMap.putAll(
                list.stream().collect(Collectors.toMap(
                        Category::getId,
                        Category::getName,
                        (oldV, newV) -> oldV
                ))
        );
    }

    @Override
    public PageDTO<GoodsVO> getGoodsListWithLikeStatus(GoodsQuery query) {
        if (BooleanUtil.isTrue(query.getIsViewSellerStore()) && query.getSellerId() == null) {
            throw new BusinessException("参数错误");
        }
        // 分页查询货物信息
        Page<Goods> page = query.toMpPageDefaultSortByCreatedTimeDesc();
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();


        if (query.getBookName() != null && !query.getBookName().trim().isEmpty()) {
            List<Long> esIds = searchGoodsIdsFromEs(query.getBookName(), query);
            if (esIds != null) {
                if (CollUtils.isEmpty(esIds)) {
                    // ES 搜索无结果
                    return PageDTO.empty(page);
                }
                // ES 命中：用 id 列表回查 MySQL（保证字段完整、状态/分类等过滤仍生效）
                wrapper.in(Goods::getId, esIds);
            } else {
                // ES 不可用，降级为 MySQL like
                wrapper.like(Goods::getBookName, query.getBookName());
            }
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, query.getCategoryId());
        } else if (CollUtils.isNotEmpty(query.getCategoryIds())) {
            wrapper.in(Goods::getCategoryId, query.getCategoryIds());
        }
        wrapper.eq(query.getSellerId() != null, Goods::getUserId, query.getSellerId());
        wrapper.like(query.getAuthor() != null, Goods::getAuthor, query.getAuthor());
        wrapper.eq(query.getDegree() != null, Goods::getDegree, query.getDegree());
        if (BooleanUtil.isTrue(query.getIsViewSellerStore())) {
            //需要指定状态查询,如果不传递默认查1
            wrapper.eq(query.getStatus() != null, Goods::getStatus, query.getStatus());
        } else {
            //不需要指定状态查询,首页内容
            wrapper.eq(Goods::getStatus, 1);
        }
        wrapper.eq(Goods::getIsDeleted, 0);
/*        if (query.getDegree() != null) {
            wrapper.eq(Goods::getDegree, query.getDegree());
        }*/
        Page<Goods> goodsPage = goodsMapper.selectPage(page, wrapper);
        List<Goods> records = goodsPage.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(goodsPage);
        }

        List<GoodsVO> voList = BeanUtils.copyList(records, GoodsVO.class);

        Long userId = UserContext.getUserId();

        if (userId == null) {
            return PageDTO.of(goodsPage, voList);
        }

        String key = RedisConstant.COLLECT_USER_KEY + userId;
        Set<String> collectSet = redisTemplate.opsForSet().members(key);

        if (CollUtils.isEmpty(collectSet)) {
            return PageDTO.of(goodsPage, voList);
        }
        Set<Long> collectGoodsIds = collectSet.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());


        voList.forEach(vo -> vo.setIsLike(collectGoodsIds.contains(vo.getId())));


        return PageDTO.of(goodsPage, voList);
    }

    @Override
    public GoodsVO getGoodsByIdWithLikeStatus(Long id) {
        Goods goods = lambdaQuery()
                .eq(Goods::getId, id)
                .one();
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        GoodsVO vo = BeanUtil.copyProperties(goods, GoodsVO.class);
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return vo;
        }
        String key = RedisConstant.COLLECT_USER_KEY + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, id.toString());
        vo.setIsLike(BooleanUtil.isTrue(isMember));
        return vo;
    }

    @Override
    public void updateGoodsStatus(Long id, Integer status) {
        Goods goods = lambdaQuery().eq(Goods::getId, id).one();
        // 1. 商品为空
        Assert.notNull(goods, () -> new BusinessException("商品不存在"));
        // 2. 商品已删除
        Assert.notEquals(goods.getIsDeleted(), 1,
                () -> new BusinessException("商品已删除，无法修改状态"));
        // 3. 商品无库存
        if (goods.getStock() != null && goods.getStock() <= 0) {
            throw new BusinessException("商品已售空，无法修改状态");
        }
        // 4. 状态合法判断
        boolean canUpdate =
                (goods.getStatus() == 2 && status == 1) ||
                        (goods.getStatus() == 1 && status == 2) ||
                        (goods.getStatus() == 1 && status == 3);
        if (!canUpdate) {
            throw new BusinessException("状态修改不合法");
        }
        goods.setStatus(status);
        lambdaUpdate().eq(Goods::getId, id)
                .set(Goods::getStatus, goods.getStatus())
                .update();
        sendToMqSaveOrUpdateVectorStore(goods, false);
        // 同步到 ES（下架/已售时搜索结果应排除）
//        syncGoodsToEs(goods);
    }

    @Override
    public void sendToMqSaveOrUpdateVectorStore(Goods goods, boolean isSave) {
        // ===== Milvus 已停用：向量 MQ 发送逻辑整体注释，方法保留为空实现（调用点无需改动），恢复时取消注释 =====
        
        if (ObjectUtil.notEqual(goods.getStatus(), 1)) {
            log.debug("商品状态不是上架状态，不保存向量存储");
            return;
        }
        GoodsToVectorVO goodsToVectorVO = BeanUtils.copyBean(goods, GoodsToVectorVO.class);
        goodsToVectorVO.setCategoryName(categoryMap.get(goods.getCategoryId()));
//        goodsToVectorVO.setStatus(Objects.requireNonNull(Goods.StatusEnum.getByCode(goods.getStatus())).getDesc());
        goodsToVectorVO.setDegree(Objects.requireNonNull(Goods.DegreeEnum.getByCode(goods.getDegree())).getDesc());
        if (isSave) {
            VectorOperateType.SAVE.sendMessageToMq(JSONUtil.toJsonStr(goodsToVectorVO), rabbitTemplate);
        } else {
            VectorOperateType.UPDATE.sendMessageToMq(JSONUtil.toJsonStr(goodsToVectorVO), rabbitTemplate);

        }
        
    }

    @Override
    public void deleteGoods(Long id) {
        Optional.ofNullable(id)
                .map(i -> lambdaQuery().eq(Goods::getId, i).one())
                .ifPresentOrElse(g -> {
                            Assert.notEquals(g.getStatus(), 1,
                                    () -> new BusinessException("上架中的商品无法删除，请先下架")
                            );
                            removeById(id);
                            // Milvus 已停用：向量删除消息不再发送，恢复时取消注释
                            // VectorOperateType.DELETE.sendMessageToMq(id.toString(),
                            //         rabbitTemplate);
                            // 删除 ES 文档
//                            deleteGoodsFromEs(id);
                        },
                        () -> {
                            throw new BusinessException("商品不存在");
                        });
    }

    @Override
    public void uploadGoods(GoodsUploadDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Goods goods = BeanUtils.copyBean(dto, Goods.class);
        goods.setUserId(userId);
        goods.setTotalNumber(dto.getStock());
        System.out.println("上架商品:"+goods);
        save(goods);
        // 保存到向量存储中
        sendToMqSaveOrUpdateVectorStore(goods, true);
        // 同步到 ES
//        syncGoodsToEs(goods);
    }

    @Override
    public void updateGoods(GoodsUploadDto dto) {
        Long userId = UserContext.getUserId();
        Assert.notNull(userId, () -> new UnauthorizedException("未登录"));
        Assert.notNull(dto.getId(), () -> new BusinessException("商品ID不能为空"));
        Goods exist = lambdaQuery().eq(Goods::getId, dto.getId()).one();
        Assert.notNull(exist, () -> new BusinessException("商品不存在"));
        Assert.notEquals(exist.getUserId(), userId, () -> new BusinessException("上架中的商品无法修改"));
        Goods goods = BeanUtils.copyBean(dto, Goods.class);
        goods.setUserId(exist.getUserId());
        goods.setTotalNumber(exist.getTotalNumber());
        goods.setStatus(exist.getStatus());
        lambdaUpdate().eq(Goods::getId, dto.getId()).update(goods);
        sendToMqSaveOrUpdateVectorStore(goods, false);
        // 同步到 ES
//        syncGoodsToEs(goods);
    }


    /**
     * 统计商家信息
     * @return
     */
    @Override
    public GoodsSellerStatsVO getGoodsSellerStats(Long sellerId) {
        GoodsSellerStatsVO goodsSellerStatsVO = new GoodsSellerStatsVO(0, 0, 0);
        //根据用户信息查询
        List<Goods> list = lambdaQuery()
                .eq(Goods::getUserId, sellerId)
                .select(Goods::getTotalNumber, Goods::getStock, Goods::getId)
                .list();
        if (CollUtils.isEmpty(list)) {
            return goodsSellerStatsVO;
        }
        Integer totalProducts = list.stream()
                .map(Goods::getTotalNumber)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        Integer totalSales = list.stream()
                .map(Goods::getStock)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        //1商品总数量
        goodsSellerStatsVO.setTotalProducts(totalSales);
        //2商品总销量
        goodsSellerStatsVO.setTotalSales(totalProducts - totalSales);
        //3订单总数量
        Set<Long> goodsIds = list.stream()
                .map(Goods::getId)
                .collect(Collectors.toSet());
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                        .in("goods_id", goodsIds)
                        .select("order_id"));
        if (CollUtils.isEmpty(orderItems)) {
            return goodsSellerStatsVO;
        }
        Set<Long> orderIds = orderItems.stream()
                .map(OrderItem::getOrderId)
                .collect(Collectors.toSet());
        Long count = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getId, orderIds)
                        .eq(Order::getStatus, 4)
        );
        goodsSellerStatsVO.setTotalOrders(count.intValue());
        return goodsSellerStatsVO;
    }



    /**
     * 通过 ES 全文检索商品 ID 列表（按相关度排序）
     * 过滤条件：上架(status=1)、未删除(isDeleted=0)
     * 若 ES 异常返回 null，调用方应降级到 MySQL like 查询
     *
     * @param bookName 搜索关键词
     * @param query    其他过滤条件
     * @return 命中的商品 ID 列表；ES 异常返回 null；无结果返回空列表
     */
    /**
     * ES 已停用，降级为直接返回 null
     * 调用方（getGoodsListWithLikeStatus）会自动降级到 MySQL like 查询
     * 恢复 ES 时，用下方被注释的完整实现替换这个方法体即可
     */
    private List<Long> searchGoodsIdsFromEs(String bookName, GoodsQuery query) {
        // ES 停用，返回 null 让调用方降级到 MySQL like
        log.debug("ES 已停用，skip searchGoodsIdsFromEs, keyword={}", bookName);
        return null;

        /* 恢复 ES 时取消注释下方完整实现
        try {
            Criteria criteria = new Criteria("bookName").matches(bookName);
            // 只搜索上架且未删除的商品
            criteria.and(new Criteria("status").is(1));
            criteria.and(new Criteria("isDeleted").is(0));
            // 可选过滤条件
            if (query.getCategoryId() != null) {
                criteria.and(new Criteria("categoryId").is(query.getCategoryId()));
            }
            if (query.getDegree() != null) {
                criteria.and(new Criteria("degree").is(query.getDegree()));
            }
            if (query.getSellerId() != null) {
                criteria.and(new Criteria("userId").is(query.getSellerId()));
            }
            Query esQuery = new CriteriaQuery(criteria);
            // 限制返回数量，避免一次性拉取过多
            esQuery.setPageable(PageRequest.of(0, 200));

            SearchHits<GoodsDoc> hits = elasticsearchOperations.search(esQuery, GoodsDoc.class);
            List<Long> ids = hits.stream()
                    .map(SearchHit::getContent)
                    .map(GoodsDoc::getId)
                    .collect(Collectors.toList());
            log.info("ES 搜索完成, keyword={}, 命中{}条", bookName, ids.size());
            return ids;
        } catch (Exception e) {
            log.warn("ES 搜索失败，降级到 MySQL: keyword={}, err={}", bookName, e.getMessage());
            return null;
        }
        */
    }

    // ==================== Elasticsearch 同步（已停用） ====================
    // 恢复时取消注释下列方法

    /*
    /**
     * 将商品写入/更新到 ES
     * ES 异常时仅记录日志，不影响主流程（降级保护）
     *
    private void syncGoodsToEs(Goods goods) {
        try {
            GoodsDoc doc = BeanUtils.copyBean(goods, GoodsDoc.class);
            goodsDocRepository.save(doc);
            log.info("同步商品到 ES 成功: id={}", goods.getId());
        } catch (Exception e) {
            log.warn("同步商品到 ES 失败（降级，不影响主流程）: id={}, err={}", goods.getId(), e.getMessage());
        }
    }

    /**
     * 从 ES 删除商品文档
     *
    private void deleteGoodsFromEs(Long id) {
        try {
            goodsDocRepository.deleteById(id);
            log.info("从 ES 删除商品文档成功: id={}", id);
        } catch (Exception e) {
            log.warn("从 ES 删除商品文档失败: id={}, err={}", id, e.getMessage());
        }
    }


    public void rebuildElasticsearchIndex() {
        log.info("开始全量重建 ES 商品索引...");
        List<Goods> goodsList = lambdaQuery()
                .eq(Goods::getIsDeleted, 0)
                .list();
        if (CollUtils.isEmpty(goodsList)) {
            log.info("无商品数据，跳过索引重建");
            return;
        }
        List<GoodsDoc> docs = BeanUtils.copyList(goodsList, GoodsDoc.class);
        goodsDocRepository.saveAll(docs);
        log.info("ES 商品索引重建完成，共 {} 条", docs.size());
    }
    */
}
