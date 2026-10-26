package com.JiuShuFang.trade.service.impl;

import com.JiuShuFang.trade.domain.pojo.PayOrder;
import com.JiuShuFang.trade.mapper.PayOrderMapper;
import com.JiuShuFang.trade.service.IPayOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付订单表 服务实现类
 * </p>
 *
 * @since 2026-04-20
 */
@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

}
