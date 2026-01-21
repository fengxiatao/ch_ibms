<template>
	<view class="main">
		<!-- 顶部标题区域 -->
		<view class="header">
			<view class="title">停车缴费</view>
			<view class="subtitle">扫码或输入车牌号查询停车费用</view>
		</view>
		
		<!-- 车牌输入区域 -->
		<view class="plate-input-section">
			<view class="section-title">车牌号码</view>
			<view class="plate-input-wrapper">
				<car-number v-model="plateNumber" @change="changeCarNumber"></car-number>
			</view>
			<view class="query-btn" @click="queryFee">
				<text>立即缴费</text>
			</view>
			<view class="tips">
				<text class="tips-icon">💡</text>
				<text>温馨提示：请输入正确的车牌号</text>
			</view>
		</view>
		
		<!-- 历史车牌区域 -->
		<view class="history-section" v-if="historyPlates.length > 0">
			<view class="section-header">
				<text class="section-title">历史车牌</text>
			</view>
			<view class="history-list">
				<view class="history-item" v-for="(item, index) in historyPlates" :key="index" @click="selectHistory(item)">
					<text class="plate-text">{{item}}</text>
					<view class="delete-btn" @click.stop="deleteHistory(index)">
						<u-icon name="close" size="28" color="#999"></u-icon>
					</view>
				</view>
			</view>
		</view>
		
		<u-toast ref="uToast" />
	</view>
</template>

<script>
import { mapGetters } from "vuex"
import CarNumber from '@/components/codecook-carnumber/codecook-carnumber.vue'
import url from "@/common/http/URL.js"

const PARKING_HISTORY_KEY = 'parkingPlateHistory'

export default {
	components: {
		CarNumber
	},
	data() {
		return {
			plateNumber: '',
			historyPlates: [],
			parkingLotId: '', // 停车场ID（从扫码获取）
			parkingLotName: '', // 停车场名称
			verify: false
		}
	},
	computed: {
		...mapGetters(['getUser'])
	},
	onLoad(options) {
		// 处理扫码进入的参数
		if (options && options.scene) {
			const sceneText = decodeURIComponent(options.scene)
			console.log('[停车缴费] scene参数:', sceneText)
			this.parseParkingParams(sceneText)
		} else if (options && options.parkingLotId) {
			this.parkingLotId = options.parkingLotId
			this.parkingLotName = options.parkingLotName || ''
		}
		
		this.loadHistory()
	},
	methods: {
		// 解析停车场参数
		parseParkingParams(sceneText) {
			if (!sceneText) return
			
			// 格式: parkingLotId=xxx 或直接是停车场ID
			if (sceneText.includes('=')) {
				const params = {}
				sceneText.split('&').forEach(pair => {
					const [key, value] = pair.split('=')
					if (key && value) {
						params[key] = decodeURIComponent(value)
					}
				})
				this.parkingLotId = params.parkingLotId || params.id || ''
				this.parkingLotName = params.name || ''
			} else {
				this.parkingLotId = sceneText
			}
		},
		
		// 车牌号变化
		changeCarNumber(val) {
			console.log('车牌号变化:', val)
			if (val.length < 7) {
				this.verify = false
			} else {
				this.verify = !val.includes('')
			}
		},
		
		// 查询费用
		async queryFee() {
			if (!this.verify || !this.plateNumber) {
				this.$refs.uToast.show({
					title: '请输入完整的车牌号',
					type: 'warning'
				})
				return
			}
			
			// 保存到历史记录
			this.saveToHistory(this.plateNumber)
			
			// 跳转到费用明细页面
			uni.navigateTo({
				url: `/package/parking/fee-detail?plateNumber=${encodeURIComponent(this.plateNumber)}&parkingLotId=${this.parkingLotId}`
			})
		},
		
		// 选择历史车牌
		selectHistory(plate) {
			this.plateNumber = plate
			this.verify = true
			this.queryFee()
		},
		
		// 加载历史记录
		loadHistory() {
			const history = uni.getStorageSync(PARKING_HISTORY_KEY)
			if (history && Array.isArray(history)) {
				this.historyPlates = history.slice(0, 5) // 最多显示5条
			}
		},
		
		// 保存到历史记录
		saveToHistory(plate) {
			let history = uni.getStorageSync(PARKING_HISTORY_KEY) || []
			// 去重并放到最前面
			history = history.filter(item => item !== plate)
			history.unshift(plate)
			// 最多保存10条
			history = history.slice(0, 10)
			uni.setStorageSync(PARKING_HISTORY_KEY, history)
			this.historyPlates = history.slice(0, 5)
		},
		
		// 删除历史记录
		deleteHistory(index) {
			this.historyPlates.splice(index, 1)
			uni.setStorageSync(PARKING_HISTORY_KEY, this.historyPlates)
		}
	}
}
</script>

<style lang="scss" scoped>
.main {
	min-height: 100vh;
	background: linear-gradient(180deg, #1890FF, #f5f7fa 280rpx);
	padding: 0 32rpx;
	padding-bottom: 60rpx;
}

.header {
	padding-top: 60rpx;
	padding-bottom: 40rpx;
	
	.title {
		font-size: 48rpx;
		font-weight: bold;
		color: #ffffff;
		text-align: center;
	}
	
	.subtitle {
		margin-top: 16rpx;
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.85);
		text-align: center;
	}
}

.plate-input-section {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 40rpx 32rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
	
	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 24rpx;
	}
	
	.plate-input-wrapper {
		margin-bottom: 32rpx;
	}
	
	.query-btn {
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
	
	.tips {
		margin-top: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		
		.tips-icon {
			margin-right: 8rpx;
		}
		
		text {
			font-size: 24rpx;
			color: #FF9500;
		}
	}
}

.history-section {
	margin-top: 32rpx;
	background: #ffffff;
	border-radius: 24rpx;
	padding: 32rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	
	.section-header {
		margin-bottom: 24rpx;
		
		.section-title {
			font-size: 30rpx;
			font-weight: 600;
			color: #333;
		}
	}
	
	.history-list {
		.history-item {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 24rpx 0;
			border-bottom: 1rpx solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.plate-text {
				font-size: 32rpx;
				color: #1890FF;
				font-weight: 500;
				letter-spacing: 2rpx;
			}
			
			.delete-btn {
				padding: 8rpx;
			}
		}
	}
}
</style>
