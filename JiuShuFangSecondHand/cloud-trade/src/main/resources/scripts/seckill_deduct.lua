-- 秒杀库存预减脚本（一次 Redis 往返完成"去重 + 判库存 - DECR + SADD + INCR 在途"）
-- KEYS[1] = seckill:stock:{id}    库存
-- KEYS[2] = seckill:users:{id}    已抢到用户集合
-- KEYS[3] = seckill:pending:{id}  在途计数器
-- ARGV[1] = userId
-- 返回：1 = 抢到   0 = 已售罄   -1 = 重复下单
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1
end
local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock <= 0 then
    return 0
end
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
redis.call('INCR', KEYS[3])
return 1
