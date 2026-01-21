<template>
	<view class="main">
		<!-- 顶部标签 -->
		<view class="tabs">
			<view class="tab-item" :class="{active: currentTab === 'all'}" @click="switchTab('all')">
				<text>全部</text>
			</view>
			<view class="tab-item" :class="{active: currentTab === 'paid'}" @click="switchTab('paid')">
				<text>已支付</text>
			</view>
			<view class="tab-item" :class="{active: currentTab === 'unpaid'}" @click="switchTab('unpaid')">
				<text>待支付</text>
			</view>
		</view>
		
		<!-- 列表区域 -->
		<scroll-view 
			class="list-container" 
			scroll-y 
			@scrolltolower="loadMore"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="order-list" v-if="orderList.length > 0">
				<view class="order-item" v-for="(item, index) in orderList" :key="index" @click="viewDetail(item)">
					<view class="order-header">
						<text class="parking-name">{{item.parkingLotName || '停车场'}}</text>
						<text class="order-status" :class="item.status === 1 ? 'paid' : 'unpaid'">
							{{item.status === 1 ? '已支付' : '待支付'}}
						</text>
					</view>
					
					<view class="order-body">
						<view class="info-row">
							<text class="label">车牌号:</text>
							<text class="value plate">{{item.plateNumber}}</text>
						</view>
						<view class="info-row">
							<text class="label">入场时间:</text>
							<text class="value">{{item.entryTime}}</text>
						</view>
						<view class="info-row">
							<text class="label">出场时间:</text>
							<text class="value">{{item.exitTime || '--'}}</text>
						</view>
						<view class="info-row">
							<text class="label">停车时长:</text>
							<text class="value">{{item.duration}}</text>
						</view>
					</view>
					
					<view class="order-footer">
						<view class="amount-section">
							<text class="amount-label">金额:</text>
							<text class="amount-value">¥{{item.amount}}</text>
						</view>
						<view class="pay-btn" v-if="item.status !== 1" @click.stop="goPay(item)">
							<text>去支付</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-container" v-else-if="!loading">
				<view class="empty-icon">
					<text class="empty-emoji">📋</text>
				</view>
				<text class="empty-text">暂无停车记录</text>
			</view>
			
			<!-- 加载更多 -->
			<view class="load-more" v-if="orderList.length > 0">
				<text v-if="loading">加载中...</text>
				<text v-else-if="noMore">没有更多了</text>
			</view>
		</scroll-view>
		
		<u-toast ref="uToast" />
	</view>
</template>

<script>
import { mapGetters } from "vuex"
import url from "../../common/http/URL.js"

