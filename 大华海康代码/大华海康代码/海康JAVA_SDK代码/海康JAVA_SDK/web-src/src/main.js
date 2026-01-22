import Vue from 'vue'
import App from './App.vue'
import router from './router'
import '@/assets/css/Base.less'
import axios from 'axios'
import VueAxios from 'vue-axios'
import '@/common/Http.js'

Vue.config.productionTip = false

new Vue({
  router,
  VueAxios,
  axios,
  render: h => h(App),
}).$mount('#app')
