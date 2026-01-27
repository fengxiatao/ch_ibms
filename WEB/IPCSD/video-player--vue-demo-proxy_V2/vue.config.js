const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // chainWebpack(config) {
  //   config.module
  //     .rule('worker')
  //     .test(/\.worker\.js$/)
  //     .use('worker-loader')
  //     .loader('worker-loader')
  //     .end();
  //     config.module.rule('js').exclude.add(/\.worker\.js$/);
  // }
  lintOnSave: false,
  devServer: {
    proxy: {
      "/RPC2_Login": {
        target: "http://172.3.101.2:80", //代理地址
        changeOrigin: true, // 如果接口跨域，需要进行这个参数配置
        secure: false,
        // pathRewrite: {
        //   "^/RPC2_Login": "",
        // },
      },
      "/RPC2": {
        target: "http://172.3.101.2:80",
        changeOrigin: true,
        secure: false,
      },
      "/RPC_Loadfile": {
        target: "http://172.3.101.2:80",
        changeOrigin: true,
        secure: false,
      },
      "/web_caps": {
        target: "http://172.3.101.2:80",
        changeOrigin: true,
        secure: false,
      },
    },
  }
})
