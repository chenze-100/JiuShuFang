package com.JiuShuFang.ai.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.ai.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service  // Milvus 已启用
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final VectorStore vectorStore;

    @Override
    public void saveGoodsInVectorStore(List<String> texts) {
        Assert.notEmpty(texts, "商品列表不能为空");
        List<Document> list = texts.stream().map(
                text -> Optional.ofNullable(text)
                        .map(JSONUtil::parseObj)
                        .map(this::saveOneText)
                        .orElseThrow(() -> new BusinessException("商品id不能为空"))
        ).toList();
        vectorStore.accept(list);
    }

    @NotNull
    private Document saveOneText(JSONObject entries) {
        String id = entries.getStr("id");
        Assert.notBlank(id, "商品id不能为空");
        return Document.builder()
                .id(id)
                .metadata(Map.of("schema", "goods", "id", id))
                .text(JSONUtil.toJsonStr(entries))
                .build();
    }

    @Override
    public void deleteAllGoodsFromVectorStore() {
        vectorStore.delete(
                new Filter.Expression(
                        Filter.ExpressionType.EQ,
                        new Filter.Key("schema"),
                        new Filter.Value("goods")
                )
        );
    }

    @Override
    public void updateGoodsInVectorStore(String text) {
        Optional.ofNullable(text)
                .map(JSONUtil::parseObj)
                .ifPresentOrElse(entries -> {
                            deleteOneText(entries);
                            saveOneText(entries);
                        },
                        () -> {
                            throw new BusinessException("商品id不能为空");
                        }
                );
    }

    private void deleteOneText(JSONObject entries) {
        String id = entries.getStr("id");
        Assert.notBlank(id, "商品id不能为空");
        Filter.Expression expression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("doc_id"),
                new Filter.Value(id));
        vectorStore.delete(expression);
    }

    private void deleteOneText(String doc_id) {
        Assert.notBlank(doc_id, "商品id不能为空");
        Filter.Expression expression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("doc_id"),
                new Filter.Value(doc_id));
        vectorStore.delete(expression);
    }

    @Override
    public void deleteGoodsFromVectorStore(String doc_id) {
        Optional.ofNullable(doc_id)
                .ifPresentOrElse(this::deleteOneText,
                        () -> {
                            throw new BusinessException("商品id不能为空");
                        }
                );
    }
}
