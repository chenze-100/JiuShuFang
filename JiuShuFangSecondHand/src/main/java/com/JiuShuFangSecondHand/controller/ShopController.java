package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.dto.ShopAddDto;
import com.JiuShuFangSecondHand.domain.vo.ShopVO;
import com.JiuShuFangSecondHand.service.IShopService;
import com.JiuShuFangSecondHand.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商铺控制器
 */
@Tag(name = "商铺", description = "商铺相关接口")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final IShopService shopService;

    @Operation(summary = "添加商铺", description = "流程图：添加商铺完整流程，需登录")
    @PostMapping("/add")
    public Result<Map<String, Long>> addShop(@Valid @RequestBody ShopAddDto dto) {
        Long shopId = shopService.addShop(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("shopId", shopId);
        return Result.success(data, "提交成功，等待审核");
    }

    @Operation(summary = "查询我的商铺列表", description = "需登录")
    @GetMapping("/list/my")
    public Result<List<ShopVO>> listMyShops() {
        List<ShopVO> list = shopService.listMyShops();
        return Result.success(list);
    }

    @Operation(summary = "查询商铺详情", description = "公开接口，无需登录")
    @GetMapping("/{id}")
    public Result<ShopVO> getById(@PathVariable Long id) {
        ShopVO shop = shopService.getById(id);
        return Result.success(shop);
    }

    @Operation(summary = "获取创建商铺前置检查结果", description = "返回实名认证状态、商铺数量、审核中状态")
    @GetMapping("/check-before-add")
    public Result<Map<String, Object>> checkBeforeAdd() {
        Map<String, Object> result = new HashMap<>();
        System.out.println("检查创建商铺前置条件");
        result.put("realNameVerified", shopService.isRealNameVerified());
        result.put("myShopCount", shopService.countMyShops());
        result.put("hasPendingShop", shopService.hasPendingShop());
        result.put("maxShopCount", 3);
        return Result.success(result);
    }
}
