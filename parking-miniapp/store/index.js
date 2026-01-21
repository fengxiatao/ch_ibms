import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)
let userInfo = uni.getStorageSync('userInfo') || {}
let onlyParkMsg = {};
let nowParkId = null;
let roleParkId = null;
const files = require.context("./modules", false, /\.js$/);
// 线上生产环境后端接口（注意：不要以斜杠结尾）
// let baseUrl = 'https://sanligz.com.cn/server'
// let baseUrl = 'http://192.168.1.3:8881'
// let baseUrl = 'http://192.168.1.126:48888'
// 长辉线上环境
let baseUrl = 'https://ibms.gzchanghui.cn'
let modules = {
	state: {
		baseUrl,
		comMsgFirmId: null,
		onlyParkMsg,
		nowParkId,
		role: null,
		roleParkId,
		isShowInfoChange: true,
		scanGetData: null, //false ==>我是访客 true==>自己园区
		// action:"https://ismart.loongtek.cn/api-user/upload/uploadFile",
		action: baseUrl + "/api-user/upload/uploadFile",
		permissions: [],
		apiHeader: {
			apiUser: "api-user",
			apiParking: "api-parking",
			apiDoor: "api-control",
			apiIssue: "api-issue",
			apiAuth: "api-auth",
			apiRoom: 'api-room',
			apiPatrol: "api-patrol",
			apiDiscovery: "api-discovery"
		},
		userInfo,
		checkCoupluon: [],
		isWechatBound: false,
		accountBalance: 0
	},
	// 获取仓库数据的方法
	getters: {
		getCheckCoupluon(state) {
			return state.checkCoupluon
		},
		// getFirmInfo(state) {
		// 	return state.userInfo.data.firmInfo
		// },
		getUser(state) {
			return state.userInfo
		},
		getParkMsg(state) {
			return state.onlyParkMsg
		},
		getNowParkId(state) {
			return state.nowParkId
		},
		permissions(state) {
			return state.permissions
		},
		getApiHeader(state) {
			return state.apiHeader
		},
		getIsWechatBound(state) {
			return state.isWechatBound
		},
		getAccountBalance(state) {
			return state.accountBalance
		}
	},
	// 修改数据的方法（同步）
	mutations: {
		SET_TOKEN(state, obj) {
			uni.setStorageSync('token', obj.token);
			uni.setStorageSync('refreshToken', obj.refreshToken);
		},
		// SET_USER(state, val) {
		// 	let userInfo = uni.$u.deepClone(state.userInfo);
		// 	userInfo.data.user = val;
		// 	uni.setStorageSync('userInfo', userInfo)
		// 	state.userInfo = uni.getStorageSync('userInfo') || {}
		// },
		SET_USERINFO(state, val) {
			uni.setStorageSync('userInfo', val)
			uni.setStorageSync('userInfo', val)
			state.userInfo = uni.getStorageSync('userInfo') || {}
		},
		updataParkMsg(state, val) {
			state.onlyParkMsg = val;
		},
		updataNowParkId(state, val) {
			state.nowParkId = val;
		},
		scanCodeQuery(state, val) {
			state.scanGetData = val;
		},
		updataComMsgFirmId(state, val) {
			state.comMsgFirmId = val;
		},
		setPermissions(state, val) {
			state.permissions = val
		},
		SET_CHECKCOUPLUON(state, val) {
			state.checkCoupluon = val
		},
		SET_WECHAT_BOUND(state, val) {
			state.isWechatBound = val
		},
		SET_ACCOUNT_BALANCE(state, val) {
			state.accountBalance = val
		},
	},
	// 修改数据的方法（异步）
	actions: {
		setcheckList({commit}, arr) {
			commit('SET_CHECKCOUPLUON', arr)
		},
		updateWechatBound({commit}, bound) {
			commit('SET_WECHAT_BOUND', bound)
		},
		updateAccountBalance({commit}, balance) {
			commit('SET_ACCOUNT_BALANCE', balance)
		}
	},
	// 将Vuex分模块化
	modules: {}
}

files.keys().forEach((key) => {
	Object.assign(modules.state, files(key)["state"]);
	Object.assign(modules.mutations, files(key)["mutations"]);
	Object.assign(modules.actions, files(key)["actions"]);
});

const store = new Vuex.Store(modules)

export default store
