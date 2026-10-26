package com.JiuShuFang.common.api;

import com.JiuShuFang.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 所有微服务内部调用的 Feign Client 接口都放在 cloud-common 的 api 子包下
@FeignClient(name = "cloud-goods", path = "/inner/goods")
public interface GoodsApi {

    @GetMapping("/{id}")
    Result<?> findById(@PathVariable("id") Long id);
}
