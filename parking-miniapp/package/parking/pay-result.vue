<template>
	<view class="main">
		<!-- 成功状态 -->
		<view class="result-container" v-if="success">
			<view class="success-icon">
				<view class="icon-circle">
					<text class="checkmark">✓</text>
				</view>
			</view>
			
			<view class="result-title">支付成功</view>
			
			<view class="amount-display">
				<text class="currency">¥</text>
				<text class="amount">{{amount}}</text>
			</view>
			
			<view class="info-card">
				<view class="info-row">
					<text class="label">订单信息</text>
					<text class="value">{{platformName}}</text>
				</view>
				<view class="info-row">
					<text class="label">付款方式</text>
					<text class="value">{{payMethodName}}</text>
				</view>
			</view>
			
			<!-- 出场提示 -->
			<view class="exit-tips">
				<view class="tips-icon">🚗</view>
				<view class="tips-content">
					<text class="tips-title">道闸已自动放行</text>
					<text class="tips-sub">请在15分钟内驶出停车场</text>
				</view>
			</view>
		</view>
		
		<!-- 失败状态 -->
		<view class="result-container fail" v-else>
			<view class="fail-icon">
				<view class="icon-circle">
					<text class="cross">✕</text>
				</view>
			</view>
			
			<view class="result-title">支付失败</view>
			<view class="fail-reason">{{failReason || '支付过程中发生错误'}}</view>
			
			<view class="retry-btn" @click="retryPay">
				<text>重新支付</text>
			</view>
		</view>
		
		<!-- 底部按钮 -->
		<view class="bottom-actions" v-if="success">
			<view class="action-btn secondary" @click="goHome">
				<text>返回首页</text>
			</view>
			<view class="action-btn primary" @click="complete">
				<text>查看订单</text>
			</view>
		</view>
	</view>
</template>

<script>
import { mapGetters } from "vuex"
import url from "../../common/http/URL.js"

export default {
	data() {
		return {
			success: false,
			amount: '0.00',
			orderId: '',
			plateNumber: '',
			platformName: '停车收费平台',
			payMethodName: '微信支付',
			failReason: ''
		}
	},
	computed: {
		...mapGetters(['getUser'])
	},
	onLoad(options) {
		this.success = options.success === 'true'
		this.amount = options.amount || '0.00'
		this.orderId = options.orderId || ''
		this.plateNumber = decodeURIComponent(options.plateNumber || '')
		this.failReason = options.reason ? decodeURIComponent(options.reason) : ''
		
		if (this.success) {
			// 通知后端道闸放行
			this.notifyGateOpen()
		}
	},
	methods: {
		// 通知道闸放行
		async notifyGateOpen() {
			try {
				const params = {
					orderId: this.orderId,
					plateNumber: this.plateNumber
				}
				
				const res = await this.$u.api.POST(url.notifyGateOpen, params)
				
				if (res.code === 0 || res.code === 200) {
					console.log('道闸放行通知成功')
				} else {
					console.warn('道闸放行通知失败:', res.msg)
				}
			} catch (error) {
				console.error('通知道闸放行失败:', error)
			}
		},
		
		// 重新支付
		retryPay() {
			uni.navigateBack({
				delta: 1
			})
		},
		
		// 完成
		complete() {
			// 跳转到历史订单页面
			uni.redirectTo({
				url: '/package/parking/history'
			})
		},
		
		// 返回首页
		goHome() {
			uni.reLaunch({
				url: '/pages/index/index'
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.main {
	min-height: 100vh;
	background: #f5f7fa;
	display: flex;
	flex-direction: column;
}

.result-container {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 48rpx;
	
	.success-icon, .fail-icon {
		margin-bottom: 40rpx;
		
		.icon-circle {
			width: 160rpx;
			height: 160rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}
	
	.success-icon {
		.icon-circle {
			background: linear-gradient(135deg, #52C41A, #73D13D);
			box-shadow: 0 16rpx 40rpx rgba(82, 196, 26, 0.35);
			
			.checkmark {
				font-size: 80rpx;
				color: #ffffff;
				font-weight: bold;
			}
		}
	}
	
	.fail-icon {
		.icon-circle {
			background: linear-gradient(135deg, #FF4D4F, #FF7875);
			box-shadow: 0 16rpx 40rpx rgba(255, 77, 79, 0.35);
			
			.cross {
				font-size: 80rpx;
				color: #ffffff;
				font-weight: bold;
			}
		}
	}
	
	.result-title {
		font-size: 44rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 24rpx;
	}
	
	.amount-display {
		display: flex;
		align-items: baseline;
		margin-bottom: 48rpx;
		
		.currency {
			font-size: 40rpx;
			color: #333;
			margin-right: 8rpx;
		}
		
		.amount {
			font-size: 72rpx;
			font-weight: bold;
			color: #333;
		}
	}
	
	.info-card {
		width: 100%;
		background: #ffffff;
		border-radius: 24rpx;
		padding: 32rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
		margin-bottom: 32rpx;
		
		.info-row {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 20rpx 0;
			
			&:not(:last-child) {
				border-bottom: 1rpx solid #f5f5f5;
			}
			
			.label {
				font-size: 28rpx;
				color: #666;
			}
			
			.value {
				font-size: 28rpx;
				color: #333;
			}
		}
	}
	
	.exit-tips {
		width: 100%;
		background: linear-gradient(135deg, rgba(24, 144, 255, 0.08), rgba(54, 163, 255, 0.08));
		border-radius: 20rpx;
		padding: 32rpx;
		display: flex;
		align-items: center;
		border: 1rpx solid rgba(24, 144, 255, 0.2);
		
		.tips-icon {
			font-size: 48rpx;
			margin-right: 24rpx;
		}
		
		.tips-content {
			display: flex;
			flex-direction: column;
			
			.tips-title {
				font-size: 30rpx;
				color: #1890FF;
				font-weight: 600;
				margin-bottom: 8rpx;
			}
			
			.tips-sub {
				font-size: 26rpx;
				color: #666;
			}
		}
	}
	
	&.fail {
		.fail-reason {
			font-size: 28rpx;
			color: #999;
			margin-bottom: 48rpx;
		}
		
		.retry-btn {
			padding: 28rpx 80rpx;
			background: linear-gradient(135deg, #1890FF, #36A3FF);
			border-radius: 44rpx;
			box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.35);
			
			text {
				font-size: 32rpx;
				color: #ffffff;
				font-weight: 500;
			}
			
			&:active {
				transform: scale(0.98);
				opacity: 0.9;
			}
		}
	}
}

.bottom-actions {
	padding: 32rpx 48rpx;
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	gap: 24rpx;
	
	.action-btn {
		flex: 1;
		height: 96rpx;
		border-radius: 48rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		
		text {
			font-size: 32rpx;
			font-weight: 500;
		}
		
		&.primary {
			background: linear-gradient(135deg, #1890FF, #36A3FF);
			box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.35);
			
			text {
				color: #ffffff;
			}
		}
		
		&.secondary {
			background: #ffffff;
			border: 2rpx solid #1890FF;
			
			text {
				color: #1890FF;
			}
		}
		
		&:active {
			transform: scale(0.98);
			opacity: 0.9;
		}
	}
}
</style>