export default {
	data() {
		return {
			currentTab: 'all',
			orderList: [],
			loading: false,
			refreshing: false,
			noMore: false,
			page: 1,
			pageSize: 10
		}
	},
	computed: {
		...mapGetters(['getUser'])
	},
	onLoad() {
		this.loadData()
	},
	methods: {
		// 切换标签
		switchTab(tab) {
			if (this.currentTab === tab) return
			this.currentTab = tab
			this.page = 1
			this.noMore = false
			this.orderList = []
			this.loadData()
		},
		
		// 加载数据
		async loadData() {
			if (this.loading) return
			
			this.loading = true
			
			try {
				const params = {
					page: this.page,
					pageSize: this.pageSize
				}
				
				if (this.currentTab === 'paid') {
					params.status = 1
				} else if (this.currentTab === 'unpaid') {
					params.status = 0
				}
				
				const res = await this.$u.api.GET(url.parkingOrderList, params)
				
				if (res.code === 0 || res.code === 200) {
					const list = res.data?.list || res.data || []
					if (this.page === 1) {
						this.orderList = list
					} else {
						this.orderList = [...this.orderList, ...list]
					}
					
					if (list.length < this.pageSize) {
						this.noMore = true
					}
				}
			} catch (error) {
				console.error('加载订单列表失败:', error)
				this.$refs.uToast.show({
					title: '加载失败，请重试',
					type: 'error'
				})
			} finally {
				this.loading = false
				this.refreshing = false
			}
		},
		
		// 下拉刷新
		onRefresh() {
			this.refreshing = true
			this.page = 1
			this.noMore = false
			this.loadData()
		},
		
		// 加载更多
		loadMore() {
			if (this.loading || this.noMore) return
			this.page++
			this.loadData()
		},
		
		// 查看详情
		viewDetail(item) {
			// 跳转到费用明细页面查看详情
			uni.navigateTo({
				url: `/package/parking/fee-detail?plateNumber=${encodeURIComponent(item.plateNumber)}&orderId=${item.orderId}`
			})
		},
		
		// 去支付
		goPay(item) {
			const payData = {
				plateNumber: item.plateNumber,
				parkingLotId: item.parkingLotId,
				parkingLotName: item.parkingLotName,
				amount: item.amount,
				orderId: item.orderId,
				entryTime: item.entryTime,
				duration: item.duration
			}
			
			uni.navigateTo({
				url: `/package/parking/payment?data=${encodeURIComponent(JSON.stringify(payData))}`
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

.tabs {
	display: flex;
	background: #ffffff;
	padding: 0 32rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	
	.tab-item {
		flex: 1;
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		
		text {
			font-size: 28rpx;
			color: #666;
		}
		
		&.active {
			text {
				color: #1890FF;
				font-weight: 600;
			}
			
			&::after {
				content: '';
				position: absolute;
				bottom: 0;
				left: 50%;
				transform: translateX(-50%);
				width: 48rpx;
				height: 4rpx;
				background: #1890FF;
				border-radius: 2rpx;
			}
		}
	}
}

.list-container {
	flex: 1;
	padding: 24rpx 32rpx;
}

.order-list {
	.order-item {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 28rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
		
		.order-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding-bottom: 20rpx;
			border-bottom: 1rpx solid #f5f5f5;
			
			.parking-name {
				font-size: 30rpx;
				font-weight: 600;
				color: #333;
			}
			
			.order-status {
				font-size: 24rpx;
				padding: 6rpx 16rpx;
				border-radius: 20rpx;
				
				&.paid {
					color: #52C41A;
					background: rgba(82, 196, 26, 0.1);
				}
				
				&.unpaid {
					color: #FF6B00;
					background: rgba(255, 107, 0, 0.1);
				}
			}
		}
		
		.order-body {
			padding: 20rpx 0;
			
			.info-row {
				display: flex;
				justify-content: space-between;
				padding: 8rpx 0;
				
				.label {
					font-size: 26rpx;
					color: #999;
				}
				
				.value {
					font-size: 26rpx;
					color: #333;
					
					&.plate {
						color: #1890FF;
						font-weight: 500;
					}
				}
			}
		}
		
		.order-footer {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding-top: 20rpx;
			border-top: 1rpx solid #f5f5f5;
			
			.amount-section {
				.amount-label {
					font-size: 26rpx;
					color: #666;
				}
				
				.amount-value {
					font-size: 36rpx;
					font-weight: bold;
					color: #FF6B00;
					margin-left: 8rpx;
				}
			}
			
			.pay-btn {
				padding: 16rpx 40rpx;
				background: linear-gradient(135deg, #1890FF, #36A3FF);
				border-radius: 32rpx;
				
				text {
					font-size: 26rpx;
					color: #ffffff;
				}
				
				&:active {
					opacity: 0.9;
				}
			}
		}
	}
}

.empty-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 120rpx;
	
	.empty-icon {
		width: 160rpx;
		height: 160rpx;
		margin-bottom: 24rpx;
		background: #f5f5f5;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		
		.empty-emoji {
			font-size: 64rpx;
			opacity: 0.5;
		}
	}
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
}

.load-more {
	text-align: center;
	padding: 24rpx 0;
	
	text {
		font-size: 24rpx;
		color: #999;
	}
}
</style>
