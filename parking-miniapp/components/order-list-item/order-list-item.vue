<template>
	<view 
		class="order-list-item" 
		@click="handleClick"
		:class="statusClass"
	>
		<!-- Order Header -->
		<view class="item-header">
			<view class="station-info">
				<text class="station-name">{{ order.chargingStationName || '广州邮通科技园东信科技大厦充电桩' }}</text>
				<text class="pile-info">车位：{{ order.chargingPileName || order.code || 'N/A' }}</text>
			</view>
			<view class="status-badge" :class="statusBadgeClass">
				<text class="status-text">{{ statusText }}</text>
			</view>
		</view>

		<!-- Order Details -->
		<view class="item-details">
			<view class="detail-row">
				<text class="detail-label">订单编号</text>
				<text class="detail-value">{{ order.orderId || order.orderNo || 'N/A' }}</text>
			</view>
			
			<view class="detail-row" v-if="order.chargingPileId || order.gunNumber">
				<text class="detail-label">充电枪号</text>
				<text class="detail-value">{{ order.gunNumber || order.chargingPileId || 'N/A' }}</text>
			</view>

			<!-- Time Information -->
			<view class="time-section" v-if="order.paymentTime || order.chargingStartTime">
				<view class="time-row">
					<text class="time-label">开始时间</text>
					<text class="time-value">{{ formatDateTime(order.chargingStartTime || order.startTime) }}</text>
				</view>
				<view class="time-row" v-if="order.chargingEndTime || order.endTime">
					<text class="time-label">结束时间</text>
					<text class="time-value">{{ formatDateTime(order.chargingEndTime || order.endTime) }}</text>
				</view>
			</view>
		</view>

		<!-- Divider -->
		<view class="divider"></view>

		<!-- Amount Information -->
		<view class="item-footer">
			<view class="amount-section">
				<view class="amount-row">
					<text class="amount-label">预付金额</text>
					<text class="amount-value primary">¥{{ formatAmount(order.prepaymentAmount) }}</text>
				</view>
				<view class="amount-row" v-if="order.actualConsumption !== undefined && order.actualConsumption !== null">
					<text class="amount-label">实际消费</text>
					<text class="amount-value">¥{{ formatAmount(order.actualConsumption || order.actualArrivalMoney) }}</text>
				</view>
				<view class="amount-row" v-if="showRefundInfo && order.refundAmount">
					<text class="amount-label refund-label">退款金额</text>
					<text class="amount-value refund-amount">¥{{ formatAmount(order.refundAmount) }}</text>
				</view>
			</view>
			
			<view class="electricity-info" v-if="order.electricityUsed || order.dosage">
				<text class="electricity-value">{{ formatAmount(order.electricityUsed || order.dosage) }}</text>
				<text class="electricity-unit">度</text>
			</view>
		</view>

		<!-- Refund Status Display -->
		<view class="refund-status" v-if="showRefundInfo && order.refundStatus">
		<view class="refund-status-row">
			<text class="refund-status-label">退款状态：</text>
			<text class="refund-status-text" :class="refundStatusClass">
				{{ refundStatusText }}
			</text>
		</view>
			<view class="refund-arrival" v-if="order.refundStatus === 'success' && order.refundTime">
				<text class="refund-arrival-text">到账时间：{{ formatDateTime(order.refundTime) }}</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'OrderListItem',
	props: {
		// Order data object
		order: {
			type: Object,
			required: true,
			default: () => ({})
		},
		// Whether to show refund information
		showRefundInfo: {
			type: Boolean,
			default: true
		}
	},
	computed: {
		/**
		 * Get status badge class based on order status
		 * @returns {String} CSS class name
		 */
		statusBadgeClass() {
			const status = this.order.status || '';
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
			const status = this.order.status || '';
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
		 * Get status class for order item background
		 * @returns {String} CSS class name
		 */
		statusClass() {
			// Support legacy transactionStatus field
			const transactionStatus = this.order.transactionStatus;
			if (transactionStatus === 3) {
				return 'yituikuan'; // Already refunded
			}
			if (transactionStatus === 4) {
				return 'tuikuanzhong'; // Refunding
			}
			
			// Use new status field
			const status = this.order.status || '';
			if (status === 'refunded') {
				return 'yituikuan';
			}
			if (this.order.refundStatus === 'processing') {
				return 'tuikuanzhong';
			}
			return '';
		},
		/**
		 * Get refund status class for styling
		 * @returns {String} CSS class name
		 */
		refundStatusClass() {
			const refundStatus = this.order.refundStatus || '';
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
			const refundStatus = this.order.refundStatus || '';
			const statusTextMap = {
				'processing': '退款处理中',
				'success': '退款成功',
				'failed': '退款失败',
				'abnormal': '退款异常'
			};
			return statusTextMap[refundStatus] || '未知';
		}
	},
	methods: {
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
		 * Handle click event to navigate to order detail page
		 */
		handleClick() {
			this.$emit('click', this.order);
			
			// Navigate to order detail page
			// Support legacy transactionStatus field
			if (this.order.transactionStatus === 2) {
				uni.navigateTo({
					url: '/package/chargingPile/pending-order/pending-order'
				});
			} else {
				uni.navigateTo({
					url: '/package/chargingPile/orderFormDetail?item=' + encodeURIComponent(JSON.stringify({
						order: this.order.orderNo || this.order.orderId
					}))
				});
			}
		}
	}
};
</script>

