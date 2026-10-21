// 全局请求拦截器（已适配你的 userInfo 结构）
const request = async (url, options = {}) => {
  try {
    // 统一添加 /api 前缀
    let apiUrl = url.startsWith('/api') ? url : `/api${url}`
    
    // 处理 GET 请求的查询参数
    if (options.params && Object.keys(options.params).length > 0) {
      const queryString = Object.keys(options.params)
        .filter(key => options.params[key] !== undefined && options.params[key] !== null)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
        .join('&')
      
      if (queryString) {
        apiUrl += (apiUrl.includes('?') ? '&' : '?') + queryString
      }
    }
    
    // -------------- 核心修复：从 userInfo 里拿 token --------------
    const userInfo = localStorage.getItem('userInfo')
    let token = ''
    if (userInfo) {
      const user = JSON.parse(userInfo)
      console.log('user:', user)
      token = user.token || '' // 从对象里取 token
    }

    // 统一请求头
    const headers = {
      ...options.headers,
    }
    
    // 如果是 FormData，不设置 Content-Type（让浏览器自动设置）
    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json'
    }

    // 自动带上 token
    if (token) {
      headers['token'] = token
    }

    const fetchOptions = {
      ...options,
      headers
    }

    const response = await fetch(apiUrl, fetchOptions)

    if (!response.ok) {
      // 401 未授权，跳转到登录页
      if (response.status === 401) {
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        if (window.ElMessage) {
          window.ElMessage.error('请先登录')
        }
        window.location.href = '/#/'
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    if (options.rawResponse) {
      return response
    }

    const result = await response.json()

    // 处理 Result 中 code 是 401 的情况
    if (result && typeof result.code === 'number') {
      if (result.code === 401) {
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        if (window.ElMessage) {
          window.ElMessage.error('请先登录')
        }
        window.location.href = '/#/'
        throw new Error(result.message || '请先登录')
      }
      // 处理 code 是 500 或其他错误码的情况
      if (result.code !== 200) {
        if (window.ElMessage) {
          window.ElMessage.error(result.message || '请求失败')
        }
        throw new Error(result.message || '请求失败')
      }
    }

    return result
  } catch (error) {
    console.error('请求错误:', error)
    throw error
  }
}

export default request