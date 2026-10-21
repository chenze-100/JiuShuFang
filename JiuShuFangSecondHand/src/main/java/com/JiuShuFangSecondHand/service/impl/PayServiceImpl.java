package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.domain.pojo.*;
import com.JiuShuFangSecondHand.service.*;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.properties.ServerProperties;
import com.JiuShuFangSecondHand.properties.AlipayProperties;
import com.JiuShuFangSecondHand.util.CollUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements IPayService {
    private final AlipayClient client;
    private final IOrderService orderService;
    private final StringRedisTemplate redisTemplate;
    private final AlipayProperties alipayProperties;
    private final IOrderItemService orderItemService;
    private final IGoodsService goodsService;
    private final IPayOrderService payOrderService;
    private final IPayRecordService payRecordService;
    private final ServerProperties serverConfig;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void pcPay(HttpServletResponse response, Long orderId) {

        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单已支付");
        }
        //校验库存是否充足
        if (!checkStock(order)) {
            throw new BusinessException("商品库存不足或已下架");
        }
        //将订单存到redis中，用于后续查询订单状态
        addOrderToRedis(orderId);
        addPayOrder(order);

        // 2. 构造请求
        AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(orderId, order);
        request.setReturnUrl(serverConfig.getVueUrl());
        request.setNotifyUrl(serverConfig.getExternalUrl() + "/pay/notify");

        // 3. 获取支付宝表单并自动跳转
        try {
            String form = client.pageExecute(request).getBody();

            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(form);
            response.getWriter().flush();
        } catch (Exception e) {
            throw new BusinessException("支付失败");
        }
    }


    /**
     * 创建支付表单
     * @param order
     */
    public void addPayOrder(Order order) {
        PayOrder getPayOrder = payOrderService.lambdaQuery()
                .eq(PayOrder::getOrderNo, order.getId())
                .one();
        if(getPayOrder != null){
            log.debug("支付表单已存在 orderNo:{}" , order.getId());
            return;
        }
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(order.getId());
        payOrder.setOutTradeNo(order.getId());
        payOrder.setUserId(order.getUserId());
        payOrder.setTotalAmount(order.getTotalPrice());
        payOrder.setStatus(0);
        payOrderService.save(payOrder);
    }


    /**
     * 支付前校验库存是否充足，并扣除库存
     * @param order
     * @return
     */
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean checkStock(Order order) {
        Long id = order.getId();
        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, id)
                .list();
        if (CollUtils.isEmpty(orderItems)) {
            return true;
        }
        //获取所有商品
        Set<Long> goodsIds = orderItems.stream()
                .map(OrderItem::getGoodsId)
                .collect(Collectors.toSet());
        List<Goods> goods = goodsService.listByIds(goodsIds);
        Map<Long, OrderItem> collect = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getGoodsId,
                        orderItem -> orderItem));
        for (Goods good : goods) {
            //不是上架的商品不能购买
            if (good.getStatus() != 1) {
                return false;
            }
            OrderItem orderItem = collect.get(good.getId());
//            if (orderItem == null) continue;
/*            if (good.getStock() < orderItem.getNum()) {
                return false;
            }*/
            /*good.setStock(good.getStock() - orderItem.getNum());
            if (good.getStock() <= 0) {
                good.setStatus(3);
            }*/
        }
