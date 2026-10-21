package com.JiuShuFangSecondHand.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "login")
public class LoginProperties {

    private List<String> includePaths;

    private List<String> excludePaths;
}
