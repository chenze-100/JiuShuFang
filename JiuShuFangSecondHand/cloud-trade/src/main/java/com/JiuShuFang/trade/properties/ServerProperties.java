package com.JiuShuFang.trade.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    private String externalUrl;


    private String VueUrl;
}
