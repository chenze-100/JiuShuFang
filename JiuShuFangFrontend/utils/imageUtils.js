import { API_BASE_URL } from '../config/api.js'

/**
 * 将本地 MinIO 的完整 URL 转换为代理 URL（支持外网访问）
 * @param {string} url - 原始图片 URL
 * @returns {string} - 转换后的代理 URL
 */
export const convertImageUrl = (url) => {
  if (!url) return ''
  
  // 如果是 http://localhost:9100/book/ 或 http://127.0.0.1:9100/book/ 开头的 URL
  // 转换为 /image/book/upload/{year}/{month}/{day}/{filename} 格式
  if (url.startsWith('http://localhost:9100/book/') || url.startsWith('http://127.0.0.1:9100/book/')) {
    // 提取日期和文件名部分
    const match = url.match(/book\/(\d{4})\/(\d{2})\/(\d{2})\/([^?#]+)/)
    if (match) {
      const [, year, month, day, filename] = match
      return `/image/book/upload/${year}/${month}/${day}/${filename}`
    }
  }
  
  // 如果是 http://localhost:9100/picture/ 或 http://127.0.0.1:9100/picture/ 开头的 URL
  // 转换为 /image/picture/upload/{year}/{month}/{day}/{filename} 格式
  if (url.startsWith('http://localhost:9100/picture/') || url.startsWith('http://127.0.0.1:9100/picture/')) {
    // 提取日期和文件名部分
    const match = url.match(/picture\/(\d{4})\/(\d{2})\/(\d{2})\/([^?#]+)/)
    if (match) {
      const [, year, month, day, filename] = match
      return `/image/picture/upload/${year}/${month}/${day}/${filename}`
    }
  }
  
  // 如果已经是 /image/book/upload/ 或 /image/picture/upload/ 开头的代理路径，直接返回
  if (url.startsWith('/image/book/upload/') || url.startsWith('/image/picture/upload/')) {
    return url
  }
  
  // 其他情况直接返回（包括已经是 /book/ 或 /picture/ 开头的相对路径）
  return url
}

/**
 * 将本地 MinIO 的完整 URL 转换为外网可访问的 URL
 * @param {string} url - 原始图片 URL
 * @returns {string} - 转换后的外网 URL
 */
export const convertToExternalUrl = (url) => {
  if (!url) return ''

  // 如果是 http://localhost:9100/book/ 或 http://127.0.0.1:9100/book/ 开头的 URL
  // 转换为外网可访问的路径
  if (url.startsWith('http://localhost:9100/book/') || url.startsWith('http://127.0.0.1:9100/book/')) {
    // 提取日期和文件名部分
    const match = url.match(/book\/(\d{4})\/(\d{2})\/(\d{2})\/([^?#]+)/)
    if (match) {
      const [, year, month, day, filename] = match
      // 使用相对路径，通过 Vite 代理转发到后端
      return `/api/image/book/upload/${year}/${month}/${day}/${filename}`
    }
  }

  // 如果是 http://localhost:9100/picture/ 或 http://127.0.0.1:9100/picture/ 开头的 URL
  // 转换为外网可访问的路径
  if (url.startsWith('http://localhost:9100/picture/') || url.startsWith('http://127.0.0.1:9100/picture/')) {
    // 提取日期和文件名部分
    const match = url.match(/picture\/(\d{4})\/(\d{2})\/(\d{2})\/([^?#]+)/)
    if (match) {
      const [, year, month, day, filename] = match
      // 使用相对路径，通过 Vite 代理转发到后端
      return `/api/image/picture/upload/${year}/${month}/${day}/${filename}`
    }
  }


  if (url.startsWith('http://localhost:9000') || url.startsWith('http://127.0.0.1:9000')) {
    // 匹配 bucket 后面的完整路径（zhiyue/2026/09/21/xxx.jpg）
    const match = url.match(/localhost:9000\/zhiyue\/(.+)/)
        || url.match(/127\.0\.0\.1:9000\/zhiyue\/(.+)/)

    if (match) {
      const filePath = match[1] // 2026/09/21/9d4c0bcf8f9c4adaacab26521345577f.jpg
      return `/api/image/download/${filePath}`
    }
  }
  // 如果已经是 /image/book/upload/ 或 /image/picture/upload/ 开头的代理路径，直接返回
  if (url.startsWith('/image/book/upload/') || url.startsWith('/image/picture/upload/')) {
    return '/api'+url
  }

  // 其他情况直接返回
  return url
}
