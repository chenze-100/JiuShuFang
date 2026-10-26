import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    host: '0.0.0.0',
    allowedHosts: true,
    proxy: {
      // 统一代理配置：所有 /api 开头的请求都转发到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 图片路径代理（不需要 /api 前缀）
      '/image': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // 验证码相关接口代理（本地后端）
      '/capture': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // 用户相关接口代理（扫码登录等）
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // 评论相关接口代理
      '/comment': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // WebSocket 聊天接口代理
      '/chat': {
        target: 'ws://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true
      },
      // 拦截 /null 请求
      '/null': {
        target: 'http://localhost:8080', // 指向你的后端
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 直接返回 200，不转发给后端
            res.statusCode = 200;
            res.setHeader('Content-Type', 'image/gif');
            // 返回一个 1x1 的透明 GIF（或者空 body）
            res.end('GIF89a\x01\x00\x01\x00\x80\x00\x00\xff\xff\xff\x00\x00\x00!\xf9\x04\x01\x00\x00\x00\x00,\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02D\x01\x00;', 'binary');
          });
        }
      }
    }
  }
})
