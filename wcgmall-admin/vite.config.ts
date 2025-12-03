import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { ConfigEnv, UserConfig, loadEnv } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import { svgBuilder } from './src/plugins/svgBuilder'

export default defineConfig(({ command, mode }: ConfigEnv): UserConfig => {
  // 获取环境变量
  const env = loadEnv(mode, process.cwd())

  // 判断是否为生产环境构建
  const isProduction = command === 'build' || mode === 'production'
  
  return {
    css: {
      preprocessorOptions: {
        scss: {
          charset: false
        },
      },
    },
    plugins: [vue(),svgBuilder('./src/icons/svg/'), AutoImport({
      imports: [
        'vue',
        'vue-router',
        'pinia'
      ],
      dts: 'src/auto-imports.d.ts',
      // 可以选择是否自动导入 Vue 的组合式 API
      vueTemplate: true,
      // 自动导入目录下的模块
      dirs: [
        './src/composables',
        './src/stores'
      ],
    })],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    build: {
      // 配置环境变量，让构建后的应用也能读取正确的API地址
      rollupOptions: {
        output: {
          manualChunks: undefined
        }
      }
    },
    // 定义全局变量，让构建后的应用也能使用
    define: {
      'process.env.NODE_ENV': JSON.stringify(isProduction ? 'production' : 'development'),
      'process.env.VITE_APP_API_URL': JSON.stringify(env.VITE_APP_API_URL)
    },
    server: {
      host: '0.0.0.0',
      port: Number(env.VITE_APP_PORT) || 3000,
      open: false,
      proxy: {
        '/api': {
          secure:true,
          target: env.VITE_APP_API_URL,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log('代理请求:', {
                target: options.target,
                path: req.url
              })
            })
          }
        }
      }
    }
  }
})
