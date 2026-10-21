package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.dto.GoodsStatusDto;
import com.JiuShuFangSecondHand.domain.dto.GoodsUploadDto;
import com.JiuShuFangSecondHand.domain.query.GoodsQuery;
import com.JiuShuFangSecondHand.domain.vo.GoodsSellerStatsVO;
import com.JiuShuFangSecondHand.domain.vo.GoodsVO;
import com.JiuShuFangSecondHand.service.IGoodsService;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 * 商品表（二手书） 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@Tag(name = "商品", description = "商品相关接口")
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final IGoodsService goodsService;

    @GetMapping("/list")
    public Result<PageDTO<GoodsVO>> getGoodsList(@Valid GoodsQuery query) {
        PageDTO<GoodsVO> result = goodsService.getGoodsListWithLikeStatus(query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> getGoodsById(@PathVariable Long id) {
        GoodsVO goodsVO = goodsService.getGoodsByIdWithLikeStatus(id);
        if (goodsVO == null) {
            return Result.error("商品不存在或已下架");
        }
        return Result.success(goodsVO);
    }


    @PutMapping("/status")
    public Result<String> updateGoodsStatus(@Valid @RequestBody GoodsStatusDto dto) {
        goodsService.updateGoodsStatus(dto.getId(), dto.getStatus());
        return Result.success("商品状态修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return Result.success("商品删除成功");
    }

    /**
     * 上架商品
     * @param dto 上架商品 DTO
     * @return 结果
     */
    @PostMapping("/upload")
    public Result<String> uploadGoods(@Valid @RequestBody GoodsUploadDto dto) {
        goodsService.uploadGoods(dto);
        return Result.success("商品上架成功");
    }

    @PutMapping("/update")
    public Result<String> updateGoods(@Valid @RequestBody GoodsUploadDto dto) {
        goodsService.updateGoods(dto);
        return Result.success("商品修改成功");
    }


    /**
     * 统计卖家商品数量
     * @return 结果
     */
    @GetMapping("/stats/seller/{sellerId}")
    public Result<GoodsSellerStatsVO> getGoodsCountBySeller(@PathVariable Long sellerId) {
        GoodsSellerStatsVO goodsSellerStatsVO = goodsService.getGoodsSellerStats(sellerId);
        return Result.success(goodsSellerStatsVO);
    }

    /**
     * 全量重建 ES 商品索引（初始化数据用，需登录）
     */
//    @PostMapping("/rebuild-index")
//    public Result<String> rebuildIndex() {
//        goodsService.rebuildElasticsearchIndex();
//        return Result.success("ES 商品索引重建完成");
//    }
}
