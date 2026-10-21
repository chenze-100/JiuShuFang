package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.CollectDto;
import com.JiuShuFangSecondHand.domain.vo.GoodsVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.JiuShuFangSecondHand.domain.pojo.Collect;

import java.util.List;

/**
 * <p>
 * 用户收藏表 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface ICollectService extends IService<Collect> {

    /**
     * 添加收藏
     *
     * @param dto 收藏DTO
     */
    void addCollect(CollectDto dto);

    /**
     * 分页查询用户收藏
     *
     * @return 分页结果
     */
    List<GoodsVO> getCollectList();
}
