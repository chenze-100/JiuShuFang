/**
 * 商铺相关 API 封装
 */
import request from '../request.js'

// 添加商铺前置检查
export const checkBeforeAdd = () =>
  request('/api/shop/check-before-add').then(r => r.data)

// 添加商铺
export const addShop = (data) =>
  request('/api/shop/add', {
    method: 'POST',
    body: JSON.stringify(data)
  })

// 查询我的商铺列表
export const listMyShops = () =>
  request('/api/shop/list/my').then(r => r.data)

// 查询商铺详情
export const getShopById = (id) =>
  request(`/shop/${id}`).then(r => r.data)
