import Vue from 'vue'
import App from './App'
import uView from "uview-ui";

// 路由器存在兼容性问题，暂时禁用
// import {
// 	router,
// 	RouterMount
// } from "@/router";
// Vue.use(router);

Vue.use(uView);

Vue.config.productionTip = false

Vue.prototype.$bus = new Vue() // event Bus 用于无关系组件间的通信。

// store
import store from './store'
Vue.prototype.$store = store

import updata from "common/js/upData.js";
Vue.prototype.$updata = updata;

// 指令
import directive from './directive'
Vue.use(directive)

App.mpType = 'app'

const app = new Vue({
	...App
})

import httpInterceptor from './common/http/interceptor.js'
Vue.use(httpInterceptor, app)

import httpApi from './common/http/API.js'
Vue.use(httpApi, app)

// RouterMount(app, router, "#app");
app.$mount();
