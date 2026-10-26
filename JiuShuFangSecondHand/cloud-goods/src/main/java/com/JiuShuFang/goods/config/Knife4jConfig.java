package com.JiuShuFang.goods.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("旧书坊 - 二手书交易平台 API")
                        .description("二手书交易平台的 RESTful API 文档，包含用户、商品、订单、购物车、聊天等模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("知阅团队")
                                .email("support@zhiyue.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.zhiyue.com").description("生产环境")
                ));
    }


    /**
     * 【关键】这里填你 Controller 所在的 包路径！
     * 例如：com.example.demo.controller
     */
    @Bean
    public GroupedOpenApi testApi() {
        return GroupedOpenApi.builder()
                .group("ap") // 分组名称
                .pathsToMatch("/**") // 匹配所有路径的接口
                .build();
    }

}
