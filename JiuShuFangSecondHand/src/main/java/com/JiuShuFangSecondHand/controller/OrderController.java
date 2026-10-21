package com.JiuShuFangSecondHand.controller;


import com.JiuShuFangSecondHand.domain.dto.OrderAIDTO;
import com.JiuShuFangSecondHand.domain.dto.OrderDTO;
import com.JiuShuFangSecondHand.domain.dto.OrderStatusUpdateDto;
import com.JiuShuFangSecondHand.domain.query.OrderPageQuery;
import com.JiuShuFangSecondHand.domain.vo.OrderDetailVO;
import com.JiuShuFangSecondHand.domain.vo.OrderVO;
import com.JiuShuFangSecondHand.service.IOrderService;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    private final IOrderService orderService;


    /**
     * 创建订单
     * @param dto
     * @return
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "根据购物车信息创建订单")
    public Result<Long> createOrder(@RequestBody @Valid OrderDTO dto) {
        return orderService.createOrder(dto);
    }

    /**
     * 分页查询订单列表
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询订单", description = "根据条件分页查询用户订单列表")
    public Result<PageDTO<OrderVO>> pageOrders(OrderPageQuery dto) {
        return orderService.pageOrders(dto);
    }

    

    /**
     * 查询用户当前待付款订单数量
     * @return 待付款订单数量
     */
    @GetMapping("/unpaid/count")
    @Operation(summary = "查询待付款订单数量", description = "查询当前登录用户的待付款订单数量（状态为 1）")
    public Result<Long> countUnpaidOrders() {
        return orderService.countUnpaidOrders();
    }

    /**
     * 删除订单
     * @param orderId 订单 ID
     * @return 删除结果
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "删除订单", description = "逻辑删除订单及订单明细，仅支持待付款订单")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        return orderService.deleteOrder(orderId);
    }

    /**
     * 修改订单状态
     * @param dto 订单状态更新参数
     * @return 修改结果
     */
    @PutMapping("/status")
    @Operation(summary = "修改订单状态", description = "修改订单状态，支持状态流转验证")
    public Result<Void> updateOrderStatus(@RequestBody @Valid OrderStatusUpdateDto dto) {
        return orderService.updateOrderStatus(dto);
    }

    /**
     * 根据 ID 查询订单详情
     * @param orderId 订单 ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情", description = "根据订单 ID 查询订单详情")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    /**
     * 修改订单收货地址
     * @param dto 包含 orderId 和 addressId
     * @return 修改结果
     */
    @PutMapping("/address")
    @Operation(summary = "修改订单地址", description = "修改订单的收货地址，仅支持待付款订单")
    public Result<Void> updateAddress(@RequestBody java.util.Map<String, Long> dto) {
        return orderService.updateAddress(dto.get("orderId"), dto.get("addressId"));
    }


    /**
     * 设置订单地址
     * @return
     */
    @PutMapping("/ai")
    public Result<String> setOrderAddressFromAI(@RequestBody @Valid OrderAIDTO orderAIDTO) {
        orderService.setDefaultAddressFromAI(orderAIDTO);
        return Result.success("设置默认地址成功");
    }

}
