package com.JiuShuFangSecondHand.config;

import com.JiuShuFangSecondHand.properties.MinIOConfigProperties;
import io.minio.MinioClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties({MinIOConfigProperties.class})
public class MinIOConfig {

    private final MinIOConfigProperties minIOConfigProperties;

    @Bean
    public MinioClient buildMinioClient() {
        return MinioClient
                .builder()
                .credentials(minIOConfigProperties.getAccessKey(),
                        minIOConfigProperties.getSecretKey())
                .endpoint(minIOConfigProperties.getEndpoint())
                .build();
    }
}