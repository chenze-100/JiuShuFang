package com.JiuShuFang.trade.config;

 import com.alipay.api.AlipayClient;
 import com.alipay.api.DefaultAlipayClient;
 import com.JiuShuFang.trade.properties.AlipayProperties;
import lombok.RequiredArgsConstructor;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AliPayConfig {

     private final AlipayProperties alipayProperties;

     @Bean
     public AlipayClient alipayClient() {
         return new DefaultAlipayClient(
                 alipayProperties.getServerUrl(),
                 alipayProperties.getAppId(),
                 alipayProperties.getPrivateKey(),
                 alipayProperties.getFormat(),
                 alipayProperties.getCharset(),
                 alipayProperties.getAlipayPublicKey(),
                 alipayProperties.getSignType()
         );
     }
}
