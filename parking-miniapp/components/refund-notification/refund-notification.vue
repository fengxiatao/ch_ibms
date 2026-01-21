<template>
	<view 
		class="refund-notification" 
		:class="getNotificationClass()"
		@click="handleClick"
		v-if="visible"
	>
		<!-- Notification Content -->
		<view class="notification-content">
			<!-- Icon -->
			<view class="notification-icon">
				<text class="icon" :class="getIconClass()"></text>
			</view>

			<!-- Message Content -->
			<view class="notification-message">
				<!-- Title -->
				<text class="message-title">{{ getTitle() }}</text>
				
				<!-- Amount (if applicable) -->
				<text class="message-amount" v-if="amount">
					退款金额：¥{{ formatAmount(amount) }}
				</text>
				
				<!-- Custom Message -->
				<text class="message-text" v-if="message">{{ message }}</text>
				
				<!-- Refund ID -->
				<text class="message-id" v-if="showRefundId">
					退款单号：{{ refundId }}
				</text>
			</view>

			<!-- Dismiss Button -->
			<view class="notification-dismiss" @click.stop="handleDismiss">
				<text class="dismiss-icon cuIcon-close"></text>
			</view>
		</view>

		<!-- Action Hint -->
		<view class="notification-action" v-if="showActionHint">
			<text class="action-text">点击查看详情</text>
			<text class="action-arrow cuIcon-right"></text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'RefundNotification',
	props: {
		// Refund ID
		refundId: {
			type: String,
			required: true,
			default: ''
		},
		// Refund amount in yuan
		amount: {
			type: [Number, String],
			default: 0
		},
		// Refund status: 'success', 'failed', 'processing', 'abnormal'
		status: {
			type: String,
			required: true,
			default: 'success',
			validator: (value) => {
				return ['success', 'failed', 'processing', 'abnormal'].includes(value);
			}
		},
		// Custom message
		message: {
			type: String,
			default: ''
		},
		// Order ID for navigation
		orderId: {
			type: String,
			default: ''
		},
		// Show refund ID in notification
		showRefundId: {
			type: Boolean,
			default: false
		},
		// Show action hint
		showActionHint: {
			type: Boolean,
			default: true
		},
		// Auto dismiss after duration (ms), 0 = no auto dismiss
		autoDismiss: {
			type: Number,
			default: 5000
		}
	},
	data() {
		return {
			visible: true,
			dismissTimer: null
		};
	},
	mounted() {
		// Set up auto dismiss if enabled
		if (this.autoDismiss > 0) {
			this.dismissTimer = setTimeout(() => {
				this.handleDismiss();
			}, this.autoDismiss);
		}
	},
	beforeDestroy() {
		// Clear timer on component destroy
		if (this.dismissTimer) {
			clearTimeout(this.dismissTimer);
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
		 * Get notification CSS class based on status
		 * @returns {String} CSS class name
		 */
		getNotificationClass() {
			const classMap = {
				'success': 'notification-success',
				'failed': 'notification-failed',
				'processing': 'notification-processing',
				'abnormal': 'notification-abnormal'
			};
			return classMap[this.status] || '';
		},

		/**
		 * Get icon CSS class based on status
		 * @returns {String} Icon class name
		 */
		getIconClass() {
			const iconMap = {
				'success': 'cuIcon-roundcheckfill',
				'failed': 'cuIcon-roundclosefill',
				'processing': 'cuIcon-loading',
				'abnormal': 'cuIcon-warnfill'
			};
			return iconMap[this.status] || '';
		},

		/**
		 * Get notification title based on status
		 * @returns {String} Title text
		 */
		getTitle() {
			const titleMap = {
				'success': '退款成功',
				'failed': '退款失败',
				'processing': '退款处理中',
				'abnormal': '退款异常'
			};
			return titleMap[this.status] || '退款通知';
		},

		/**
		 * Handle notification click - navigate to order detail
		 */
		handleClick() {
			// Emit click event
			this.$emit('click', {
				refundId: this.refundId,
				orderId: this.orderId,
				status: this.status
			});

			// Navigate to order detail page if orderId is provided
			if (this.orderId) {
				uni.navigateTo({
					url: `/package/chargingPile/orderFormDetail?item=${encodeURIComponent(JSON.stringify({
						order: this.orderId
					}))}`
				});
			}
		},

		/**
		 * Handle dismiss button click
		 */
		handleDismiss() {
			// Clear auto dismiss timer
			if (this.dismissTimer) {
				clearTimeout(this.dismissTimer);
				this.dismissTimer = null;
			}

			// Emit dismiss event
			this.$emit('dismiss', {
				refundId: this.refundId,
				status: this.status
			});

			// Hide notification with animation
			this.visible = false;
		}
	}
};
</script>

<style lang="scss" scoped>
.refund-notification {
	position: fixed;
	top: 20rpx;
	left: 30rpx;
	right: 30rpx;
	background-color: #ffffff;
	border-radius: 16rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
	z-index: 9999;
	animation: slideDown 0.3s ease-out;
	overflow: hidden;
	
	// Add safe area padding for devices with notch
	top: calc(20rpx + env(safe-area-inset-top));
}

@keyframes slideDown {
	from {
		transform: translateY(-100%);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

/* Notification Content */
.notification-content {
	display: flex;
	align-items: flex-start;
	padding: 24rpx;
}

.notification-icon {
	flex-shrink: 0;
	margin-right: 20rpx;
}

.icon {
	font-size: 48rpx;
	
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

.notification-message {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.message-title {
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 8rpx;
	line-height: 1.4;
}

.message-amount {
	font-size: 28rpx;
	font-weight: bold;
	margin-bottom: 8rpx;
	line-height: 1.4;
}

.message-text {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.7);
	margin-bottom: 8rpx;
	line-height: 1.5;
}

.message-id {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 1.4;
}

.notification-dismiss {
	flex-shrink: 0;
	margin-left: 16rpx;
	padding: 8rpx;
}

.dismiss-icon {
	font-size: 32rpx;
	color: rgba(51, 51, 51, 0.5);
}

/* Action Hint */
.notification-action {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 16rpx 24rpx;
	border-top: 2rpx solid rgba(51, 51, 51, 0.1);
}

.action-text {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.6);
	margin-right: 8rpx;
}

.action-arrow {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.6);
}

/* Status-specific Styles */
.notification-success {
	border-left: 8rpx solid #67C23A;
	
	.icon {
		color: #67C23A;
	}
	
	.message-title {
		color: #67C23A;
	}
	
	.message-amount {
		color: #67C23A;
	}
}

.notification-failed {
	border-left: 8rpx solid #F56C6C;
	
	.icon {
		color: #F56C6C;
	}
	
	.message-title {
		color: #F56C6C;
	}
	
	.message-amount {
		color: #F56C6C;
	}
}

.notification-processing {
	border-left: 8rpx solid #409EFF;
	
	.icon {
		color: #409EFF;
	}
	
	.message-title {
		color: #409EFF;
	}
	
	.message-amount {
		color: #409EFF;
	}
}

.notification-abnormal {
	border-left: 8rpx solid #E6A23C;
	
	.icon {
		color: #E6A23C;
	}
	
	.message-title {
		color: #E6A23C;
	}
	
	.message-amount {
		color: #E6A23C;
	}
}
</style>
