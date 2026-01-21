import ajax from '@/uni_modules/u-ajax/js_sdk'
import store from "../../store/index.js";
import Vue from "vue"
// 创建请求实例
const instance = ajax.create({
	// 默认配置
	baseURL: store.state.baseUrl,
	// 设置为json，返回后会对数据进行一次JSON.parse()
	dataType: 'json',
	// 配置请求头信息
	header: {
		'clientId': 'app',
		'clientSecret': 'app'
	},
	timeout:15000
})

// 添加请求拦截器
instance.interceptors.request.use(
	config => {
		// 在发送请求前做些什么
		
		return config
	},
	error => {
		return Promise.reject(error)
	}
)

// 添加响应拦截器
instance.interceptors.response.use(
	response => {

		return response
	},
	error => {
		
		
		return Promise.reject(error)
	}
)

export const install = Vue => {
	// 如果您是像我下面这样挂载在 Vue 原型链上，则通过 this.$ajax 调用
	Vue.prototype.$ajax = instance
}

export default instance
