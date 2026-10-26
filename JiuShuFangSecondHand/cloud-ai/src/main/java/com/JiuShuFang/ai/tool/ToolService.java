package com.JiuShuFang.ai.tool;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
// 微服务拆分：以下跨服务类属于 cloud-goods / cloud-trade，第一阶段注释，后续改由 Feign 调用
// import com.JiuShuFang.ai.domain.dto.OrderDTO;
// import com.JiuShuFang.ai.domain.dto.OrderItemDTO;
// import com.JiuShuFang.ai.domain.pojo.Address;
// import com.JiuShuFang.ai.domain.pojo.Category;
// import com.JiuShuFang.ai.domain.pojo.Goods;
// import com.JiuShuFang.ai.domain.query.GoodsQuery;
import com.JiuShuFang.ai.domain.query.PageQuery;
import com.JiuShuFang.ai.domain.tool.ToolCategoryVO;
import com.JiuShuFang.ai.domain.tool.ToolGoodsDTO;
import com.JiuShuFang.ai.domain.tool.ToolGoodsVO;
import com.JiuShuFang.common.util.*;
// import com.JiuShuFang.ai.domain.vo.CategoryVO;
import com.JiuShuFang.ai.domain.vo.GoodsToVectorVO;
// import com.JiuShuFang.ai.domain.vo.GoodsVO;
import com.JiuShuFang.ai.domain.vo.OrderDetailVO;
import com.JiuShuFang.common.exception.BusinessException;
// import com.JiuShuFang.ai.service.IAddressService;
// import com.JiuShuFang.ai.service.ICategoryService;
// import com.JiuShuFang.ai.service.IGoodsService;
// import com.JiuShuFang.ai.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToolService {

    // 微服务拆分：categoryService/goodsService/orderService/addressService 属于其他服务，第一阶段注释，后续改由 Feign 注入
    // private final ICategoryService categoryService;
    // private final IGoodsService goodsService;
    // private final IOrderService orderService;
    // private final IAddressService addressService;
    private final VectorStore vectorStore;  // Milvus 已启用


  @Tool(name = "queryBook", description = "【查询书籍】当用户需要【查找/搜索/购买】本商场的书籍时调用此工具。例如：帮我找文学类的书、推荐几本考研的书、有没有《哈利波特》、我要买这本。<br>" +
            "【参数规则】category 优先传 queryBookCategory 返回的分类 id；如果用户说的是'计算机类/编程技术'这类大类口语，转成最接近的分类名称传入（如 计算机编程）。<br>" +
            "【禁止】name 只填用户明确说出的书名（如 哈利波特），禁止把分类名或类别词填入 name。<br>" +
            "【禁止】如果用户正在【上架卖书】流程中（已记录书名），禁止调用此工具，应继续收集上架信息。<br>" +
            "【禁止】如果用户只是提供书名想卖书，禁止调用此工具。<br>" +
            "必须同时满足：用户有明确查找/购买意图，且不是在卖书流程中。")
    public List<ToolGoodsVO> queryBook(@ToolParam(description = "商品名称,例如:哈利波特。仅当用户明确说出书名时填写,否则不要填", required = false) String name,
                                       @ToolParam(description = "商品分类名称或id,例如:四六级,编程,100 , 101 ", required = false) String category,
                                       @ToolParam(description = "商品作者,例如:张三,李四。仅当用户明确说出作者名时填写,否则不要填", required = false) String author) {
        // 微服务拆分：依赖 cloud-goods / cloud-category 服务，第一阶段注释，后续改由 Feign 调用
        // // 参数防呆：空白字符串一律视为未提供，避免生成 LIKE '%%' 或按垃圾词过滤导致查不到书
        // if (name != null && name.isBlank()) name = null;
        // if (category != null && category.isBlank()) category = null;
        // if (author != null && author.isBlank()) author = null;
        // List<Long> categoryIds;
        // if (category == null) {
        //     // 未提供分类：不按分类过滤，返回全部匹配书籍（按收藏/销量排序后取前5）
        //     categoryIds = new ArrayList<>();
        // } else if (!category.matches("\\d+")) {
        //     List<Category> list = categoryService.lambdaQuery().like(Category::getName, category)
        //             .select(Category::getId)
        //             .list();
        //     if (CollUtils.isEmpty(list)) {
        //         categoryIds = new ArrayList<>();
        //     } else {
        //         categoryIds = list.stream().map(Category::getId).toList();
        //     }
        // } else {
        //     categoryIds = List.of(Long.parseLong(category));
        // }
        // GoodsQuery goodsQuery = GoodsQuery.builder()
        //         .bookName(name)
        //         .categoryIds(categoryIds)
        //         .author(author)
        //         .build();
        // PageDTO<GoodsVO> pageDTO = goodsService.getGoodsListWithLikeStatus(goodsQuery);
        // return pageDTO.getList().stream().sorted(
        //                 (o1, o2) -> {
        //                     // 先比较是否收藏
        //                     if (o1.getIsLike() != o2.getIsLike()) {
        //                         return o1.getIsLike() ? -1 : 1;
        //                     }
        //                     // 再比较销量
        //                     return Integer.compare(o2.getTotalNumber() - o2.getStock()
        //                             , o1.getTotalNumber() - o1.getStock());
        //                 }).limit(5).map(k ->
        //                 BeanUtils.copyBean(k, ToolGoodsVO.class))
        //         .toList();
        log.warn("[queryBook] 跨服务调用暂未接入 Feign（cloud-goods），返回空列表");
        return new ArrayList<>();
    }

    // Milvus 已停用：暂时从工具注册中摘除（@Tool 注释掉），避免模型调用向量搜索返回空。
    // 恢复 Milvus 时取消下面 @Tool 注解的注释即可重新启用。
    // @Tool(name = "searchByRag",
    //         description = "【智能搜索书籍】当用户想要【查找/搜索/购买】书籍时调用此工具。根据用户描述进行向量相似度搜索，返回匹配的书籍列表。<br>" +
    //                 "【重要】此工具会自动过滤掉用户自己上架的书籍，只返回其他卖家上架的书籍。<br>" +
    //                 "【重要】必须严格使用此工具返回的书籍数据进行回复，禁止自行编造或补充任何书籍信息。<br>" +
    //                 "例如：帮我找文学类的书、推荐几本考研的书、有没有《哈利波特》、我要买这本、有没有什么算法相关的书。")
    public List<GoodsToVectorVO> searchByRag(@ToolParam(description = "搜索关键词，可以是书名、作者、类型、描述等", required = true) String query) {
        log.info("[searchByRag] 调用，query={}", query);

        Long currentUserId = UserContext.getUserId();
        log.info("[searchByRag] 当前用户ID={}", currentUserId);

        // 执行向量搜索
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(10)  // 多检索一些，后续过滤
                .similarityThreshold(0.5f)
                .build();

        List<org.springframework.ai.document.Document> documents = vectorStore.similaritySearch(searchRequest);
        log.info("[searchByRag] 向量搜索返回 {} 条文档", documents.size());

        // 过滤掉自己上架的书籍，并转换为 VO
        List<GoodsToVectorVO> result = new ArrayList<>();
        for (org.springframework.ai.document.Document doc : documents) {
            try {
                String text = doc.getText();
                JSONObject entries = JSONUtil.parseObj(text);

                Long docUserId = Long.valueOf(entries.getStr("userId"));
                log.info("[searchByRag] 文档 userId={}, currentUserId={}, 过滤={}",
                        docUserId, currentUserId, docUserId.equals(currentUserId));

                // 过滤掉自己上架的书籍
                if (docUserId.equals(currentUserId)) {
                    continue;
                }

                GoodsToVectorVO vo = JSONUtil.toBean(entries, GoodsToVectorVO.class);
                result.add(vo);
            } catch (Exception e) {
                log.error("[searchByRag] 解析文档失败: {}", e.getMessage());
            }
        }

        log.info("[searchByRag] 过滤后返回 {} 条书籍", result.size());
        return result;
    }

    @Tool(name = "queryBookCategory",
            description = "【查询分类】当用户想【查找/浏览/搜索】书籍分类时调用此工具。例如：有哪些分类、帮我看看有什么书。<br>" +
                    "【禁止】如果用户正在上架卖书流程中，禁止调用此工具。<br>" +
                    "【禁止】如果用户已有明确书名或购买意图，禁止调用此工具。<br>" +
                    "必须同时满足：用户想浏览/查找书籍分类，且不是在卖书流程中。")
    public List<ToolCategoryVO> queryBookCategory() {
        // 微服务拆分：依赖 cloud-goods 的分类服务，第一阶段注释，后续改由 Feign 调用
        // PageDTO<CategoryVO> categoryList = categoryService.getCategoryList(new PageQuery());
        // return BeanUtils.copyList(categoryList.getList(), ToolCategoryVO.class);
        log.warn("[queryBookCategory] 跨服务调用暂未接入 Feign（cloud-goods），返回空列表");
        return new ArrayList<>();
    }


    @Tool(name = "buyBook",
            description = "用户想要购买书籍/帮我下单相关要求的时候必须调用此工具,如果用户只是想要筛选想要的书籍就暂时不调用,如果抛出异常则返回异常信息,无需填写其他字段，禁止直接回答")
    public OrderDetailVO buyBook(@ToolParam(description = "书籍id集合 , 例如 [1,2,3]")List<Long> bookIds,
                                     @ToolParam(description = "所购买的书籍数量,必须和书籍id位置一一对应 , 例如 : [1,1,1]")List<Integer> num) {
        // 微服务拆分：依赖 cloud-goods / cloud-trade / 地址服务，第一阶段注释，后续改由 Feign 调用
        // log.info("[buyBook] 调用，bookIds={}, num={}", bookIds, num);
        // //拼接
        // List<OrderItemDTO> orderItemDTOS = IntStream.range(0, bookIds.size())
        //         .mapToObj(i -> {
        //             OrderItemDTO orderItem = new OrderItemDTO();
        //             orderItem.setGoodsId(bookIds.get(i));
        //             orderItem.setNum(num.get(i));
        //             return orderItem;
        //         }).toList();
        // //计算总价
        // List<Goods> goods = goodsService.listByIds(bookIds);
        // BigDecimal totalPrice = new BigDecimal(0);
        // for (int i = 0; i < goods.size(); i++) {
        //     BigDecimal price = goods.get(i).getPrice();
        //     Integer num1 = num.get(i);
        //     BigDecimal multiply = price.multiply(new BigDecimal(num1));
        //     totalPrice = totalPrice.add(multiply);
        // }
        // //获取默认地址
        // Address one = addressService.lambdaQuery()
        //         .eq(Address::getUserId, UserContext.getUserId())
        //         .eq(Address::getIsDefault, 1)
        //         .one();
        // if(one == null){
        //     log.error("用户没有默认地址");
        //     throw new BusinessException("用户没有默认地址");
        // }
        // //创建订单
        // OrderDTO orderDTO = new OrderDTO();
        // orderDTO.setOrderItemDTOList(orderItemDTOS);
        // orderDTO.setTotalPrice(totalPrice);
        // orderDTO.setAddressId(one.getId());
        // orderDTO.setIsClearCart(false);
        // //创建订单并获取订单id
        // Result<Long> order = orderService.createOrder(orderDTO);
        // Long data = order.getData();
        // //根据订单id拿到订单详情
        // Result<OrderDetailVO> orderDetail = orderService.getOrderDetail(data);
        // return orderDetail.getData();
        log.warn("[buyBook] 跨服务调用暂未接入 Feign（cloud-trade），返回 null");
        return null;
    }

    @Tool(name = "listBook",
            description = "当用户想要上架商品的时候必须调用此工具,调用之前必须要确定用户已经填写完所有数据")
    public void listBook(@ToolParam(description = "书籍对象") ToolGoodsDTO goodsDTO) {
        Assert.notNull(goodsDTO , "书籍对象不能为空");
        // 微服务拆分：依赖 cloud-goods，第一阶段注释，后续改由 Feign 调用
        // Long userId = UserContext.getUserId();
        // Goods goods = BeanUtils.copyBean(goodsDTO, Goods.class);
        // goods.setUserId(userId);
        // goods.setStock(1);
        // goodsService.save(goods);
        log.warn("[listBook] 跨服务调用暂未接入 Feign（cloud-goods），已跳过保存");
    }

}

// OrderDetailVO 返回类型说明：该类属于 cloud-trade，第一阶段用占位保留签名