<style lang="scss" scoped>
.order-list-item {
	padding: 34rpx 30rpx 24rpx;
	width: 640rpx;
	border-radius: 20rpx;
	box-shadow: 2rpx 2rpx 20rpx 0rpx rgba(202, 200, 200, 0.3);
	background-color: #ffffff;
	margin-bottom: 36rpx;
	position: relative;
	
	// Background images for refund status
	&.yituikuan {
		background-image: url('http://ismart.loongtek.cn/image/xcx/charge/yituikuan.png');
		background-repeat: no-repeat;
		background-size: 150rpx 150rpx;
		background-position: right -20rpx top -20rpx;
	}
	
	&.tuikuanzhong {
		background-image: url('http://ismart.loongtek.cn/image/xcx/charge/tuikuanzhong.png');
		background-repeat: no-repeat;
		background-size: 150rpx 150rpx;
		background-position: right -20rpx top -20rpx;
	}
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.station-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.station-name {
	font-size: 30rpx;
	font-weight: bold;
	color: #333333;
	line-height: 40rpx;
	margin-bottom: 8rpx;
	max-width: 480rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	text-overflow: ellipsis;
	word-break: break-all;
}

.pile-info {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
}

.status-badge {
	padding: 6rpx 16rpx;
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
	font-size: 22rpx;
	color: #ffffff;
	line-height: 28rpx;
}

.item-details {
	margin-bottom: 20rpx;
}

.detail-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.detail-label {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
}

.detail-value {
	font-size: 26rpx;
	color: #333333;
	line-height: 32rpx;
	max-width: 400rpx;
	text-align: right;
	word-break: break-all;
}

.time-section {
	margin-top: 20rpx;
}

.time-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.time-label {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
}

.time-value {
	font-size: 26rpx;
	color: #333333;
	line-height: 32rpx;
}

.divider {
	width: 100%;
	height: 2rpx;
	background-color: rgba(51, 51, 51, 0.1);
	margin: 20rpx 0;
}

.item-footer {
	display: flex;
	justify-content: space-between;
	align-items: flex-end;
}

.amount-section {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.amount-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.amount-label {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
	
	&.refund-label {
		color: #9B59B6;
	}
}

.amount-value {
	font-size: 30rpx;
	font-weight: bold;
	color: #f4a7a0;
	line-height: 36rpx;
	
	&.primary {
		font-size: 32rpx;
	}
	
	&.refund-amount {
		color: #9B59B6;
	}
}

.electricity-info {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	margin-left: 20rpx;
}

.electricity-value {
	font-size: 36rpx;
	font-weight: bold;
	color: #f4a7a0;
	line-height: 42rpx;
}

.electricity-unit {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
	margin-top: 4rpx;
}

.refund-status {
	margin-top: 20rpx;
	padding-top: 20rpx;
	border-top: 2rpx solid rgba(51, 51, 51, 0.1);
}

.refund-status-row {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
}

.refund-status-label {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
}

.refund-status-text {
	font-size: 26rpx;
	font-weight: bold;
	line-height: 32rpx;
	
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

.refund-arrival {
	margin-top: 8rpx;
}

.refund-arrival-text {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
}
</style>
