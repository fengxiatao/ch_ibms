<template>
	<view class="refund-status-display">
		<!-- Refund Header -->
		<view class="refund-header">
			<text class="refund-icon cuIcon-moneybag"></text>
			<text class="refund-title">退款信息</text>
		</view>

		<!-- Refund Amount -->
		<view class="refund-amount-section">
			<text class="amount-label">退款金额</text>
			<text class="amount-value" :class="getAmountClass()">
				¥{{ formatAmount(refundAmount) }}
			</text>
		</view>

		<!-- Refund Status -->
		<view class="refund-status-section">
			<view class="status-row">
				<text class="status-label">退款状态</text>
				<view class="status-badge" :class="getStatusBadgeClass()">
					<text class="status-icon" :class="getStatusIcon()"></text>
					<text class="status-text">{{ getStatusText() }}</text>
				</view>
			</view>

			<!-- Status Description -->
			<view class="status-description" v-if="getStatusDescription()">
				<text class="description-text">{{ getStatusDescription() }}</text>
			</view>
		</view>

		<!-- Arrival Time Information -->
		<view class="arrival-section" v-if="showArrivalInfo()">
			<!-- Estimated Arrival (for processing status) -->
			<view class="arrival-row" v-if="refundStatus === 'processing' && estimatedArrival">
				<text class="arrival-label">预计到账</text>
				<text class="arrival-value">{{ estimatedArrival }}</text>
			</view>

			<!-- Actual Arrival (for success status) -->
			<view class="arrival-row" v-if="refundStatus === 'success' && actualArrival">
				<text class="arrival-label">到账时间</text>
				<text class="arrival-value">{{ formatDateTime(actualArrival) }}</text>
			</view>

			<!-- Arrival Time Hint -->
			<view class="arrival-hint" v-if="refundStatus === 'processing'">
				<text class="hint-icon cuIcon-infofill"></text>
				<text class="hint-text">{{ getArrivalHint() }}</text>
			</view>
		</view>

		<!-- Failed Reason (for failed status) -->
		<view class="failed-section" v-if="refundStatus === 'failed' && failureReason">
			<view class="failed-header">
				<text class="failed-icon cuIcon-warnfill"></text>
				<text class="failed-title">失败原因</text>
			</view>
			<text class="failed-reason">{{ failureReason }}</text>
			<text class="failed-contact">如有疑问，请联系客服</text>
		</view>

		<!-- Abnormal Notice (for abnormal status) -->
		<view class="abnormal-section" v-if="refundStatus === 'abnormal'">
			<view class="abnormal-header">
				<text class="abnormal-icon cuIcon-warnfill"></text>
				<text class="abnormal-title">退款异常</text>
			</view>
			<text class="abnormal-text">退款处理出现异常，我们正在处理中</text>
			<text class="abnormal-contact">如需帮助，请联系客服</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'RefundStatusDisplay',
	props: {
		// Refund amount in yuan
		refundAmount: {
			type: [Number, String],
			required: true,
			default: 0
		},
		// Refund status: 'processing', 'success', 'failed', 'abnormal'
		refundStatus: {
			type: String,
			required: true,
			default: 'processing',
			validator: (value) => {
				return ['processing', 'success', 'failed', 'abnormal'].includes(value);
			}
		},
		// Estimated arrival time description (e.g., "5分钟内", "1-3个工作日")
		estimatedArrival: {
			type: String,
			default: ''
		},
		// Actual arrival time (ISO datetime string or formatted string)
		actualArrival: {
			type: String,
			default: ''
		},
		// Failure reason (for failed status)
		failureReason: {
			type: String,
			default: ''
		},
		// Payment method (wallet or card) - affects arrival time hint
		paymentMethod: {
			type: String,
			default: 'wallet',
			validator: (value) => {
				return ['wallet', 'card'].includes(value);
			}
		}
	},
	methods: {
		/**
		 * Format amount to 2 decimal places
		 * @param {Number|String} amount - Amount to format
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
				return '';
			}
			// Handle different datetime formats
			if (typeof datetime === 'string') {
				// Replace 'T' with space for better readability
				return datetime.replace('T', ' ').substring(0, 19);
			}
			return datetime;
		},

		/**
		 * Get CSS class for amount based on status
		 * @returns {String} CSS class name
		 */
		getAmountClass() {
			const statusClassMap = {
				'processing': 'amount-processing',
				'success': 'amount-success',
				'failed': 'amount-failed',
				'abnormal': 'amount-abnormal'
			};
			return statusClassMap[this.refundStatus] || '';
		},

		/**
		 * Get status badge CSS class
		 * @returns {String} CSS class name
		 */
		getStatusBadgeClass() {
			const statusClassMap = {
				'processing': 'badge-processing',
				'success': 'badge-success',
				'failed': 'badge-failed',
				'abnormal': 'badge-abnormal'
			};
			return statusClassMap[this.refundStatus] || '';
		},

		/**
		 * Get status icon class
		 * @returns {String} Icon class name
		 */
		getStatusIcon() {
			const iconMap = {
				'processing': 'cuIcon-loading',
				'success': 'cuIcon-roundcheckfill',
				'failed': 'cuIcon-roundclosefill',
				'abnormal': 'cuIcon-warnfill'
			};
			return iconMap[this.refundStatus] || '';
		},

		/**
		 * Get status text for display
		 * @returns {String} Status text
		 */
		getStatusText() {
			const statusTextMap = {
				'processing': '退款处理中',
				'success': '退款成功',
				'failed': '退款失败',
				'abnormal': '退款异常'
			};
			return statusTextMap[this.refundStatus] || '未知状态';
		},

		/**
		 * Get status description
		 * @returns {String} Status description
		 */
		getStatusDescription() {
			const descriptionMap = {
				'processing': '退款申请已提交，正在处理中',
				'success': '退款已成功到账',
				'failed': '退款处理失败',
				'abnormal': '退款处理出现异常'
			};
			return descriptionMap[this.refundStatus] || '';
		},

		/**
		 * Check if should show arrival info
		 * @returns {Boolean}
		 */
		showArrivalInfo() {
			return (this.refundStatus === 'processing' && this.estimatedArrival) ||
			       (this.refundStatus === 'success' && this.actualArrival);
		},

		/**
		 * Get arrival time hint based on payment method
		 * @returns {String} Arrival hint text
		 */
		getArrivalHint() {
			if (this.paymentMethod === 'wallet') {
				return '退款将原路返回至微信钱包，通常5分钟内到账';
			} else if (this.paymentMethod === 'card') {
				return '退款将原路返回至银行卡，通常1-3个工作日到账';
			}
			return '退款将原路返回至原支付方式';
		}
	}
};
</script>

