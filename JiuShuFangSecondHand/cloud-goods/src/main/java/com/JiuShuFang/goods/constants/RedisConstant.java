package com.JiuShuFang.goods.constants;

public interface RedisConstant {
    //登录注册相关
    String LOGIN_CODE_KEY = "login:code:";
    String LOGIN_ID_KEY = "login:id:";
    String LOGIN_USERINFO_KEY = "login:userinfo:";
    String LOGIN_TOKEN_KEY = "login:token:";
    
    //验证码相关
    String CODE_SEND_KEY = "code:send:";

    //minio 有关
    String MINIO_BOOK_BUCKET_NAME = "minio:book";

    //书籍评分相关
    String BOOK_SCORE_AMOUNT = "book:score:amount:";
    String BOOK_SCORE_AVERAGE = "book:score:average:";

    //用户收藏相关
    String COLLECT_USER_KEY = "collect:user:";
    
    //用户浏览历史相关
    String BOOK_HISTORY_KEY = "book:history:";


    //订单校验状态
    String ORDER_CHECK_KEY = "order:check";


    //聊天Session相关
    String CHAT_SESSION_KEY = "chat:session:";



    //ai聊天记录相关
    String CHAT_MEMORY_LIST_KEY = "chat:memory:list:";


    //秒杀相关
    String SECKILL_STOCK_KEY = "seckill:stock:";        //String，预减库存
    String SECKILL_USERS_KEY = "seckill:users:";        //Set，已抢到用户（去重）
    String SECKILL_PENDING_KEY = "seckill:pending:";    //String，在途计数器
    String SECKILL_RESULT_KEY = "seckill:result:";      //String，seckill:result:{id}:{userId} 结果轮询
    String SECKILL_LIMIT_KEY = "seckill:limit:";        //String，seckill:limit:{id}:{秒} 秒级限流
}
