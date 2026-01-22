const { defineConfig } = require('@vue/cli-service')
const CopyWebpackPlugin = require('copy-webpack-plugin')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave:false,
  configureWebpack: {
    plugins:[
      new CopyWebpackPlugin([
        {
          from: 'node_modules/@easydarwin/easyplayer/dist/component/EasyPlayer.wasm',
          to: './libs/EasyPlayer/'
        },
        {
          from: 'node_modules/@easydarwin/easyplayer/dist/component/crossdomain.xml',
          to: './libs/EasyPlayer/'
        },
        {
          from: 'node_modules/@easydarwin/easyplayer/dist/component/EasyPlayer-lib.min.js',
          to: './libs/EasyPlayer/'
        }
      ])
    ]
  }
})

