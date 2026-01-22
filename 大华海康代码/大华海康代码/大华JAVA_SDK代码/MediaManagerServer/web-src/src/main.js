import Vue from 'vue'
import App from './App.vue'
import router from './router'
import '@/assets/css/Base.less'
import '@/assets/css/publicCss.less'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css';    //全局引入element的样式
import axios from 'axios'
import VueAxios from 'vue-axios'
import '@/common/Http.js'

Vue.config.productionTip = false
Vue.use(ElementUI)

new Vue({
  router,
  VueAxios,
  axios,
  render: h => h(App),
}).$mount('#app')
