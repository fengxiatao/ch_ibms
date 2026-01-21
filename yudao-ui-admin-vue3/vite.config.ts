import {resolve} from 'path'
import type {ConfigEnv, UserConfig} from 'vite'
import {loadEnv} from 'vite'
import {createVitePlugins} from './build/vite'
import {exclude, include} from "./build/vite/optimize"
// 当前执行node命令时文件夹的地址(工作目录)
const root = process.cwd()

// 路径查找
function pathResolve(dir: string) {
    return resolve(root, '.', dir)
}

// https://vitejs.dev/config/
export default ({command, mode}: ConfigEnv): UserConfig => {
    let env = {} as any
    const isBuild = command === 'build'
    if (!isBuild) {
        env = loadEnv((process.argv[3] === '--mode' ? process.argv[4] : process.argv[3]), root)
    } else {
        env = loadEnv(mode, root)
    }
    return {
        base: env.VITE_BASE_PATH,
        root: root,
        // 服务端渲染
        server: {
            port: env.VITE_PORT, // 端口号
            host: "0.0.0.0",
            open: env.VITE_OPEN === 'true',
            // 本地跨域代理
            proxy: {
              '/admin-api': {
                target: env.VITE_BASE_URL || 'http://127.0.0.1:48888',
                ws: true,  // ✅ 启用 WebSocket 代理
                changeOrigin: true,
                // 不重写路径，保持 /admin-api 前缀
              },
              // WebSocket 代理 - 设备状态、门禁事件等
              '/ws': {
                target: env.VITE_BASE_URL || 'http://127.0.0.1:48888',
                ws: true,  // ✅ 启用 WebSocket 代理
                changeOrigin: true,
              },
              // WebSocket 代理 - 报警事件
              '/websocket': {
                target: env.VITE_BASE_URL || 'http://127.0.0.1:48888',
                ws: true,  // ✅ 启用 WebSocket 代理
                changeOrigin: true,
              },
              // GeoServer 代理 - 解决开发模式下的 CORS 问题
              '/geoserver': {
                target: 'http://192.168.1.126:8088',
                ws: false,
                changeOrigin: true,
                // 保持 /geoserver 路径
              },
              // 大华设备RPC代理 - 将RPC请求转发到大华设备
              '/RPC2': {
                target: 'http://192.168.1.200:80',
                ws: false,
                changeOrigin: true,
                // 保持 /RPC2 路径
              },
              '/RPC2_Login': {
                target: 'http://192.168.1.200:80',
                ws: false,
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/RPC2_Login/, '/RPC2_Login')
              },
            },
        },
        // 项目使用的vite插件。 单独提取到build/vite/plugin中管理
        plugins: createVitePlugins(),
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: '@use "@/styles/variables.scss" as *;',
                    javascriptEnabled: true,
                    silenceDeprecations: ["legacy-js-api"], // 参考自 https://stackoverflow.com/questions/78997907/the-legacy-js-api-is-deprecated-and-will-be-removed-in-dart-sass-2-0-0
                }
            }
        },
        resolve: {
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.scss', '.css'],
            alias: [
                {
                    find: 'vue-i18n',
                    replacement: 'vue-i18n/dist/vue-i18n.cjs.js'
                },
                {
                    find: /\@\//,
                    replacement: `${pathResolve('src')}/`
                }
            ]
        },
        build: {
            minify: 'esbuild',
            outDir: env.VITE_OUT_DIR || 'dist',
            sourcemap: env.VITE_SOURCEMAP === 'true' ? 'inline' : false,
            // brotliSize: false,
            rollupOptions: {
                output: {
                    manualChunks: {
                      echarts: ['echarts'], 
                      'form-create': ['@form-create/element-ui'], 
                      'form-designer': ['@form-create/designer'],
                    }
                },
            },
        },
        optimizeDeps: {include, exclude}
    }
}
