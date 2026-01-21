<template>
	<view>
		<u-modal v-model="show" :show-confirm-button="false" title="温馨提示">
			<view class="padding">
				登录即同意，<text class="text-blue"
					@click="toPush('https://ismart.loongtek.cn/image/xcx/agreement/iSmart智慧园区隐私政策.docx')">《iSmart隐私协议》</text>、<text
					class="text-blue"
					@click="toPush('https://ismart.loongtek.cn/image/xcx/agreement/iSmart智慧园区用户协议.docx')">《iSmart用户协议》</text>
			</view>
			<view class="flex" slots="confirm-button">
				<view class="flex-sub">
					<navigator open-type="exit" target="miniProgram">
						<button>退出</button>
					</navigator>
				</view>
				<view class="flex-sub">
					<button @click="init(1)">同意</button>
				</view>
			</view>
		</u-modal>
		<u-toast ref="uToast"></u-toast>
	</view>
</template>

<script>
	import url from "../../common/http/URL.js";
	import store from "../../store/index.js";
	import {
		visitorLogin,
		getParkMsg,
		getUserMsg
	} from "../../common/js/request.js";
	export default {
		data() {
			return {
				show: false,
				loading: false,
				redirect: null,
			}
		},
		onShow() {
			console.log(this.$Route.query, '$Route');
			let {
				redirect
			} = this.$Route.query;
			this.redirect = redirect
			let isAgreePrivacy = uni.getStorageSync('isAgreePrivacy');
			if (isAgreePrivacy) {
				this.init(0)
			} else {
				this.show = true
			}
		},
		methods: {
			toPush(url) {
				uni.downloadFile({
					url,
					success: (res) => {
						const filePath = res.tempFilePath;
						uni.openDocument({
							filePath,
							success(res) {
								console.log(res);
							},
							fail(err) {
								console.log(err);
							}
						})
					}
				})
			},
			init(state) {
				if (state) {
					uni.setStorageSync('isAgreePrivacy', true)
					this.fastLogin()
				} else {
					this.fastLogin()
				}
			},
			// 登录逻辑
			fastLogin() {
				uni.showLoading({
					title: '登录中',
					mask: true
				});
				let params = {};
				params.type = 2
				visitorLogin(params).then(res => {
					let token = res.data.data.access_token;
					let refreshToken = res.data.data.refresh_token;
					uni.setStorageSync('token', token);
					uni.setStorageSync('refreshToken', refreshToken);
					getUserMsg(token).then((res) => {
						console.log(res, "userReS")
						let {
							user,
							permissions,
							park
						} = res.data;
						console.log(user);
						this.$store.commit('SET_USERINFO', res)
						store.state.comMsgFirmId = user.firmId
						let ParkId = user.parkId;
						//储存角色 => 访客登录 | 账号登录
						store.state.nowParkId = ParkId;
						store.state.role = user.sysRoles[0];
						store.state.roleParkId = ParkId;
						uni.hideLoading();
						let arr = getCurrentPages();
						console.log(arr);
						if (ParkId == null) {
							let {
								parkId
							} = this.$Route.query;
							uni.navigateTo({
								url: '/pages/CurrentPark/CurrentPark?parkId=' + (parkId ? parkId : '')
							})
						} else {
							let onlyParkMsg = park;
							if (park.bannerImage != null && park.bannerImage != '') {
								if (park.bannerImage.indexOf(',') != -1) {
									onlyParkMsg.bannerImage = park.bannerImage.split(',')
								}
							}
							this.$store.commit('updataParkMsg', onlyParkMsg)
							uni.setStorageSync('parkInfo', onlyParkMsg);

							if (arr.length == 1) {
								uni.reLaunch({
									url: this.redirect || "/pages/index/index"
								})
							} else {
								uni.navigateBack({
									delta: 1
								})
							}
						}
					}).catch(err => {
						console.log(err, "this.err1")
						uni.hideLoading();
						this.$refs.uToast.show({
							title: '获取信息失败,请检查网络',
							type: 'error',
							duration: '3000',
							callback: () => {
								uni.reLaunch({
									url: "/pages/empty/empty"
								})
							}
						})
					})
				}).catch(err => {
					console.log(err, "this.err")
					uni.hideLoading();
					this.$refs.uToast.show({
						title: '获取信息失败,请检查网络',
						type: 'error',
						duration: '3000',
						callback: () => {
							uni.reLaunch({
								url: "/pages/empty/empty"
							})
						}
					})
				})
			},
		},
	}
</script>

<style scoped lang="scss">
</style>
