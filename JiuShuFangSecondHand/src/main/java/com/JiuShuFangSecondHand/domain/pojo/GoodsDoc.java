package com.JiuShuFangSecondHand.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 ES 文档
 * 用于商品搜索（书名全文检索）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "goods")
public class GoodsDoc {

    @Id
    private Long id;

    /** 书籍名称（全文检索字段） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String bookName;

    /** 作者 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String author;

    /** 出版社 */
    @Field(type = FieldType.Keyword)
    private String publisher;

    /** 描述 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    /** 价格 */
    @Field(type = FieldType.Double)
    private BigDecimal price;

    /** 分类ID */
    @Field(type = FieldType.Long)
    private Long categoryId;

    /** 卖家ID */
    @Field(type = FieldType.Long)
    private Long userId;

    /** 状态：1上架 2下架 3已售 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 逻辑删除 0未删 1已删 */
    @Field(type = FieldType.Integer)
    private Integer isDeleted;

    /** 成色 */
    @Field(type = FieldType.Integer)
    private Integer degree;

    /** 封面图 */
    @Field(type = FieldType.Keyword, index = false)
    private String bookImg;

    /** 创建时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
}
