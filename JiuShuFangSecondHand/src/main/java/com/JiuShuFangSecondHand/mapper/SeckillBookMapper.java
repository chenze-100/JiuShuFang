package com.JiuShuFangSecondHand.mapper;

import com.JiuShuFangSecondHand.domain.pojo.SeckillBook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 秒杀书籍表 Mapper 接口
 * </p>
 *
 * @since 2026-10-14
 */
@Mapper
public interface SeckillBookMapper extends BaseMapper<SeckillBook> {

    /**
     * 数据库侧原子扣减（兜底防线）：只有 seckill_stock > 0 才会更新成功
     *
     * @return 影响行数，0 表示已售罄
     */
    @Update("update seckill_book set seckill_stock = seckill_stock - 1 " +
            "where id = #{id} and seckill_stock > 0 and is_deleted = 0")
    int deductStock(@Param("id") Long id);

    /**
     * 压测复位：把库存还原成初始值（resetStock 仅压测链路使用）
     */
    @Update("update seckill_book set seckill_stock = #{stock}, status = 1 where id = #{id}")
    int resetStock(@Param("id") Long id, @Param("stock") Integer stock);

    /**
     * 压测复位辅助：直接按给定剩余库存覆盖（层 A / 层 C 场景）
     */
    @Update("update seckill_book set seckill_stock = #{stock} where id = #{id}")
    int forceStock(@Param("id") Long id, @Param("stock") Integer stock);
}