//        goodsService.updateBatchById(goods);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        try {
            log.info("支付宝回调参数: {}", params);

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );

            if (!signVerified) {
                log.warn("支付宝回调签名验证失败");
                return "fail";
            }

            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");

            // 创建支付回调记录
            addPayRecord(params);

            log.info("支付宝回调 - 订单号: {}, 交易状态: {}", outTradeNo, tradeStatus);

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 幂等性处理：先查询订单状态，如果已支付则直接返回 success
                Order order = orderService.getById(Long.parseLong(outTradeNo));
                if (order == null) {
                    log.error("订单不存在: {}", outTradeNo);
                    return "fail";
                }
                if (order.getStatus() == 2 || order.getStatus() == 3) {
                    log.info("订单已支付，跳过处理: {}", outTradeNo);
                    return "success";
                }

                // 设置订单和支付状态为成功
                setOrderAndPayStatusToSuccess(outTradeNo);

                log.info("订单支付成功: {}", outTradeNo);

                // 删除redis缓存
                redisTemplate.opsForSet().remove(RedisConstant.ORDER_CHECK_KEY, outTradeNo);
                return "success";
            }

            log.warn("支付宝回调交易状态异常: {}", tradeStatus);
            return "fail";
        } catch (Exception e) {
            log.error("支付宝回调异常", e);
            return "fail";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setOrderAndPayStatusToSuccess(String outTradeNo) {
        // 更新订单为已支付
        orderService.lambdaUpdate()
                .eq(Order::getId, Long.parseLong(outTradeNo))
                .set(Order::getPayType, 1)
                .set(Order::getStatus, 2)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();

        //更新支付订单为已支付
        payOrderService.lambdaUpdate()
                .eq(PayOrder::getOrderNo, Long.parseLong(outTradeNo))
                .set(PayOrder::getStatus, 1)
                .set(PayOrder::getPayTime, LocalDateTime.now())
                .set(PayOrder::getPayType, 1)
                .update();
    }


    /**
     * 创建支付宝支付记录
     *
     * @param params
     */
    private void addPayRecord(Map<String, String> params) {
        // 取值
        String orderNo = params.get("out_trade_no");
        String alipayTradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");
        String notifyParams = JSONUtil.toJsonStr(params);

        // 插入数据库
        PayRecord record = new PayRecord();
        record.setOrderNo(Long.parseLong(orderNo));
        record.setOutTradeNo(alipayTradeNo);
        record.setTradeStatus(tradeStatus);
        record.setTotalAmount(new BigDecimal(totalAmount));
        record.setNotifyParams(notifyParams);

        payRecordService.save(record);
    }

    private void addOrderToRedis(Long orderId) {
        String key = RedisConstant.ORDER_CHECK_KEY;
        Boolean member = redisTemplate.opsForSet().isMember(key, orderId.toString());
        // 如果订单正在支付中，则不能重复支付
        if (BooleanUtil.isTrue(member)) {
            throw new BusinessException("订单正在支付中");
        }
        redisTemplate.opsForSet().add(key, orderId.toString());
    }

    @NotNull
    private static AlipayTradePagePayRequest getAlipayTradePagePayRequest(Long orderId, Order order
    ) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // 订单信息
        String outTradeNo = orderId.toString();
        String totalAmountStr = String.valueOf(order.getTotalPrice().doubleValue());
        String subject = order.getReceiverName() + "的订单";

        String bizContent = "{"
                + "\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmountStr + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}";

        request.setBizContent(bizContent);
        return request;
    }


    @Scheduled(fixedDelay = 4000)
    public void CheckOrderStatus() {
        // 支付宝私钥未配置（测试阶段）时跳过对账，避免定时任务反复触发签名报错
        if (!StringUtils.hasText(alipayProperties.getPrivateKey())) {
            return;
        }
        //获取所有订单状态
        String key = RedisConstant.ORDER_CHECK_KEY;
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (CollUtils.isEmpty(members)) {
            log.debug("暂无订单");
            return;
        }
        Set<String> removeMembers = new HashSet<>();
        //遍历所有订单状态并操作
        for (String member : members) {
            try {
                Boolean isRemove = CheckOrderStatusAndUpdate(member);
                if (isRemove) {
                    removeMembers.add(member);
                }
            } catch (Exception e) {
                // 单笔订单对账异常不影响其他订单，下一轮定时任务会重试
                log.warn("订单对账异常 orderId:{}", member, e);
            }
        }
        if (CollUtils.isEmpty(removeMembers)) return;
        redisTemplate.opsForSet().remove(key, removeMembers.toArray());
    }

    private Boolean CheckOrderStatusAndUpdate(String orderId) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderId);
        request.setBizModel(model);
        AlipayTradeQueryResponse response = null;
        try {
            response = client.execute(request);
        } catch (AlipayApiException e) {
            log.debug("查询订单状态失败");
            return false;
        }
        if (!response.isSuccess()) {
            log.debug("订单查询失败 orderId:{}, msg:{}", orderId, response.getMsg());
            return true;
        }

        //待支付
        if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())) {
            return false;
        }
        //支付成功
        if ("TRADE_SUCCESS".equals(response.getTradeStatus())
                || "TRADE_FINISHED".equals(response.getTradeStatus())) {
            //修改状态为已支付
            ((IPayService) AopContext.currentProxy())
                    .setOrderAndPayStatusToSuccess(orderId);
        }
        return true;
    }

    /**
     * 测试阶段模拟支付（不对接真实支付渠道）：
     * 复用支付宝成功回调的落库逻辑，一次事务内完成：
     * 1.校验订单 2.生成支付单 3.更新订单/支付单为已支付 4.写入支付流水
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void balancePay(Long orderId, BigDecimal amount) {
        // 1. 校验订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单已支付，请勿重复支付");
        }

        // 2. 生成支付单（内部有幂等判断，已存在则跳过）
        addPayOrder(order);

        // 3. 更新订单为已支付（支付方式：2余额；status: 1待付款 -> 2待发货）
        orderService.lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getPayType, 2)
                .set(Order::getStatus, 2)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();

        // 4. 更新支付单为已支付（status: 0待支付 -> 1已支付）
        payOrderService.lambdaUpdate()
                .eq(PayOrder::getOrderNo, orderId)
                .set(PayOrder::getStatus, 1)
                .set(PayOrder::getPayType, 2)
                .set(PayOrder::getPayTime, LocalDateTime.now())
                .update();

        // 5. 写入支付流水（模拟支付宝回调生成的记录，便于测试阶段追溯）
        Map<String, String> mockParams = new HashMap<>();
        mockParams.put("out_trade_no", orderId.toString());
        mockParams.put("trade_no", "TEST" + System.currentTimeMillis());
        mockParams.put("trade_status", "TRADE_SUCCESS");
        mockParams.put("total_amount", amount != null ? amount.toPlainString()
                : String.valueOf(order.getTotalPrice().doubleValue()));
        mockParams.put("notify_params", "测试阶段模拟支付，无真实回调");
        addPayRecord(mockParams);

        // 6. 若之前发起过支付宝支付，清理 Redis 中的"支付中"标记
        redisTemplate.opsForSet().remove(RedisConstant.ORDER_CHECK_KEY, orderId.toString());

        log.info("测试支付成功 orderId:{}, amount:{}", orderId, amount);
    }
}
