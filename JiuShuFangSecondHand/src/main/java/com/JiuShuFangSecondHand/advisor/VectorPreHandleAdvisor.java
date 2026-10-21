package com.JiuShuFangSecondHand.advisor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class VectorPreHandleAdvisor implements BaseAdvisor {
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        String qaRetrievedDocuments = "qa_retrieved_documents";
        Object o = context.get(qaRetrievedDocuments);
        if(!(o instanceof List<?>)){
            log.info("VectorPreHandleAdvisor: qa_retrieved_documents is not a List, type={}", o == null ? "null" : o.getClass().getName());
            return chatClientRequest;
        }
        List<Document> documents = (List<Document>) o;
        if(CollectionUtil.isEmpty(documents)){
            log.info("VectorPreHandleAdvisor: documents is empty");
            return chatClientRequest;
        }
        log.info("VectorPreHandleAdvisor: before filter, documents count = {}", documents.size());

        Long userId = UserContext.getUserId();
        log.info("VectorPreHandleAdvisor: current userId = {}", userId);

        List<Document> finalDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String text = document.getText();
            log.info("VectorPreHandleAdvisor: document[{}] text = {}", i, text);

            try {
                JSONObject entries = JSONUtil.parseObj(text);

                // 尝试获取 userId 字段
                Long docUserId = null;
                if (entries.containsKey("userId")) {
                    docUserId = Long.valueOf(entries.getStr("userId"));
                } else if (entries.containsKey("id")) {
                    // 尝试用 id 字段
                    docUserId = Long.valueOf(entries.getStr("id"));
                }

                log.info("VectorPreHandleAdvisor: document[{}] docUserId = {}, currentUserId = {}, equals = {}",
                        i, docUserId, userId, docUserId != null && docUserId.equals(userId));

                if (docUserId != null && docUserId.equals(userId)) {
                    log.info("VectorPreHandleAdvisor: filtering out document[{}] with userId = {}", i, docUserId);
                    continue;
                }
            } catch (Exception e) {
                log.error("VectorPreHandleAdvisor: failed to parse document[{}], error = {}", i, e.getMessage());
            }
            finalDocuments.add(document);
        }
        log.info("VectorPreHandleAdvisor: after filter, documents count = {} (filtered {} documents)",
                finalDocuments.size(), documents.size() - finalDocuments.size());
        context.put(qaRetrievedDocuments , finalDocuments);
        return chatClientRequest.mutate().context(context).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {

        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
