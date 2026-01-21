import url from "../http/URL.js";
import store from "../../store/index.js";
// 线上生产环境后端接口（注意：不要以斜杠结尾）
let baseUrl = 'https://sanligz.com.cn/server'
// let baseUrl = 'http://192.168.1.3:8881'
// 获取登录信息code
function getLogin() {
	return new Promise((resolve, reject) => {
		uni.login({
			success: resolve,
			fail: reject
		})
	})
}
// 访客获取token
function getToken(params, loginName) {
	return new Promise((resolve, reject) => {
		uni.request({
			// social为一键登录，user为账号登录
			url: baseUrl + `api-auth/oauth/${loginName}/token`,
			data: JSON.stringify(params),
			method: 'POST',
			header: {
				'clientId': 'app',
				'clientSecret': 'app'
			},
			success: resolve,
			fail: reject
		})
	})
}

function visitorLogin(params) {
	return new Promise((resolve, reject) => {
		wx.login({
			success: res => {
				console.log('res1', res)
				let header = {
					'content-type': 'application/x-www-form-urlencoded',
				};
				params.principal = res.code;
				if (res.code) {
					// 登录获取token
					uni.request({
						url: baseUrl + 'api-auth/oauth/social/token',
						data: JSON.stringify(params),
						method: 'POST',
						header: {
							'clientId': 'app',
							'clientSecret': 'app'
						},
						success: resolve,
						fail: reject
					})
				}
			}
		})
	})

}


function hasAccountLogin(params, that, flower) {

	wx.login({
		success(res) {
			console.log('res2', res)
			// params.password = params.password + ','+ res.code;
			console.log(params, 'params');
			if (res.code) {
				//发起网络请求
				uni.request({
					url: baseUrl + 'api-auth/oauth/user/token',
					data: JSON.stringify(params),
					method: 'POST',
					header: {
						'clientId': 'app',
						'clientSecret': 'app'
					},
					success: res => {
						console.log(res)
						switch (res.data.code) {
							case "000000":
								let token = res.data.data.access_token;
								let refreshToken = res.data.data.refresh_token;
								uni.setStorageSync('token', token);
								uni.setStorageSync('refreshToken', refreshToken);
								getUserMsg(token).then((res) => {
									console.log("res=>", res)
									uni.setStorageSync('userInfo', res);
									let ParkId = res.data.user.parkId;
									//储存角色 => 访客登录 | 账号登录
									store.state.comMsgFirmId = res.data.user.firmId
									store.state.role = res.data.user.sysRoles[0];
									store.state.roleParkId = ParkId;
									store.state.nowParkId = ParkId;
									store.state.permissions = res.data.permissions
									if (ParkId == null) {
										if (res.data.user.sysRoles[0].code ==
											"INTERMEDIARY_AGENT") {
											that.$u.route({
												type: 'reLaunch',
												url: '/package/IntermediarySystem/ListOfParks/ListOfParks',
											})
										} else {
											that.$u.route("/pages/CurrentPark/CurrentPark")
										}
									} else {
										getParkMsg(that, ParkId)
									}

								}).catch((err) => {
									console.log(err, "错误");
								})

								that.flower = false;
								break;
							default:
								console.log(res)
								that.flower = false;
								uni.showToast({
									title: res.data.msg,
									icon: 'loading',
									duration: 2000 //持续的时间
								})
								break;
						}

					}
				})
			} else {
				console.log('登录失败！' + res.errMsg)
			}
		}
	})
}


function getUserMsg(token, parkId = null) {
	return new Promise((resolve, reject) => {
		console.log(token);
		uni.request({
			url: baseUrl + "api-auth/oauth/userinfo?type=XCX&parkId=" + parkId,
			method: 'GET',
			header: {
				'clientId': 'app',
				'clientSecret': 'app',
				'Authorization': 'Bearer ' + token
			},
			success: resolve,
			fail: reject
		})
	})

}

function getParkMsg1(that, ParkId) {
	return new Promise((resolve, reject) => {
		that.$u.api.GET(url.getParkInfoByParkId + ParkId).then(res => {
			resolve(res)
		})
	})
}

function getParkMsg(that, ParkId, route = "/pages/index") {
	that.$u.api.GET(url.getParkInfoByParkId + ParkId).then(res => {
		console.log(res, '12132')
		if (!res.data) {
			uni.showToast({
				title: '园区不存在',
				icon: 'loading',
				duration: 2000 //持续的时间
			})
			return;
		}
		let onlyParkMsg = res.data;
		if (res.data.bannerImage != null && res.data.bannerImage != '') {
			if (res.data.bannerImage.indexOf(',') != -1) {
				onlyParkMsg.bannerImage = res.data.bannerImage.split(',')
			}
		}
		that.$store.commit('updataParkMsg', onlyParkMsg)
		uni.setStorageSync('parkInfo', onlyParkMsg)
		if (res.code === 0) {

			let allPages = getCurrentPages();
			let currentPage = allPages[allPages.length - 1];
			if (currentPage.$page.fullPath == route) {
				// currentPage.onLoad()
				// currentPage.onShow()
				// currentPage.onReady()
				// console.log(123)
				uni.redirectTo({
					url: route
				})

			} else {
				that.$u.route({
					type: 'tab',
					url: route,
				})

			}
		} else {
			uni.showToast({
				title: '信息有误',
				icon: 'loading',
				duration: 2000, //持续的时间，
				callback: () => {
					uni.reLaunch({
						url: "/pages/empty/empty"
					})
				}
			})

		}


	})

}

// 完整登录
async function universalLogin(parkId, fn) {
	try {
		uni.showLoading({
			title: "加载中"
		})
		const {
			code
		} = await getLogin();
		const openId = store.getters.getUser.openId
		const params = {
			principal: code,
			openId,
			type: 3,
			parkId
		}
		const result = await getToken(params, 'social');
		const {
			access_token: token,
			refresh_token: refreshToken
		} = result.data.data
		store.commit('SET_TOKEN', {
			token,
			refreshToken
		})
		const userData = await getUserMsg(token, parkId)
		store.commit('SET_USERINFO', userData)
		let {
			user
		} = userData.data
		let ParkId = user.parkId;
		store.state.comMsgFirmId = user.firmId
		store.state.role = user.sysRoles[0];
		store.state.roleParkId = ParkId;
		store.state.nowParkId = ParkId;
		uni.hideLoading()
		return fn(userData.data)
	} catch (e) {
		console.log(e);
		//TODO handle the exception
		uni.hideLoading()
	}
}




export {
	visitorLogin,
	hasAccountLogin,
	getUserMsg,
	getParkMsg,
	baseUrl,
	getLogin,
	getToken,
	getParkMsg1,
	universalLogin
}
