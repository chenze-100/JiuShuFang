package com.JiuShuFangSecondHand.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.JiuShuFangSecondHand.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "向量嵌入", description = "向量嵌入相关接口")

@RestController  // Milvus 已停用：注释 Bean（EmbeddingServiceImpl 已停用），恢复时取消注释
@RequestMapping("/embedding")
@RequiredArgsConstructor
@Slf4j
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    /**
     * 向量嵌入(批量)
     * @param texts
     */
    @PostMapping
    public void saveGoodsInVectorStore(@RequestParam("texts") List<String> texts) {
        embeddingService.saveGoodsInVectorStore(texts);
    }


    /**
     * 删除向量库中所有商品数据
     */
    @DeleteMapping
    public void deleteAllGoodsFromVectorStore() {
        embeddingService.deleteAllGoodsFromVectorStore();
    }


    /**
     * 删除向量库中商品数据
     */
    @DeleteMapping("/{doc_id}")
    public void deleteGoodsFromVectorStore(@PathVariable String doc_id) {
        embeddingService.deleteGoodsFromVectorStore(doc_id);
    }


    @PutMapping
    public void updateGoodsInVectorStore(@RequestParam("text") String text) {
        embeddingService.updateGoodsInVectorStore(text);
    }
}
