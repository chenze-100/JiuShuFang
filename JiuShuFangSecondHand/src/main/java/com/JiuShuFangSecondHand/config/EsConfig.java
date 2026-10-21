package com.JiuShuFangSecondHand.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Elasticsearch 配置
 * ES 8 默认开启 HTTPS + 账号密码（自签名证书）
 * 这里配置 basic 认证并信任本地自签名证书（仅限本地开发环境）
 */
@Configuration
public class EsConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:https://localhost:9200}")
    private String uris;

    @Value("${spring.elasticsearch.username:elastic}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        // 去掉协议前缀，connectedTo 只需要 host:port
        String hostAndPort = uris.replace("https://", "").replace("http://", "");
        try {
            // 信任所有证书（自签名证书），仅本地开发使用
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            HostnameVerifier allowAllHosts = (hostname, session) -> true;

            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort)
                    .usingSsl(sslContext, allowAllHosts)
                    .withBasicAuth(username, password)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch SSL 配置失败", e);
        }
    }
}
