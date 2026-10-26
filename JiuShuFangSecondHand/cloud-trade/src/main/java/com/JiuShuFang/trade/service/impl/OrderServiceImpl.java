package com.JiuShuFang.trade.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFang.trade.domain.dto.OrderAIDTO;
import com.JiuShuFang.trade.domain.dto.OrderDTO;
import com.JiuShuFang.trade.domain.dto.OrderItemDTO;
import com.JiuShuFang.trade.domain.dto.OrderStatusUpdateDto;
import com.JiuShuFang.trade.domain.pojo.*;
import com.JiuShuFang.trade.service.*;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.util.*;
import com.JiuShuFang.trade.domain.query.OrderPageQuery;
import com.JiuShuFang.trade.domain.vo.AddressVO;
import com.JiuShuFang.trade.domain.vo.OrderDetailVO;
import com.JiuShuFang.trade.domain.vo.OrderItemDetailVO;
import com.JiuShuFang.trade.domain.vo.OrderItemVO;
import com.JiuShuFang.trade.domain.vo.OrderVO;
import com.JiuShuFang.trade.domain.vo.GoodsToVectorVO;
import com.JiuShuFang.trade.enums.VectorOperateType;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.trade.exception.UnauthorizedException;
import com.JiuShuFang.trade.mapper.ChatMemoryMapper;
import com.JiuShuFang.trade.mapper.GoodsMapper;
import com.JiuShuFang.trade.mapper.OrderMapper;
import com.JiuShuFang.trade.mapper.CategoryMapper;
import com.JiuShuFang.trade.mapper.ShoppingCartMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.JiuShuFang.trade.constants.MqConstant.Order.*;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final IAddressService addressService;
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final IOrderItemService orderItemService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 创建订单
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public Result<Long> createOrder(OrderDTO dto) {
        Assert.notNull(dto, () -> new BusinessException("参数不能为空"));
        Long userId = UserContext.getUserId();
        Assert.notNull(userId, () -> new UnauthorizedException("请先登录"));
        //解析收货地址
        AddressVO address = addressService.getAddressById(dto.getAddressId());
        Assert.notNull(address, () -> new BusinessException("收货地址不存在"));
        //处理订单
        Order order = CreateOrder(dto, address, userId);
        //处理订单详情并扣减库存
        List<OrderItem> orderItems = CreateOrderDetailAndDeductStock(dto);
        order.setMaxCommentNum(orderItems.size());
        save(order);
        orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
        orderItemService.saveBatch(orderItems);
        //清空购物车
        if (dto.getIsClearCart()) {
            clearCart();
        }
        //发送消息到mq验证是否已经完成支付(延迟消息1天)
        rabbitTemplate.convertAndSend(
                ORDER_DIRECT_EXCHANGE,
                ORDER_TO_PAY_ROUTING_KEY,
                order.getId(),
                message -> {
                    message.getMessageProperties().setExpiration("86400000");
                    return message;
                });
        return Result.success(order.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearCart() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            shoppingCartMapper.delete(new QueryWrapper<ShoppingCart>().eq("user_id", userId));
        }
    }


    /**
     * 创建订单详情并扣减库存
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public List<OrderItem> CreateOrderDetailAndDeductStock(OrderDTO dto) {
        //解析商品
        List<OrderItemDTO> orderItemDTOList = dto.getOrderItemDTOList();
        Set<Long> goodsId = orderItemDTOList.stream()
                .map(OrderItemDTO::getGoodsId)
                .collect(Collectors.toSet());
        if (CollUtils.isEmpty(goodsId)) {
            throw new BusinessException("请填写提交订单");
        }
        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsId);
        //获取当前商品->购买数量映射
        Map<Long, Integer> goodsNumMap = orderItemDTOList.stream()
                .collect(Collectors.toMap(OrderItemDTO::getGoodsId, OrderItemDTO::getNum));
        List<OrderItem> orderItems = new ArrayList<>(goodsList.size());
        //创建订单项并扣减库存
        for (Goods goods : goodsList) {
            Integer num = goodsNumMap.getOrDefault(goods.getId(), 1);
            Integer stock = goods.getStock();
            Assert.equals(goods.getStatus(), 1, () -> new BusinessException("商品已下架"));
            Assert.isFalse(stock < num, () -> new BusinessException("商品库存不足"));
            //扣减库存
            goods.setStock(stock - num);
            if (stock.equals(num)) {
                //修改商品状态
                goods.setStatus(3);
            }
            OrderItem orderItem = GoodsToOrderItem(goods, num);
            orderItems.add(orderItem);
            //todo 批量修改
            sendToMqSaveOrUpdateVectorStore(goods, false);
        }
        for (Goods goods : goodsList) {
            goodsMapper.updateById(goods);
        }

        //保存订单项
        return orderItems;
    }

    @NotNull
    private static OrderItem GoodsToOrderItem(Goods goods, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setSellerId(goods.getUserId());
        orderItem.setGoodsId(goods.getId());
        orderItem.setBookImg(goods.getBookImg());
        orderItem.setBookName(goods.getBookName());
        orderItem.setPrice(goods.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalPrice(goods.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }

    /**
     * 同步商品到向量库（复用商品域逻辑，交易域下单扣库存后触发更新）
     */
    private void sendToMqSaveOrUpdateVectorStore(Goods goods, boolean isSave) {
        if (ObjectUtil.notEqual(goods.getStatus(), 1)) {
            log.debug("商品状态不是上架状态，不保存向量存储");
            return;
        }
        GoodsToVectorVO goodsToVectorVO = BeanUtils.copyBean(goods, GoodsToVectorVO.class);
        Category category = categoryMapper.selectById(goods.getCategoryId());
        if (category != null) {
            goodsToVectorVO.setCategoryName(category.getName());
        }
        goodsToVectorVO.setDegree(Objects.requireNonNull(Goods.DegreeEnum.getByCode(goods.getDegree())).getDesc());
        if (isSave) {
            VectorOperateType.SAVE.sendMessageToMq(JSONUtil.toJsonStr(goodsToVectorVO), rabbitTemplate);
        } else {
            VectorOperateType.UPDATE.sendMessageToMq(JSONUtil.toJsonStr(goodsToVectorVO), rabbitTemplate);
        }
    }

    public Order CreateOrder(OrderDTO dto, AddressVO address, Long userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        //处理详细地址
        String receiverAddress = address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();
        order.setReceiverAddress(receiverAddress);
        order.setTotalPrice(dto.getTotalPrice());
        return order;
    }

    @Override
    public Result<PageDTO<OrderVO>> pageOrders(OrderPageQuery dto) {
        //获取用户信息
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        boolean isNumber = StringUtils.isNumeric(dto.getQuery());
        boolean queryBookNameCondition = !isNumber && StrUtil.isNotBlank(dto.getQuery()) && dto.getQuery() != null;
        //判断是否是卖家查看信息
        List<Long> list = new ArrayList<>();
        boolean isSeller = BooleanUtil.isTrue(dto.getIsSeller());
        List<OrderItem> orderItems = new ArrayList<>();
        if (isSeller) {
            orderItems = orderItemService.lambdaQuery()
                    .eq(OrderItem::getSellerId, userId)
                    //第二条件过滤,如果query是字符串就要过滤商品信息
                    .like(queryBookNameCondition, OrderItem::getBookName, dto.getQuery())
                    .list();
            if (CollUtils.isEmpty(orderItems)) {
                return Result.success(PageDTO.empty());
            }
            list = orderItems.stream().map(OrderItem::getOrderId).toList();
        }
        //分页查询
        Page<Order> page = dto.toMpPageDefaultSortByCreatedTimeDesc();
        LambdaQueryChainWrapper<Order> wrapper = lambdaQuery()
                .in(isSeller, Order::getId, list)
                .eq(!isSeller, Order::getUserId, userId)
                .eq(dto.getStatus() != null, Order::getStatus, dto.getStatus())
                .between(dto.getBeginTime() != null && dto.getEndTime() != null, Order::getCreateTime, dto.getBeginTime(), dto.getEndTime());
        if (isNumber) {
            wrapper.like(Order::getId, Long.parseLong(dto.getQuery()));
        }
        page = wrapper.page(page);
        List<Order> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return Result.success(PageDTO.empty(page));
        }
        //获取所有订单id用来获取订单明细信息
        Set<Long> orderIds = records.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
        if (CollUtils.isEmpty(orderIds)) {
            return Result.success(PageDTO.empty(page));
        }
        if (!isSeller) {
            orderItems = orderItemService.lambdaQuery()
                    .in(OrderItem::getOrderId, orderIds)
                    //第二条件过滤,如果query是字符串就要过滤商品信息
                    .like(queryBookNameCondition, OrderItem::getBookName, dto.getQuery())
                    .list();
            if (CollUtils.isEmpty(orderItems)) {
                return Result.success(PageDTO.empty(page));
            }
        }
        //获取所有商品 ID 用来查询商品状态
        Map<Long, Integer> goodsStatusMap = getGoodsStatusMap(orderItems);
        //组装信息
        Map<Long, List<OrderItem>> collect = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<OrderVO> orderVOList = new ArrayList<>(collect.size());
        for (Order record : records) {
            // 获取订单明细
            List<OrderItem> orderItemsGroup = collect.get(record.getId());
            // 如果没有订单明细则跳过
            if (CollUtils.isEmpty(orderItemsGroup)) continue;
            //组装订单明细
            OrderVO orderVO = BeanUtils.copyBean(record, OrderVO.class);
            //组装订单明细VO
            List<OrderItemVO> orderItemVOList = BeanUtils.copyList(orderItemsGroup, OrderItemVO.class);
            //设置商品状态
            for (OrderItemVO itemVO : orderItemVOList) {
                // 获取商品 ID
                Long goodsId = itemVO.getGoodsId();
                // 如果商品 ID 不为空且商品状态映射中包含该商品 ID 则设置商品状态
                if (goodsId != null && goodsStatusMap.containsKey(goodsId)) {
                    itemVO.setGoodStatus(goodsStatusMap.get(goodsId));
                }
            }
            orderVO.setOrderItemVOList(orderItemVOList);
            orderVOList.add(orderVO);
        }
        //返回结果
        PageDTO<OrderVO> pageDTO = PageDTO.of(page, orderVOList);
        return Result.success(pageDTO);
    }

    private Map<Long, Integer> getGoodsStatusMap(List<OrderItem> orderItems) {
        Set<Long> goodsIds = orderItems.stream()
                .map(OrderItem::getGoodsId)
                .collect(Collectors.toSet());
        if (CollUtils.isNotEmpty(goodsIds)) {
            List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
            return goodsList.stream()
                    .collect(Collectors.toMap(Goods::getId, Goods::getStatus));
        }
        return Collections.emptyMap();
    }

    @Override
    public Result<Long> countUnpaidOrders() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        //查询待付款订单数量：状态为 1
        long count = lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, 1)
                .count();
        return Result.success(count);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public Result<Void> deleteOrder(Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        if (orderId == null) {
            throw new BusinessException("订单 ID 不能为空");
        }
        //查询订单信息
        Order order = lambdaQuery()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId)
                .one();
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        //只有未付款的订单才能删除（状态为 1）
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待付款订单才能删除");
        }
        //逻辑删除订单
        lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getIsDeleted, 1)
                .update();
        //逻辑删除订单明细
        orderItemService.lambdaUpdate()
                .eq(OrderItem::getOrderId, orderId)
                .set(OrderItem::getIsDeleted, 1)
                .update();
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Result<Void> updateOrderStatus(OrderStatusUpdateDto dto) {
        Long userId = dto.getUserId();
        if (userId == null) {
            userId = UserContext.getUserId();
            if (userId == null) {
                log.error("用户ID不能为空");
                throw new UnauthorizedException("用户ID不能为空");
            }
        }
        Long orderId = dto.getOrderId();
        //查询订单信息
        Order order = lambdaQuery()
                .eq(Order::getId, orderId)
                .one();
        Assert.notNull(order , () -> new BusinessException("订单不存在"));
        if (!Objects.equals(order.getUserId(), userId)) {
            Long count = orderItemService.lambdaQuery()
                    .eq(OrderItem::getOrderId, order)
                    .eq(OrderItem::getSellerId, userId)
                    .count();
            Assert.checkBetween(count.intValue(),
                    Integer.MIN_VALUE,
                    0,
                    () -> new BusinessException("订单不存在"));
        }
        IOrderService orderService = (IOrderService) AopContext.currentProxy();
        orderService.updateStatus(order, dto);
        updateById(order);
        return Result.success();
    }

    @Override
    public Result<OrderDetailVO> getOrderDetail(Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        //查询订单信息
        Order order = getById(orderId);
        Assert.notNull(order, () -> new BusinessException("订单不存在"));
        //组装订单详情 VO
        OrderDetailVO orderDetailVO = BeanUtils.copyBean(order, OrderDetailVO.class);
        orderDetailVO.setFreightAmount(BigDecimal.ZERO);
        orderDetailVO.setCouponAmount(BigDecimal.ZERO);

        //查询订单项
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(!Objects.equals(order.getUserId(), userId), OrderItem::getSellerId, userId)
                .eq(OrderItem::getOrderId, orderId)
                .list();

        Assert.notEmpty(orderItems, () -> new BusinessException("订单不存在"));

        //获取所有商品 ID
        Map<Long, Integer> finalGoodsStatusMap = getGoodsStatusMap(orderItems);
        //组装订单项列表
        List<OrderItemDetailVO> orderItemDetailVOList = orderItems.stream()
                .map(item -> {
                    OrderItemDetailVO vo = new OrderItemDetailVO();
                    vo.setGoodsId(item.getGoodsId());
                    vo.setGoodsName(item.getBookName());
                    vo.setGoodsImage(item.getBookImg());
                    vo.setGoodsSpec(null); // 订单项中没有规格字段
                    vo.setNum(item.getNum());
                    vo.setPrice(item.getTotalPrice());
                    Long goodsId = item.getGoodsId();
                    vo.setGoodStatus(finalGoodsStatusMap.getOrDefault(goodsId, 0)); // Using finalGoodsStatusMap
                    return vo;
                }).collect(Collectors.toList());

        orderDetailVO.setOrderItemList(orderItemDetailVOList);

        return Result.success(orderDetailVO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void updateStatus(Order order, OrderStatusUpdateDto dto) {
        Integer status = dto.getStatus();
        Integer cStatus = order.getStatus();
        //如果当前状态是1->5, 2->3 3->4
        if (cStatus == 1 && status == 5) {
            //取消订单
            order.setStatus(5);
            order.setCancelReason(dto.getCancelReason());
            order.setCancelTime(LocalDateTime.now());
            //回复库存
            restoreStock(order.getId());
            return;
        }
        if (cStatus == 2 && status == 3) {
            //已发货,待收货
            order.setStatus(3);
            order.setDeliveryTime(LocalDateTime.now());
            return;
        }
        if (cStatus == 3 && status == 4) {
            //确认收货
            order.setStatus(4);
            order.setReceiveTime(LocalDateTime.now());
            return;
        }
        if (cStatus == 4 && status == 6) {
            //完成评价
            //将订单项中设置成已评论
            orderItemService.lambdaUpdate()
                    .eq(OrderItem::getId, dto.getOrderItemId())
                    .set(OrderItem::getIsComment, true)
                    .update();
            //当前评论数量加1
            order.setCurrentCommentNum(order.getCurrentCommentNum() + 1);
            //如果当前评论数量等于最大评论数量，则设置订单状态为评价
            if (order.getCurrentCommentNum().equals(order.getMaxCommentNum())) {
                order.setStatus(6);
            }
            return;
        }
        throw new BusinessException("订单状态异常");
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void restoreStock(Long id) {
        //1查询所有订单项中的商品
        List<OrderItem> orderItems = orderItemService
                .lambdaQuery()
                .eq(OrderItem::getOrderId, id)
                .list();
        if (CollUtils.isEmpty(orderItems)) {
            return;
        }
        Set<Long> goodsIds = orderItems
                .stream()
                .map(OrderItem::getGoodsId)
                .collect(Collectors.toSet());
        List<Goods> goodsList = goodsMapper.selectList(new QueryWrapper<Goods>()
                .in("id", goodsIds)
                .select("id", "stock"));
        Map<Long, Integer> orderItemNum = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getGoodsId, OrderItem::getNum));
        //2回复库存
        for (Goods goods : goodsList) {
            Integer num = orderItemNum.get(goods.getId());
            if (num == null) continue;
            goods.setStock(goods.getStock() + num);
        }
        for (Goods goods : goodsList) {
            goodsMapper.updateById(goods);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Result<Void> updateAddress(Long orderId, Long addressId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        //查询订单
        Order order = lambdaQuery()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId)
                .one();
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        //只有待付款状态才能修改地址
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待付款订单才能修改地址");
        }
        //查询地址
        AddressVO address = addressService.getAddressById(addressId);
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }
        //更新地址
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        updateById(order);
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void setDefaultAddressFromAI(OrderAIDTO orderAIDTO) {
        Order order = getById(orderAIDTO.getOrderId());
        if (order == null) {
            log.error("订单不存在");
            throw new BusinessException("订单不存在");
        }
        Address address = addressService.getById(orderAIDTO.getAddressId());
        if (address == null) {
            log.error("地址不存在");
            throw new BusinessException("地址不存在");
        }
        order.setAddressId(orderAIDTO.getAddressId());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        updateById(order);
        //修改ai的json文件(数据库+redis)
        updateAIOrderInfo(orderAIDTO.getConversationId(), order);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void updateAIOrderInfo(String conversationId, Order order) {
        ChatMemory chatMemory = chatMemoryMapper.selectOne(new LambdaQueryWrapper<ChatMemory>()
                .eq(ChatMemory::getConversationId, conversationId)
                .eq(ChatMemory::getOrderId, order.getId()));
        updateAIOrderInfo(chatMemory, order);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAIOrderInfo(ChatMemory chatMemory, Order order) {
        if (chatMemory == null || ObjectUtil.notEqual(chatMemory.getOrderId(), order.getId())) {
            log.debug("chatMemoryId is null");
            return;
        }
        //获取上下文
        String content = chatMemory.getContent();
        //解析上下文
        OrderDetailVO orderDetailVO = AIJsonUtil.extractJson(content, OrderDetailVO.class);
        if (orderDetailVO == null || orderDetailVO.getId() == null) {
            log.debug("orderDetailVO is null");
            return;
        }
        //替换订单信息
        OrderDetailVO newOrderDetailVO = BeanUtils.copyBean(order, OrderDetailVO.class);
        newOrderDetailVO.setOrderItemList(orderDetailVO.getOrderItemList());
        String jsonStr = JSONUtil.toJsonStr(newOrderDetailVO);
        System.out.println("jsonStr = " + jsonStr);
        //替换上下文
        String newContent = AIJsonUtil.replaceJson(content, newOrderDetailVO);
        chatMemory.setContent(newContent);
        chatMemoryMapper.updateById(chatMemory);
        if (StrUtil.isBlank(newContent)) {
            log.error("订单不存在");
            throw new BusinessException("订单不存在");
        }
        //修改redis中的内容
        setRedisContent(newContent, chatMemory.getConversationId(), order.getId());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAIOrderInfo(String conversationId, OrderDetailVO orderDetailVO) {
        updateAIOrderInfo(conversationId, BeanUtils.copyBean(orderDetailVO, Order.class));
    }

    public void setRedisContent(String content, String conversationId, Long orderId) {
        String redisKey = "chat:memory:" + UserContext.getUserId() + ":" + conversationId;
        List<String> range = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (CollUtils.isEmpty(range)) {
            log.error("订单不存在");
            throw new BusinessException("订单不存在");
        }
        List<String> newRange = new ArrayList<>(range.size());
        String match = orderId.toString();
        for (Object r : range) {
            String s = r.toString();
            if (!s.contains(match)) {
                newRange.add(s);
                continue;
            }
            Map<String, Object> map = JSONUtil.parseObj(s);
            map.put("textContent", content);
            newRange.add(JSONUtil.toJsonStr(map));
        }
        redisTemplate.opsForList().trim(redisKey, 1, 0);
        redisTemplate.opsForList().rightPushAll(redisKey, newRange);
    }


}
