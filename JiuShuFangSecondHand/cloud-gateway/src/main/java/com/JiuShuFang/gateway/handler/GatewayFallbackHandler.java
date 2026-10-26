package com.JiuShuFang.gateway.handler;

import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GatewayFallbackHandler {

    public Mono<ServerResponse> handle(String serviceName, Throwable t) {
        HttpStatus status = (t instanceof NotFoundException) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;
        return ServerResponse.status(status).bodyValue(
                "{\"code\":" + status.value() + ",\"msg\":\"服务[" + serviceName + "]暂不可用，请稍后重试\"}"
        );
    }
}
