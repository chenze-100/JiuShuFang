package com.JiuShuFang.trade.service.impl;

import com.JiuShuFang.trade.domain.pojo.OrderItem;
import com.JiuShuFang.trade.mapper.OrderItemMapper;
import com.JiuShuFang.trade.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
