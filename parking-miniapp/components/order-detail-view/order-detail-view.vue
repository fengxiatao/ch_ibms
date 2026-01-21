<template>
	<view class="order-detail-view">
		<!-- Loading State -->
		<view v-if="loading" class="loading-container">
			<u-loading mode="circle" size="60"></u-loading>
			<text class="loading-text">加载中...</text>
		</view>
		
		<!-- Error State -->
		<view v-else-if="error" class="error-container">
			<text class="error-icon">⚠️</text>
			<text class="error-text">{{ errorMessage }}</text>
			<u-button @click="loadOrderDetail" type="primary" size="small">重试</u-button>
		</view>
		
		<!-- Order Detail Content -->
		<view v-else-if="orderData" class="detail-content">
			<!-- Order Summary Card -->
			<view class="summary-card">
				<view class="summary-header">
					<text class="station-name">{{ orderData.chargingStationName || '充电站' }}</text>
				<view class="status-badge" :class="statusBadgeClass">
					<text class="status-text">{{ statusText }}</text>
				</view>
				</view>
				
				<view class="summary-amounts">
					<view class="amount-item">
						<text class="amount-label">预付金额</text>
						<text class="amount-value primary">¥{{ formatAmount(orderData.prepaymentAmount) }}</text>
					</view>
					<view class="amount-item" v-if="orderData.actualConsumption !== undefined && orderData.actualConsumption !== null">
						<text class="amount-label">实际消费</text>
						<text class="amount-value">¥{{ formatAmount(orderData.actualConsumption) }}</text>
					</view>
					<view class="amount-item" v-if="orderData.refundAmount && orderData.refundAmount > 0">
						<text class="amount-label refund-label">退款金额</text>
						<text class="amount-value refund">¥{{ formatAmount(orderData.refundAmount) }}</text>
					</view>
				</view>
			</view>
			
			<!-- Order Timeline -->
			<order-timeline :order="orderData"></order-timeline>
			
			<!-- Order Information -->
			<view class="info-card">
				<view class="info-title">订单信息</view>
				
				<view class="info-row">
					<text class="info-label">订单编号</text>
					<text class="info-value">{{ orderData.orderId || orderData.orderNo || 'N/A' }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.chargingPileId || orderData.chargingPileName">
					<text class="info-label">充电桩</text>
					<text class="info-value">{{ orderData.chargingPileName || orderData.chargingPileId || 'N/A' }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.licensePlate || orderData.carNum">
					<text class="info-label">车牌号</text>
					<text class="info-value">{{ orderData.licensePlate || orderData.carNum || 'N/A' }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.paymentMethod">
					<text class="info-label">支付方式</text>
					<text class="info-value">{{ getPaymentMethodText() }}</text>
				</view>
			</view>
			
			<!-- Time Information -->
			<view class="info-card">
				<view class="info-title">时间信息</view>
				
				<view class="info-row" v-if="orderData.createdAt">
					<text class="info-label">创建时间</text>
					<text class="info-value">{{ formatDateTime(orderData.createdAt) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.paymentTime">
					<text class="info-label">支付时间</text>
					<text class="info-value">{{ formatDateTime(orderData.paymentTime) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.chargingStartTime || orderData.startDateTime">
					<text class="info-label">开始充电</text>
					<text class="info-value">{{ formatDateTime(orderData.chargingStartTime || orderData.startDateTime) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.chargingEndTime || orderData.endDateTime">
					<text class="info-label">结束充电</text>
					<text class="info-value">{{ formatDateTime(orderData.chargingEndTime || orderData.endDateTime) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.chargingDuration || orderData.totalDuration">
					<text class="info-label">充电时长</text>
					<text class="info-value">{{ orderData.chargingDuration || orderData.totalDuration }} 分钟</text>
				</view>
			</view>
			
			<!-- Consumption Breakdown -->
			<view class="info-card" v-if="showConsumptionBreakdown()">
				<view class="info-title">消费明细</view>
				
				<view class="info-row" v-if="orderData.electricityUsed || orderData.cumulativeQuantity">
					<text class="info-label">用电量</text>
					<text class="info-value">{{ formatAmount(orderData.electricityUsed || orderData.cumulativeQuantity) }} 度</text>
				</view>
				
				<view class="info-row" v-if="orderData.electricChargeMoney">
					<text class="info-label">电费</text>
					<text class="info-value">¥{{ formatAmount(orderData.electricChargeMoney) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.serviceMoney">
					<text class="info-label">服务费</text>
					<text class="info-value">¥{{ formatAmount(orderData.serviceMoney) }}</text>
				</view>
				
				<view class="info-row" v-if="hasDiscount()">
					<text class="info-label">优惠金额</text>
					<text class="info-value discount">-¥{{ formatAmount(getDiscountAmount()) }}</text>
				</view>
			</view>
			
			<!-- Refund Information -->
			<view class="info-card refund-card" v-if="orderData.refundAmount && orderData.refundAmount > 0">
				<view class="info-title">退款信息</view>
				
				<view class="info-row">
					<text class="info-label">退款金额</text>
					<text class="info-value refund-amount">¥{{ formatAmount(orderData.refundAmount) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.refundStatus">
					<text class="info-label">退款状态</text>
					<text class="info-value" :class="refundStatusClass">
						{{ refundStatusText }}
					</text>
				</view>
				
				<view class="info-row" v-if="orderData.refundTime">
					<text class="info-label">退款时间</text>
					<text class="info-value">{{ formatDateTime(orderData.refundTime) }}</text>
				</view>
				
				<view class="info-row" v-if="orderData.estimatedArrival">
					<text class="info-label">预计到账</text>
					<text class="info-value">{{ orderData.estimatedArrival }}</text>
				</view>
				
				<view class="refund-notice" v-if="orderData.refundStatus === 'processing'">
					<text class="notice-icon">ℹ️</text>
					<text class="notice-text">退款处理中，请耐心等待</text>
				</view>
				
				<view class="refund-notice error" v-if="orderData.refundStatus === 'failed'">
					<text class="notice-icon">⚠️</text>
					<text class="notice-text">退款失败，请联系客服处理</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import OrderTimeline from '../order-timeline/order-timeline.vue';
import url from '../../common/http/URL.js';
import { mapGetters } from 'vuex';

export default {
	name: 'OrderDetailView',
	components: {
		OrderTimeline
	},
	props: {
		// Order ID to load
		orderId: {
			type: String,
			required: true
		}
	},
	data() {
		return {
			loading: false,
			error: false,
			errorMessage: '',
			orderData: null
		};
	},
	computed: {
		...mapGetters(['getUser']),
		/**
		 * Get status badge class based on order status
		 * @returns {String} CSS class name
		 */
		statusBadgeClass() {
			if (!this.orderData) return 'status-default';
			const status = this.orderData.status || '';
			const statusMap = {
				'created': 'status-created',
				'paid': 'status-paid',
				'charging': 'status-charging',
				'completed': 'status-completed',
				'refunded': 'status-refunded',
				'cancelled': 'status-cancelled'
			};
			return statusMap[status] || 'status-default';
		},
		/**
		 * Get status text for display
		 * @returns {String} Status text
		 */
		statusText() {
			if (!this.orderData) return '未知';
			const status = this.orderData.status || '';
			const statusTextMap = {
				'created': '已创建',
				'paid': '已支付',
				'charging': '充电中',
				'completed': '已完成',
				'refunded': '已退款',
				'cancelled': '已取消'
			};
			return statusTextMap[status] || '未知';
		},
		/**
		 * Get refund status class for styling
		 * @returns {String} CSS class name
		 */
		refundStatusClass() {
			if (!this.orderData) return '';
			const refundStatus = this.orderData.refundStatus || '';
			const statusMap = {
				'processing': 'refund-processing',
				'success': 'refund-success',
				'failed': 'refund-failed',
				'abnormal': 'refund-abnormal'
			};
			return statusMap[refundStatus] || '';
		},
		/**
		 * Get refund status text for display
		 * @returns {String} Refund status text
		 */
		refundStatusText() {
			if (!this.orderData) return '未知';
			const refundStatus = this.orderData.refundStatus || '';
			const statusTextMap = {
				'processing': '退款处理中',
				'success': '退款成功',
				'failed': '退款失败',
				'abnormal': '退款异常'
			};
			return statusTextMap[refundStatus] || '未知';
		}
	},
	mounted() {
		this.loadOrderDetail();
	},
	methods: {
		/**
		 * Load order details from API
		 */
		async loadOrderDetail() {
			this.loading = true;
			this.error = false;
			this.errorMessage = '';
			
			try {
				// Try new API endpoint first
				const params = {
					orderId: this.orderId,
					userId: this.getUser.id
				};
				
				// Check if new API endpoint exists
				if (url.getOrderDetail) {
					const response = await this.$u.api.GET(url.getOrderDetail, params);
					if (response.code === 200 && response.data) {
						this.orderData = response.data;
						this.loading = false;
						return;
					}
				}
				
				// Fallback to legacy API
				const legacyParams = {
					orderNo: this.orderId,
					userId: this.getUser.id
				};
				
				const response = await this.$u.api.GET(url.getHistoryChargeDetails, legacyParams);
				
				if (response.code === 200 && response.data) {
					// Map legacy data to new structure
					this.orderData = this.mapLegacyData(response.data);
					this.loading = false;
				} else {
					throw new Error(response.msg || '加载订单详情失败');
				}
			} catch (err) {
				console.error('Load order detail error:', err);
				this.error = true;
				this.errorMessage = err.message || '加载订单详情失败，请稍后重试';
				this.loading = false;
			}
		},
		
		/**
		 * Map legacy data structure to new structure
		 * @param {Object} legacyData - Legacy order data
		 * @returns {Object} Mapped order data
		 */
		mapLegacyData(legacyData) {
			return {
				orderId: legacyData.orderNo,
				orderNo: legacyData.orderNo,
				userId: legacyData.userId,
				chargingStationName: legacyData.chargingStationName || '充电站',
				chargingPileId: legacyData.chargingPileId,
				chargingPileName: legacyData.chargingPileName,
				licensePlate: legacyData.carNum,
				carNum: legacyData.carNum,
				transactionId: legacyData.transactionId,
				prepaymentAmount: legacyData.totalAmount,
				actualConsumption: legacyData.actualArrivalMoney,
				actualArrivalMoney: legacyData.actualArrivalMoney,
				refundAmount: legacyData.refundAmount || 0,
				refundStatus: this.mapLegacyRefundStatus(legacyData.transactionStatus),
				refundTime: legacyData.refundTime,
				status: this.mapLegacyStatus(legacyData.transactionStatus),
				paymentMethod: legacyData.paymentType === 1 ? 'balance' : legacyData.paymentType === 2 ? 'card' : 'wallet',
				paymentTime: legacyData.paymentTime,
				chargingStartTime: legacyData.startDateTime,
				startDateTime: legacyData.startDateTime,
				chargingEndTime: legacyData.endDateTime,
				endDateTime: legacyData.endDateTime,
				chargingDuration: legacyData.totalDuration,
				totalDuration: legacyData.totalDuration,
				electricityUsed: legacyData.cumulativeQuantity,
				cumulativeQuantity: legacyData.cumulativeQuantity,
				electricChargeMoney: legacyData.electricChargeMoney,
				serviceMoney: legacyData.serviceMoney,
				createdAt: legacyData.createdAt || legacyData.startDateTime,
				updatedAt: legacyData.updatedAt
			};
		},
		
		/**
		 * Map legacy transaction status to new status
		 * @param {Number} transactionStatus - Legacy transaction status
		 * @returns {String} New status
		 */
		mapLegacyStatus(transactionStatus) {
			const statusMap = {
				1: 'completed',
				2: 'charging',
				3: 'refunded',
				4: 'processing',
				5: 'completed'
			};
			return statusMap[transactionStatus] || 'completed';
		},
		
		/**
		 * Map legacy transaction status to refund status
		 * @param {Number} transactionStatus - Legacy transaction status
		 * @returns {String} Refund status
		 */
		mapLegacyRefundStatus(transactionStatus) {
			if (transactionStatus === 3) {
				return 'success';
			}
			if (transactionStatus === 4) {
				return 'processing';
			}
			return null;
		},
		
		/**
		 * Format amount to 2 decimal places
		 * @param {Number} amount - Amount to format
		 * @returns {String} Formatted amount
		 */
		formatAmount(amount) {
			if (amount === undefined || amount === null || amount === '') {
				return '0.00';
			}
			const num = parseFloat(amount);
			if (isNaN(num)) {
				return '0.00';
			}
			return num.toFixed(2);
		},
		
		/**
		 * Format datetime for display
		 * @param {String} datetime - Datetime string
		 * @returns {String} Formatted datetime
		 */
		formatDateTime(datetime) {
			if (!datetime) {
				return 'N/A';
			}
			// Handle different datetime formats
			if (typeof datetime === 'string') {
				// Replace 'T' with space for better readability
				return datetime.replace('T', ' ').substring(0, 19);
			}
			return datetime;
		},
		
		/**
		 * Get payment method text
		 * @returns {String} Payment method text
		 */
		getPaymentMethodText() {
			const method = this.orderData.paymentMethod || '';
			const methodMap = {
				'wallet': '微信钱包',
				'balance': '余额支付',
				'card': '银行卡'
			};
			return methodMap[method] || '未知';
		},
		
		/**
		 * Check if consumption breakdown should be shown
		 * @returns {Boolean} True if should show
		 */
		showConsumptionBreakdown() {
			const data = this.orderData;
			return data.electricityUsed || data.cumulativeQuantity || 
			       data.electricChargeMoney || data.serviceMoney;
		},
		
		/**
		 * Check if order has discount
		 * @returns {Boolean} True if has discount
		 */
		hasDiscount() {
			const data = this.orderData;
			if (data.totalAmount && data.actualArrivalMoney) {
				return data.totalAmount > data.actualArrivalMoney;
			}
			return false;
		},
		
		/**
		 * Calculate discount amount
		 * @returns {Number} Discount amount
		 */
		getDiscountAmount() {
			const data = this.orderData;
			if (data.totalAmount && data.actualArrivalMoney) {
				return data.totalAmount - data.actualArrivalMoney;
			}
			return 0;
		}
	}
};
</script>

<style lang="scss" scoped>
.order-detail-view {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 20rpx;
}

.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	min-height: 400rpx;
}

.loading-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.5);
}

.error-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	min-height: 400rpx;
	padding: 40rpx;
}

.error-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.error-text {
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.5);
	text-align: center;
	margin-bottom: 30rpx;
	line-height: 40rpx;
}

.detail-content {
	padding-bottom: 40rpx;
}

.summary-card {
	background-color: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.summary-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.station-name {
	font-size: 34rpx;
	font-weight: bold;
	color: #333333;
	line-height: 42rpx;
	flex: 1;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 30rpx;
	margin-left: 20rpx;
	
	&.status-created {
		background-color: #999999;
	}
	
	&.status-paid {
		background-color: #409EFF;
	}
	
	&.status-charging {
		background-color: #67C23A;
	}
	
	&.status-completed {
		background-color: #E6A23C;
	}
	
	&.status-refunded {
		background-color: #9B59B6;
	}
	
	&.status-cancelled {
		background-color: #F56C6C;
	}
	
	&.status-default {
		background-color: #DCDFE6;
	}
}

.status-text {
	font-size: 24rpx;
	color: #ffffff;
	line-height: 32rpx;
}

.summary-amounts {
	display: flex;
	flex-direction: column;
}

.amount-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.amount-label {
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 36rpx;
	
	&.refund-label {
		color: #9B59B6;
	}
}

.amount-value {
	font-size: 36rpx;
	font-weight: bold;
	color: #f4a7a0;
	line-height: 42rpx;
	
	&.primary {
		font-size: 40rpx;
	}
	
	&.refund {
		color: #9B59B6;
	}
}

.info-card {
	background-color: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	
	&.refund-card {
		border: 2rpx solid #9B59B6;
	}
}

.info-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
	margin-bottom: 24rpx;
	line-height: 42rpx;
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.info-label {
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 36rpx;
	flex-shrink: 0;
	margin-right: 20rpx;
}

.info-value {
	font-size: 28rpx;
	color: #333333;
	line-height: 36rpx;
	text-align: right;
	flex: 1;
	word-break: break-all;
	
	&.discount {
		color: #E6A23C;
	}
	
	&.refund-amount {
		color: #9B59B6;
		font-weight: bold;
		font-size: 32rpx;
	}
	
	&.refund-processing {
		color: #409EFF;
	}
	
	&.refund-success {
		color: #67C23A;
	}
	
	&.refund-failed {
		color: #F56C6C;
	}
	
	&.refund-abnormal {
		color: #E6A23C;
	}
}

.refund-notice {
	margin-top: 20rpx;
	padding: 20rpx;
	background-color: #E8F4FF;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	
	&.error {
		background-color: #FEF0F0;
	}
}

.notice-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.notice-text {
	font-size: 26rpx;
	color: #409EFF;
	line-height: 36rpx;
	flex: 1;
	
	.error & {
		color: #F56C6C;
	}
}
</style>
