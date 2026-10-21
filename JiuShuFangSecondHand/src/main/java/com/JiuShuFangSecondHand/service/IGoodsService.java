package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.GoodsUploadDto;
import com.JiuShuFangSecondHand.domain.query.GoodsQuery;
import com.JiuShuFangSecondHand.domain.vo.GoodsSellerStatsVO;
import com.JiuShuFangSecondHand.domain.vo.GoodsVO;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.JiuShuFangSecondHand.domain.pojo.Goods;

/**
 * <p>
 * 商品表（二手书） 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 分页查询商品（带用户收藏状态）
     *
     * @param query 商品查询参数
     * @return 分页结果
     */
    PageDTO<GoodsVO> getGoodsListWithLikeStatus(GoodsQuery query);

    /**
     * 根据 ID 查询商品详情（带用户收藏状态）
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    GoodsVO getGoodsByIdWithLikeStatus(Long id);

    /**
     * 修改商品状态
     *
     * @param id 商品 ID
     * @param status 商品状态
     */
    void updateGoodsStatus(Long id, Integer status);

    void sendToMqSaveOrUpdateVectorStore(Goods goods, boolean isSave);

    /**
     * 删除商品
     *
     * @param id 商品 ID
     */
    void deleteGoods(Long id);

    /**
     * 上架商品
     *
     * @param dto 上架商品 DTO
     */
    void uploadGoods(GoodsUploadDto dto);

    void updateGoods(GoodsUploadDto dto);

    GoodsSellerStatsVO getGoodsSellerStats(Long userId);

    /**
     * 全量重建 ES 商品索引（用于初始化数据）
     */
//    void rebuildElasticsearchIndex();
}
