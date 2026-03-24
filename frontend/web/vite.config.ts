import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 4000,
    strictPort: false,
    proxy: {
      // SSE 流式接口，需要禁用缓冲
      '/api/v1/chat/stream': {
        target: 'http://localhost:3001',
        changeOrigin: true,
        // 禁用 http-proxy 缓冲，让 SSE 数据立即推送到浏览器
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            proxyRes.headers['X-Accel-Buffering'] = 'no'
            proxyRes.headers['Cache-Control'] = 'no-cache'
          })
        }
      },
      '/api/v1': {
        target: 'http://localhost:3001',
        changeOrigin: true
      },
      '/health': {
        target: 'http://localhost:3001',
        changeOrigin: true
      },
    }
  }
})
