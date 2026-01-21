<template>
	<view class="main">
		<!-- 订单详情卡片 -->
		<view class="order-card">
			<view class="card-header">
				<text class="header-title">订单详情</text>
			</view>
			
			<view class="order-info">
				<view class="info-row">
					<text class="label">订单号:</text>
					<text class="value order-id">{{payData.orderId || '--'}}</text>
				</view>
				<view class="info-row amount-row">
					<text class="label">金额:</text>
					<text class="value amount">{{payData.amount || '0.00'}}元</text>
				</view>
				<view class="info-row">
					<text class="label">订单描述:</text>
					<text class="value">停车费用</text>
				</view>
			</view>
		</view>
		
		<!-- 支付方式 -->
		<view class="payment-card">
			<view class="card-header">
				<text class="header-title">支付方式</text>
			</view>
			
			<view class="payment-methods">
				<!-- 微信支付 -->
				<view class="payment-item active">
					<view class="method-left">
						<view class="method-icon wechat-icon">
							<text class="icon-text">微</text>
						</view>
						<view class="method-info">
							<text class="method-name">微信支付</text>
						</view>
					</view>
					<view class="method-right">
						<view class="radio-circle checked">
							<view class="radio-inner"></view>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 确认支付按钮 -->
		<view class="confirm-btn" :class="{disabled: paying}" @click="confirmPay">
			<text v-if="!paying">确认支付</text>
			<text v-else>支付中...</text>
		</view>
		
		<u-toast ref="uToast" />
	</view>
</template>

<script>
import { mapGetters } from "vuex"
import url from "../../common/http/URL.js"

export default {
	data() {
		return {
			payData: {},
			paying: false
		}
	},
	computed: {
		...mapGetters(['getUser'])
	},
	onLoad(options) {
		if (options.data) {
			try {
				this.payData = JSON.parse(decodeURIComponent(options.data))
			} catch (e) {
				console.error('解析支付数据失败:', e)
			}
		}
	},
	methods: {
		// 确认支付
		async confirmPay() {
			if (this.paying) return
			
			this.paying = true
			
			try {
				await this.wechatPay()
			} catch (error) {
				console.error('支付失败:', error)
				this.$refs.uToast.show({
					title: error.message || '支付失败，请重试',
					type: 'error'
				})
			} finally {
				this.paying = false
			}
		},
		
		// 微信支付
		async wechatPay() {
			// 获取微信登录code
			const loginRes = await new Promise((resolve, reject) => {
				wx.login({
					success: resolve,
					fail: reject
				})
			})
			
			if (!loginRes.code) {
				throw new Error('获取微信授权失败')
			}
			
			// 调用后端获取支付参数
			const params = {
				code: loginRes.code,
				orderId: this.payData.orderId,
				amount: this.payData.amount,
				plateNumber: this.payData.plateNumber,
				parkingLotId: this.payData.parkingLotId
			}
			
			const res = await this.$u.api.POST(url.parkingWechatPay, params)
			
			if (res.code !== 0 && res.code !== 200) {
				throw new Error(res.msg || '获取支付参数失败')
			}
			
			// 调用微信支付
			await new Promise((resolve, reject) => {
				wx.requestPayment({
					timeStamp: res.data.timeStamp,
					nonceStr: res.data.nonceStr,
					package: res.data.packageValue,
					signType: res.data.signType,
					paySign: res.data.paySign,
					success: resolve,
					fail: (err) => {
						if (err.errMsg.includes('cancel')) {
							reject(new Error('支付已取消'))
						} else {
							reject(new Error('支付失败'))
						}
					}
				})
			})
			
			// 支付成功，跳转结果页
			this.goPayResult(true)
		},
		
		// 跳转支付结果页
		goPayResult(success) {
			uni.redirectTo({
				url: `/package/parking/pay-result?success=${success}&amount=${this.payData.amount}&orderId=${this.payData.orderId}&plateNumber=${encodeURIComponent(this.payData.plateNumber)}`
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.main {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 32rpx;
	padding-bottom: 200rpx;
}

.order-card, .payment-card {
	background: #ffffff;
	border-radius: 24rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	margin-bottom: 32rpx;
	
	.card-header {
		padding: 28rpx 32rpx;
		border-bottom: 1rpx solid #f5f5f5;
		
		.header-title {
			font-size: 32rpx;
			font-weight: 600;
			color: #333;
		}
	}
}

.order-info {
	padding: 24rpx 32rpx;
	
	.info-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 16rpx 0;
		
		.label {
			font-size: 28rpx;
			color: #666;
		}
		
		.value {
			font-size: 28rpx;
			color: #333;
			
			&.order-id {
				font-size: 24rpx;
				color: #999;
			}
			
			&.amount {
				font-size: 36rpx;
				font-weight: bold;
				color: #FF6B00;
			}
		}
		
		&.amount-row {
			padding: 24rpx 0;
		}
	}
}

.payment-methods {
	padding: 16rpx 0;
	
	.payment-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 28rpx 32rpx;
		border-bottom: 1rpx solid #f8f8f8;
		transition: background-color 0.2s;
		
		&:last-child {
			border-bottom: none;
		}
		
		&:active {
			background: #f9f9f9;
		}
		
		&.active {
			background: rgba(24, 144, 255, 0.03);
		}
		
		.method-left {
			display: flex;
			align-items: center;
			
			.method-icon {
				width: 56rpx;
				height: 56rpx;
				margin-right: 24rpx;
				border-radius: 12rpx;
				display: flex;
				align-items: center;
				justify-content: center;
				
				.icon-text {
					font-size: 28rpx;
					font-weight: bold;
					color: #ffffff;
				}
				
				&.wechat-icon {
					background: linear-gradient(135deg, #07C160, #2DC83C);
				}
			}
			
			.method-info {
				display: flex;
				flex-direction: column;
				
				.method-name {
					font-size: 30rpx;
					color: #333;
					font-weight: 500;
				}
				
				.method-desc {
					font-size: 24rpx;
					color: #999;
					margin-top: 4rpx;
				}
			}
		}
		
		.method-right {
			.radio-circle {
				width: 40rpx;
				height: 40rpx;
				border-radius: 50%;
				border: 3rpx solid #ddd;
				display: flex;
				align-items: center;
				justify-content: center;
				transition: all 0.2s;
				
				&.checked {
					border-color: #1890FF;
					
					.radio-inner {
						width: 24rpx;
						height: 24rpx;
						border-radius: 50%;
						background: #1890FF;
					}
				}
			}
		}
	}
}

.confirm-btn {
	position: fixed;
	bottom: 48rpx;
	left: 32rpx;
	right: 32rpx;
	height: 96rpx;
	background: linear-gradient(135deg, #1890FF, #36A3FF);
	border-radius: 48rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.35);
	
	text {
		font-size: 36rpx;
		font-weight: 600;
		color: #ffffff;
	}
	
	&:active {
		transform: scale(0.98);
		opacity: 0.9;
	}
	
	&.disabled {
		opacity: 0.6;
		pointer-events: none;
	}
}
</style>
