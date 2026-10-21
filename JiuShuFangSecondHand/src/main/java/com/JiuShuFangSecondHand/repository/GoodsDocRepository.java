package com.JiuShuFangSecondHand.repository;

import com.JiuShuFangSecondHand.domain.pojo.GoodsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品 ES 仓储
 * 注意：不能放在 mapper 包下，否则会被 MyBatis @MapperScan 扫描导致 Bean 冲突
 */
public interface GoodsDocRepository extends ElasticsearchRepository<GoodsDoc, Long> {
}
