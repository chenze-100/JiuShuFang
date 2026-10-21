package com.JiuShuFangSecondHand;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.JiuShuFangSecondHand.domain.pojo.Comment;
import com.JiuShuFangSecondHand.domain.pojo.Goods;
import com.JiuShuFangSecondHand.factory.FileStorageFactory;
import com.JiuShuFangSecondHand.service.ICommentService;
import com.JiuShuFangSecondHand.service.IGoodsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
class JiuShuFangSecondHandApplicationTests {

    @Autowired
    private FileStorageFactory fileStorageFactory;

    @Autowired
    private IGoodsService goodsService;

    @Test
    void contextLoads() {
        QrCodeUtil.generate("https://ys.mihoyo.com/cloud/m/#/",
                200, 200,
                FileUtil.file("D:\\qrCode.png"));
    }

    @Test
    void minioInput() throws FileNotFoundException {
        String pathUrl = "D:\\javaSpringAI\\";
        List<String> filenameList = new ArrayList<>(15);
        for (int i = 1; i <= 15; i++) {
            String fileName = pathUrl + i + ".jpg";
            String ran = UUID.fastUUID().toString(true) + ".jpg";
            String s = fileStorageFactory.uploadImgFile(ran, new FileInputStream(fileName));
            filenameList.add(s);
        }
        List<Goods> list = goodsService.list();
        List<Goods> goodsList = new ArrayList<>(100);
        int i = 0;
        for (Goods goods : list) {
            i++;
            goods.setBookImg(filenameList.get(RandomUtil.randomInt(0, 14)));
            goodsList.add(goods);
            if (i % 100 == 0) {
                goodsService.updateBatchById(goodsList);
                goodsList.clear();
            }
        }
        goodsService.updateBatchById(goodsList);

    }


    @Autowired
    private ICommentService commentService;

    private final Random random = new Random();

    // 中文评论池（随机抽取）
    private final String[] contents = {
            "这本书内容非常棒，值得反复阅读！",
            "印刷清晰，纸质很好，性价比很高",
            "内容很实用，学到了很多知识",
            "故事很精彩，一口气看完了",
            "包装完好，发货速度快，非常满意",
            "知识点讲解详细，通俗易懂",
            "非常喜欢的一本书，推荐大家购买",
            "内容丰富，逻辑清晰，受益匪浅",
            "正版书籍，质量有保障",
            "文笔流畅，读起来很舒服",
            "对我帮助很大，强烈推荐",
            "内容深入浅出，非常适合新手",
            "书籍品相很好，没有破损",
            "经典好书，值得收藏",
            "内容干货满满，没有废话"
    };

    @Test
    public void batchInsertComments() {
        // 书籍ID范围：1 ~ 1018
        int startBookId = 1;
        int endBookId = 1018;

        // 每本书生成 100 条评论
        int commentsPerBook = 100;

        // 批量插入，每次1000条，避免内存溢出
        List<Comment> batchList = new ArrayList<>(1000);

        for (long bookId = startBookId; bookId <= endBookId; bookId++) {
            System.out.println("正在生成书籍ID：" + bookId + " 的评论");

            for (int i = 0; i < commentsPerBook; i++) {
                Comment comment = new Comment();
                comment.setGoodsId(bookId);
                // 随机用户ID 1000 ~ 99999
                comment.setUserId(1000L + random.nextInt(99000));
                // 随机评论内容
                comment.setContent(contents[random.nextInt(contents.length)]);
                // 随机评分 1-5星
                comment.setScore(random.nextInt(5) + 1);
                comment.setCreateTime(LocalDateTime.now());
                comment.setIsDeleted(0);

                batchList.add(comment);

                // 每满1000条执行一次插入
                if (batchList.size() >= 1000) {
                    commentService.saveBatch(batchList);
                    batchList.clear();
                }
            }
        }

        // 插入剩余数据
        if (!batchList.isEmpty()) {
            commentService.saveBatch(batchList);
        }

        System.out.println("===== 所有评论插入完成！=====");
    }

}
