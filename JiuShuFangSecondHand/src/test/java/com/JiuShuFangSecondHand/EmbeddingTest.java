package com.JiuShuFangSecondHand;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.domain.pojo.Category;
import com.JiuShuFangSecondHand.domain.pojo.Goods;
import com.JiuShuFangSecondHand.domain.vo.GoodsToVectorVO;
import com.JiuShuFangSecondHand.service.EmbeddingService;
import com.JiuShuFangSecondHand.service.ICategoryService;
import com.JiuShuFangSecondHand.service.IGoodsService;
import com.JiuShuFangSecondHand.util.BeanUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class EmbeddingTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private IGoodsService goodsService;


    @Autowired
    private ICategoryService categoryService;


    /**
     * 批量生成商品向量 Embedding
     * 分页查询 + 分批处理，避免OOM
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    void embeddingTest() {
        // 分页参数
        long current = 1;
        long size = 1000;
        log.info("===== 开始批量生成商品Embedding，每页条数：{} =====", size);

        while (true) {
            // 1. 分页查询商品
            Page<Goods> goodsPage = goodsService.lambdaQuery()
                    .eq(Goods::getStatus, 1)
                    .eq(Goods::getIsDeleted, 0)
                    .orderByDesc(Goods::getCreateTime)
                    .page(new Page<>(current, size));
            List<Goods> records = goodsPage.getRecords();

            // 2. 无数据则退出循环
            if (CollectionUtil.isEmpty(records)) {
                log.info("===== 所有商品处理完成，总页数：{} =====", current - 1);
                break;
            }

            log.info("正在处理第 {} 页，商品数量：{}", current, records.size());

            try {
                // 3. 商品转JSON字符串
                List<String> jsonList = records.stream()
                        .map(k -> {
                            if (ObjectUtil.notEqual(k.getStatus(), 1)) {
                                log.debug("商品状态不是上架状态，不保存向量存储");
                                return null;
                            }
                            List<Category> list = categoryService.list();
                            Map<Long, String> collect = list.stream()
                                    .collect(Collectors.toMap(Category::getId, Category::getName));
                            GoodsToVectorVO goodsToVectorVO = BeanUtils.copyBean(k, GoodsToVectorVO.class);
                            goodsToVectorVO.setCategoryName(collect.getOrDefault(k.getCategoryId() , "其他书籍"));
//        goodsToVectorVO.setDegree(Objects.requireNonNull(Goods.DegreeEnum.getByCode(k.getDegree())).getDesc());
                            return goodsToVectorVO;
                        })
                        .filter(Objects::nonNull)
                        .map(JSONUtil::toJsonStr)
                        .toList();

                // 4. 调用向量生成服务
                for (List<String> strings : batchList(jsonList, 9)) {
                    embeddingService.saveGoodsInVectorStore(strings);
                }

                // 5. 可选：控制调用速度，避免接口限流（根据实际情况开启）
                // TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                log.error("第 {} 页商品生成Embedding失败", current, e);
                // 可选择中断或继续，这里选择中断便于排查问题
                throw new RuntimeException("Embedding生成失败，任务终止", e);
            }

            // 下一页
            current++;
        }
    }


    /**
     * 将List按指定大小分批
     * @param list 原始集合
     * @param batchSize 每批大小
     * @return 分批后的集合列表
     */
    public static <T> List<List<T>> batchList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return batches;
        }
        int total = list.size();
        int pageNum = total % batchSize == 0 ? total / batchSize : total / batchSize + 1;

        for (int i = 0; i < pageNum; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, total);
            batches.add(list.subList(start, end));
        }
        return batches;
    }


    @Test
    void deleteAllTest() {
        embeddingService.deleteAllGoodsFromVectorStore();
    }
}