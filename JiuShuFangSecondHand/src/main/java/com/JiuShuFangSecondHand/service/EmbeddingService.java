package com.JiuShuFangSecondHand.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmbeddingService {
    void saveGoodsInVectorStore(List<String> texts);

    void deleteAllGoodsFromVectorStore();

    void updateGoodsInVectorStore(String text);

    void deleteGoodsFromVectorStore(String doc_id);
}
