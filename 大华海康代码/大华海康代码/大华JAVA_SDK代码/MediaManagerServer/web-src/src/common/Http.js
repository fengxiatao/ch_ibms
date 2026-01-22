import axios from 'axios'
import Vue from 'vue'

let BaseUrl
const host = window.location.hostname
switch (host) {
    case '[正式环境域名]':
        BaseUrl = '[正式环境接口]'
        break
    case '192.168.50.18':
        BaseUrl = 'http://192.168.50.18:8088'
        break
    case 'localhost':
        BaseUrl = 'http://localhost:8090'
        break
}
// 配置请求的根路径
axios.defaults.baseURL =BaseUrl
// 为每次请求添加请求拦截器,为请求头添加authorization属性
axios.interceptors.request.use((config) => {
    // console.log(config);
    //config.headers.Authorization = window.sessionStorage.getItem('token');
    config.timeout=6000
    return config;
})

Vue.prototype.$http = axios