<style lang="scss" scoped>
.refund-status-display {
	background-color: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin: 20rpx 30rpx;
	box-shadow: 2rpx 2rpx 20rpx 0rpx rgba(202, 200, 200, 0.3);
}

/* Refund Header */
.refund-header {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid rgba(51, 51, 51, 0.1);
}

.refund-icon {
	font-size: 40rpx;
	color: #e36452;
	margin-right: 16rpx;
}

.refund-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
}

/* Refund Amount Section */
.refund-amount-section {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding: 20rpx;
	background-color: #f8f8f8;
	border-radius: 12rpx;
}

.amount-label {
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.7);
}

.amount-value {
	font-size: 40rpx;
	font-weight: bold;
	
	&.amount-processing {
		color: #409EFF;
	}
	
	&.amount-success {
		color: #67C23A;
	}
	
	&.amount-failed {
		color: #F56C6C;
	}
	
	&.amount-abnormal {
		color: #E6A23C;
	}
}

/* Refund Status Section */
.refund-status-section {
	margin-bottom: 30rpx;
}

.status-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.status-label {
	font-size: 28rpx;
	color: rgba(51, 51, 51, 0.7);
}

.status-badge {
	display: flex;
	align-items: center;
	padding: 8rpx 20rpx;
	border-radius: 30rpx;
	
	&.badge-processing {
		background-color: rgba(64, 158, 255, 0.1);
		
		.status-icon,
		.status-text {
			color: #409EFF;
		}
	}
	
	&.badge-success {
		background-color: rgba(103, 194, 58, 0.1);
		
		.status-icon,
		.status-text {
			color: #67C23A;
		}
	}
	
	&.badge-failed {
		background-color: rgba(245, 108, 108, 0.1);
		
		.status-icon,
		.status-text {
			color: #F56C6C;
		}
	}
	
	&.badge-abnormal {
		background-color: rgba(230, 162, 60, 0.1);
		
		.status-icon,
		.status-text {
			color: #E6A23C;
		}
	}
}

.status-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
	
	&.cuIcon-loading {
		animation: rotate 1s linear infinite;
	}
}

@keyframes rotate {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}

.status-text {
	font-size: 26rpx;
	font-weight: bold;
}

.status-description {
	padding: 16rpx 20rpx;
	background-color: #f8f8f8;
	border-radius: 8rpx;
}

.description-text {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.6);
	line-height: 1.6;
}

/* Arrival Section */
.arrival-section {
	margin-bottom: 30rpx;
	padding: 20rpx;
	background-color: #f0f9ff;
	border-radius: 12rpx;
	border-left: 4rpx solid #409EFF;
}

.arrival-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.arrival-label {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.7);
}

.arrival-value {
	font-size: 26rpx;
	color: #333333;
	font-weight: bold;
}

.arrival-hint {
	display: flex;
	align-items: flex-start;
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 2rpx solid rgba(64, 158, 255, 0.2);
}

.hint-icon {
	font-size: 28rpx;
	color: #409EFF;
	margin-right: 8rpx;
	margin-top: 2rpx;
}

.hint-text {
	flex: 1;
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.6);
	line-height: 1.6;
}

/* Failed Section */
.failed-section {
	padding: 20rpx;
	background-color: #fef0f0;
	border-radius: 12rpx;
	border-left: 4rpx solid #F56C6C;
}

.failed-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.failed-icon {
	font-size: 32rpx;
	color: #F56C6C;
	margin-right: 12rpx;
}

.failed-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #F56C6C;
}

.failed-reason {
	display: block;
	font-size: 26rpx;
	color: #333333;
	line-height: 1.6;
	margin-bottom: 12rpx;
}

.failed-contact {
	display: block;
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.6);
}

/* Abnormal Section */
.abnormal-section {
	padding: 20rpx;
	background-color: #fdf6ec;
	border-radius: 12rpx;
	border-left: 4rpx solid #E6A23C;
}

.abnormal-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.abnormal-icon {
	font-size: 32rpx;
	color: #E6A23C;
	margin-right: 12rpx;
}

.abnormal-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #E6A23C;
}

.abnormal-text {
	display: block;
	font-size: 26rpx;
	color: #333333;
	line-height: 1.6;
	margin-bottom: 12rpx;
}

.abnormal-contact {
	display: block;
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.6);
}
</style>
