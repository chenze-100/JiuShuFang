package com.JiuShuFangSecondHand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

// 排除 Milvus 向量库自动配置：本地不启动 Milvus 时项目也能正常启动（向量相关功能已注释）
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.JiuShuFangSecondHand.mapper")
@EnableScheduling
public class JiuShuFangSecondHandApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiuShuFangSecondHandApplication.class, args);
    }


}
