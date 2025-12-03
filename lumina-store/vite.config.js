import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { loadEnv } from 'vite';
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';
import { resolve } from 'path';
import tailwindcss from '@tailwindcss/vite';
export default defineConfig(function (_a) {
    var command = _a.command, mode = _a.mode;
    // 获取环境变量
    var env = loadEnv(mode, process.cwd());
    return {
        css: {
            preprocessorOptions: {
                scss: {
                    charset: false
                },
            },
        },
        plugins: [
            vue(),
            tailwindcss(),
            createSvgIconsPlugin({
                // 指定需要缓存的图标文件夹
                iconDirs: [resolve(process.cwd(), 'src/icons/svg')],
                // 指定symbolId格式
                symbolId: 'icon-[name]',
            }),
        ],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            }
        },
        server: {
            host: '0.0.0.0',
            port: Number(env.VITE_APP_PORT) || 3001,
            open: false,
            proxy: {
                '/api': {
                    secure: true,
                    target: env.VITE_APP_API_URL,
                    changeOrigin: true,
                    rewrite: function (path) { return path.replace(/^\/api/, ''); },
                    configure: function (proxy, options) {
                        proxy.on('proxyReq', function (proxyReq, req, res) {
                            console.log('代理请求:', {
                                target: options.target,
                                path: req.url
                            });
                        });
                    }
                }
            }
        }
    };
});
