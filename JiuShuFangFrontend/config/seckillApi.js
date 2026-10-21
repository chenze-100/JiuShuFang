/**
 * 限量书秒杀 API 封装
 */
import request from '../request.js'

// 秒杀活动列表
export const listSeckill = () =>
  request('/seckill/list').then(r => r.data)

// 秒杀详情（含 Redis 实时剩余库存与当前用户参与状态）
export const getSeckillDetail = (seckillId) =>
  request(`/seckill/detail/${seckillId}`).then(r => r.data)

// 抢购（Redis 预减 + MQ 异步下单，同步返回"排队中"）
export const doSeckill = (seckillId) =>
  request('/seckill/order', {
    method: 'POST',
    body: JSON.stringify({ seckillId })
  })

// 轮询抢购结果
export const getSeckillResult = (seckillId) =>
  request(`/seckill/result?seckillId=${seckillId}`).then(r => r.data)

// 我的秒杀订单
export const listMySeckillOrders = () =>
  request('/seckill/my-orders').then(r => r.data)
