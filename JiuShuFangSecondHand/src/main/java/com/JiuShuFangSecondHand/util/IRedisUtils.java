package com.JiuShuFangSecondHand.util;

public interface IRedisUtils {

    void delOrderIdInRedis(Long id , String type);

    String getStringHashValue(String key, String string);
}
