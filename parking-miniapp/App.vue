<script>
	import store from "./store/index.js";
	import {
		visitorLogin,
		hasAccountLogin,
		getUserMsg
	} from "./common/js/request.js";
	export default {
		globalData: {
			token: null,
			onlyParkMsg: null,
			userInfo: null,
			curRoute: null,
		},
		data() {
			return {

			}
		},
		//全局只触发一次
		onLaunch(option) {
			this.$options.globalData.curRoute = option.path
			
			// 版本更新检测 - Requirements 9.5
			this.checkForUpdate()
			
			// 检查登录状态
			this.checkLoginStatus()
		},
		onShow(option) {
			// 小程序从后台切换到前台时触发
			console.log('小程序切换到前台显示')
		},
		onHide: function() {
			console.log('小程序切换到后台显示')
		},
		methods: {
			/**
			 * 版本更新检测
			 * Requirements 9.5: 检查更新并提示用户重启
			 */
			checkForUpdate() {
				// 检查是否支持 getUpdateManager API
				if (typeof wx !== 'undefined' && wx.getUpdateManager) {
					const updateManager = wx.getUpdateManager()
					
					updateManager.onCheckForUpdate((res) => {
						// 请求完新版本信息的回调
						console.log('检查更新结果:', res.hasUpdate)
						if (res.hasUpdate) {
							console.log('发现新版本，开始下载...')
						}
					})
					
					updateManager.onUpdateReady(() => {
						// 新版本下载完成
						wx.showModal({
							title: '更新提示',
							content: '新版本已经准备好，是否重启应用？',
							confirmText: '立即重启',
							cancelText: '稍后再说',
							success: (res) => {
								if (res.confirm) {
									// 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
									updateManager.applyUpdate()
								}
							}
						})
					})
					
					updateManager.onUpdateFailed(() => {
						// 新版本下载失败
						wx.showModal({
							title: '更新提示',
							content: '新版本下载失败，请检查网络后重试',
							showCancel: false,
							confirmText: '知道了'
						})
					})
				}
			},
			
			/**
			 * 检查登录状态
			 */
			checkLoginStatus() {
				const token = uni.getStorageSync('token')
				const userInfo = uni.getStorageSync('userInfo')
				
				if (token && userInfo) {
					// 已登录，更新全局状态
					this.$options.globalData.token = token
					this.$options.globalData.userInfo = userInfo
				}
			},
			
			/**
			 * 检查微信会话状态
			 */
			myCheckSession() {
				let parkInfo = uni.getStorageSync('parkInfo');

				if (parkInfo != '') {
					wx.checkSession({
						success: (res) => {
							//session_key 未过期，并且在本生命周期一直有效
							let uinfo = uni.getStorageSync('userInfo');

							//访客没有ParkId ==>null 
							store.state.roleParkId = uinfo.data.user.parkId;
							store.state.role = uinfo.data.user.sysRoles[0];
							//当前园区信息
							store.state.onlyParkMsg = uni.getStorageSync('parkInfo');
							//当前园区ID
							store.state.nowParkId = store.state.onlyParkMsg.id;

							uni.getUserInfo({
								success: res => {
									console.log('getUserInfo', res);
									let userInfo = uni.getStorageSync('userInfo');
									let params = {};
									if (userInfo && userInfo.data.user.openId) {
										params.openId = userInfo.data.user.openId
									} else {
										params.openId = null
									}

									params.rawData = res.rawData;
									params.type = 1
									visitorLogin(params).then(res => {
										let token = res.data.data.access_token;
										let refreshToken = res.data.data.refresh_token;
										uni.setStorageSync('token', token);
										uni.setStorageSync('refreshToken', refreshToken);
										getUserMsg(token).then((res) => {
											uni.setStorageSync('userInfo', res);
											uni.switchTab({
												url: '/pages/index'
											})
										})
									}).catch(err => {
										setTimeout(() => {
											uni.reLaunch({
												url: '/pages/login/login'
											})
										}, 300)
									})
								},
								fail: (err) => {
									console.log('获取用户信息失败', err)
									setTimeout(() => {
										uni.reLaunch({
											url: '/pages/login/login'
										})
									}, 300)
								}
							})
						},
						fail: (err) => {
							console.log('session_key 已过期', err)
							setTimeout(() => {
								uni.reLaunch({
									url: '/pages/login/login'
								})
							}, 300)
						}
					})
				} else {
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/empty/empty',
						})
					}, 300)
				}
			},
		}
	}
</script>

<style lang="scss">
	/* 注意要写在第一行，同时给style标签加入lang="scss"属性 */
	@import "uview-ui/index.scss";
	@import "@/colorui/icon.css";
	@import "@/colorui/main.css";
	@import "@/static/css/global.scss";
	// ------------------
	@import "static/style/base.scss";
	// -----------------
	.index {
		min-height: 100vh;
		background-color: $uni-bg-color-grey;

	}

	.uform {
		background-color: #FFFFFF;
		padding: 40rpx;
	}

	.parkMsg {
		width: 100%;
		background-color: #FFFFFF;
		padding: 0 40rpx;

		.partName {
			margin-top: 60rpx;
			font-weight: 600;
			display: flex;
			justify-content: space-between;
		}

		.star::before,
		.notic::before {
			content: "";
			width: 1rpx;
			height: 40rpx;
			border-left: solid 10rpx $uni-btn;
			margin-right: 14rpx;
		}

		.body {
			display: flex;
			padding: 30rpx 0 30rpx 30rpx;
			border-bottom: 1rpx solid #eee;

			.u-body-item-img {
				image {
					width: 100%;
					height: 100%;
					border-radius: 10rpx;
				}
			}
		}
	}

	/deep/.iconfont {
		margin-right: 5px;
	}

	.notic,
	.star {
		font-size: 36rpx;
		font-weight: normal;
		display: flex;
		align-items: center;
	}
</style>
<style>
	@import "./static/css/iconfont.css";
	@import "@/static/icon/iconfont.css";
</style>
