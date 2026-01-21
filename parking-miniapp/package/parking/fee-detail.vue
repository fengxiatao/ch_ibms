<template>
	<view class="main">
		<!-- 加载中 -->
		<view class="loading-container" v-if="loading">
			<view class="loading-spinner"></view>
			<text>正在查询停车费用...</text>
		</view>
		
		<!-- 费用信息 -->
		<view class="fee-container" v-else-if="feeInfo">
			<!-- 金额卡片 -->
			<view class="amount-card">
				<view class="amount-label">支付金额:</view>
				<view class="amount-value">
					<text class="amount-number">{{feeInfo.amount || '0.00'}}</text>
					<text class="amount-unit">元</text>
				</view>
			</view>
			
			<!-- 详情信息 -->
			<view class="detail-card">
				<view class="detail-row">
					<text class="label">停车场名:</text>
					<text class="value">{{feeInfo.parkingLotName || '--'}}</text>
				</view>
				<view class="detail-row">
					<text class="label">车牌号码:</text>
					<view class="plate-value">
						<text class="value highlight">{{plateNumber}}</text>
						<text class="view-photo" @click="viewPhoto" v-if="feeInfo.photoUrl">[查看图片]</text>
					</view>
				</view>
				<view class="detail-row">
					<text class="label">入场时间:</text>
					<text class="value">{{feeInfo.entryTime || '--'}}</text>
				</view>
				<view class="detail-row">
					<text class="label">查询时间:</text>
					<text class="value">{{currentTime}}</text>
				</view>
				<view class="detail-row">
					<text class="label">停车时长:</text>
					<text class="value duration">{{feeInfo.duration || '--'}}</text>
				</view>
			</view>
			
			<!-- 支付按钮 -->
			<view class="pay-btn" @click="goPay">
				<text>立即支付</text>
			</view>
			
			<!-- 提示信息 -->
			<view class="tips-section">
				<view class="tips-row">
					<text class="tips-text">支付成功后，请在</text>
					<text class="tips-highlight">15分钟</text>
					<text class="tips-text">内出场</text>
				</view>
			</view>
			
			<!-- 重新输入车牌 -->
			<view class="reenter-section" @click="reenterPlate">
				<text>点击重新输入车牌</text>
			</view>
		</view>
		
		<!-- 无记录 -->
		<view class="empty-container" v-else-if="!loading && !feeInfo">
			<view class="empty-icon">
				<text class="empty-emoji">🚗</text>
			</view>
			<text class="empty-text">未查询到该车牌的停车记录</text>
			<text class="empty-sub">请确认车牌号是否正确</text>
			<view class="reenter-btn" @click="reenterPlate">
				<text>重新输入车牌</text>
			</view>
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
			loading: true,
			plateNumber: '',
			parkingLotId: '',
			currentTime: '',
			feeInfo: null
		}
	},
	computed: {
		...mapGetters(['getUser'])
	},
	onLoad(options) {
		this.plateNumber = decodeURIComponent(options.plateNumber || '')
		this.parkingLotId = options.parkingLotId || ''
		this.currentTime = this.formatTime(new Date())
		
		if (this.plateNumber) {
			this.queryParkingFee()
		} else {
			this.loading = false
		}
	},
	methods: {
		// 格式化时间
		formatTime(date) {
			const pad = n => n.toString().padStart(2, '0')
			return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
		},
		
		// 查询停车费用
		async queryParkingFee() {
			this.loading = true
			
			try {
				const params = {
					plateNumber: this.plateNumber
				}
				if (this.parkingLotId) {
					params.parkingLotId = this.parkingLotId
				}
				
				const res = await this.$u.api.GET(url.queryParkingFee, params)
				
			if ((res.code === 0 || res.code === 200) && res.data) {
				this.feeInfo = res.data
					// 更新查询时间
					this.currentTime = this.formatTime(new Date())
				} else {
					this.feeInfo = null
					if (res.msg) {
						this.$refs.uToast.show({
							title: res.msg,
							type: 'warning'
						})
					}
				}
			} catch (error) {
				console.error('查询停车费用失败:', error)
				this.feeInfo = null
				this.$refs.uToast.show({
					title: '查询失败，请重试',
					type: 'error'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 查看入场照片
		viewPhoto() {
			if (this.feeInfo && this.feeInfo.photoUrl) {
				uni.previewImage({
					urls: [this.feeInfo.photoUrl]
				})
			}
		},
		
		// 去支付
		goPay() {
			if (!this.feeInfo) return
			
			const payData = {
				plateNumber: this.plateNumber,
				parkingLotId: this.parkingLotId,
				parkingLotName: this.feeInfo.parkingLotName,
				amount: this.feeInfo.amount,
				orderId: this.feeInfo.orderId,
				entryTime: this.feeInfo.entryTime,
				duration: this.feeInfo.duration
			}
			
			uni.navigateTo({
				url: `/package/parking/payment?data=${encodeURIComponent(JSON.stringify(payData))}`
			})
		},
		
		// 重新输入车牌
		reenterPlate() {
			uni.navigateBack()
		}
	}
}
</script>

<style lang="scss" scoped>
.main {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 32rpx;
}

.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 200rpx;
	
	.loading-spinner {
		width: 64rpx;
		height: 64rpx;
		border: 4rpx solid #e0e0e0;
		border-top-color: #1890FF;
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}
	
	text {
		margin-top: 24rpx;
		font-size: 28rpx;
		color: #666;
	}
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.fee-container {
	.amount-card {
		background: linear-gradient(135deg, #1890FF, #36A3FF);
		border-radius: 24rpx;
		padding: 48rpx 32rpx;
		text-align: center;
		box-shadow: 0 8rpx 32rpx rgba(24, 144, 255, 0.3);
		
		.amount-label {
			font-size: 28rpx;
			color: rgba(255, 255, 255, 0.85);
			margin-bottom: 16rpx;
		}
		
		.amount-value {
			display: flex;
			align-items: baseline;
			justify-content: center;
			
			.amount-number {
				font-size: 80rpx;
				font-weight: bold;
				color: #ffffff;
			}
			
			.amount-unit {
				font-size: 36rpx;
				color: #ffffff;
				margin-left: 8rpx;
			}
		}
	}
	
	.detail-card {
		margin-top: 32rpx;
		background: #ffffff;
		border-radius: 24rpx;
		padding: 32rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
		
		.detail-row {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 24rpx 0;
			border-bottom: 1rpx solid #f5f5f5;
			
			&:last-child {
				border-bottom: none;
			}
			
			.label {
				font-size: 28rpx;
				color: #666;
				flex-shrink: 0;
			}
			
			.value {
				font-size: 28rpx;
				color: #333;
				text-align: right;
				flex: 1;
				margin-left: 16rpx;
				
				&.highlight {
					color: #1890FF;
					font-weight: 500;
				}
				
				&.duration {
					color: #FF6B00;
					font-weight: 600;
				}
			}
			
			.plate-value {
				display: flex;
				align-items: center;
				justify-content: flex-end;
				flex: 1;
				
				.view-photo {
					margin-left: 12rpx;
					font-size: 24rpx;
					color: #1890FF;
				}
			}
		}
	}
	
	.pay-btn {
		margin-top: 48rpx;
		width: 100%;
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
	}
	
	.tips-section {
		margin-top: 24rpx;
		text-align: center;
		
		.tips-row {
			font-size: 26rpx;
			
			.tips-text {
				color: #666;
			}
			
			.tips-highlight {
				color: #FF6B00;
				font-weight: 600;
				margin: 0 4rpx;
			}
		}
	}
	
	.reenter-section {
		margin-top: 32rpx;
		text-align: center;
		
		text {
			font-size: 28rpx;
			color: #1890FF;
		}
	}
}

.empty-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 160rpx;
	
	.empty-icon {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 32rpx;
		background: #f0f0f0;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		
		.empty-emoji {
			font-size: 80rpx;
			opacity: 0.5;
		}
	}
	
	.empty-text {
		font-size: 32rpx;
		color: #333;
		margin-bottom: 12rpx;
	}
	
	.empty-sub {
		font-size: 26rpx;
		color: #999;
	}
	
	.reenter-btn {
		margin-top: 48rpx;
		padding: 24rpx 64rpx;
		background: #1890FF;
		border-radius: 40rpx;
		
		text {
			font-size: 30rpx;
			color: #ffffff;
		}
	}
}
</style>
