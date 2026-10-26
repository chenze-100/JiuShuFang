package com.JiuShuFang.common.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class AIJsonUtil {


    /**
     * 从文本中提取出 ```json...``` 部分
     * @param text
     * @return
     */
    public static String extractJson(String text) {
        // 匹配 ```json 开头，``` 结尾的内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "```json\\s*(.*?)\\s*```",
                java.util.regex.Pattern.DOTALL
        );

        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // 返回匹配到的纯JSON
            String trim = matcher.group(1).trim();
            JSONObject entries = JSONUtil.parseObj(trim);
            Object o = entries.get("createTime");
            if (o instanceof List) {
                List<Integer> list = (List<Integer>) o;
                LocalDateTime localDateTime = parseAiTime(list);
                entries.set("createTime", localDateTime);
            }
            trim = JSONUtil.toJsonStr(entries);
            return trim;
        }
        return null;
    }


    /**
     * 从文本中提取出 ```json...``` 部分
     * @param text
     * @return
     */
    public static <T> T extractJson(String text, Class<T> clazz) {
        String json = extractJson(text);
        if(json == null){
            return null;
        }
        return JSONUtil.toBean(json, clazz);
    }


    private static LocalDateTime parseAiTime(List<Integer> time) {
        if (time == null || time.size() < 6) return null;
        return LocalDateTime.of(
                time.get(0),
                time.get(1),
                time.get(2),
                time.get(3),
                time.get(4),
                time.get(5)
        );
    }

    /**
     * 把文本中的 ```json...``` 替换成新的 JSON
     * @param originalText 原始文本
     * @param newJson 要放回去的新 JSON
     * @return 替换后的完整文本
     */
    public static String replaceJson(String originalText, String newJson) {
        String regex = "(?s)```json\\s*.*?\\s*```";

        // 执行替换
        return originalText.replaceAll(
                regex,
                "```json\n" + newJson + "\n```"
        );
    }


    /**
     * 把文本中的 ```json...``` 替换成新的 JSON
     * @param originalText 原始文本的对象
     * @param newObject 要放回去的新 JSON
     * @return 替换后的完整文本
     */
    public static <T> String replaceJson(String originalText, T newObject) {
        // 生成新JSON
        String newJson = JSONUtil.toJsonStr(newObject);
        return replaceJson(originalText, newJson);
    }
}
