<template>
	<view class="order-timeline">
		<view class="timeline-title">订单进度</view>
		<view class="timeline-container">
			<view 
				v-for="(stage, index) in stages" 
				:key="index"
				class="timeline-stage"
				:class="stage.stageClass"
			>
				<!-- Timeline Node -->
				<view class="timeline-node">
					<view class="node-icon" :class="stage.nodeIconClass">
						<text v-if="stage.status === 'completed'" class="icon-check">✓</text>
						<text v-else-if="stage.status === 'in-progress'" class="icon-loading">●</text>
						<text v-else class="icon-pending">○</text>
					</view>
					<view v-if="index < stages.length - 1" class="node-line" :class="stage.lineClass"></view>
				</view>
				
				<!-- Timeline Content -->
				<view class="timeline-content">
					<view class="stage-name" :class="stage.stageNameClass">
						{{ stage.name }}
					</view>
					<view v-if="stage.timestamp" class="stage-time">
						{{ formatStageTime(stage.timestamp) }}
					</view>
					<view v-if="stage.description" class="stage-description">
						{{ stage.description }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'OrderTimeline',
	props: {
		// Order data object
		order: {
			type: Object,
			required: true,
			default: () => ({})
		}
	},
	computed: {
		/**
		 * Generate timeline stages based on order data
		 * Stages: created → paid → charging → completed → refunded
		 * @returns {Array} Array of stage objects with pre-computed class names
		 */
		stages() {
			const order = this.order;
			const stages = [];
			
			// Stage 1: Created
			const createdStatus = this.getStageStatus('created');
			stages.push({
				name: '订单创建',
				status: createdStatus,
				timestamp: order.createdAt,
				description: order.status === 'created' ? '等待支付' : null,
				stageClass: `stage-${createdStatus}`,
				nodeIconClass: `node-${createdStatus}`,
				lineClass: createdStatus === 'completed' ? 'line-completed' : 'line-pending',
				stageNameClass: `name-${createdStatus}`
			});
			
			// Stage 2: Paid
			const paidStatus = this.getStageStatus('paid');
			stages.push({
				name: '支付完成',
				status: paidStatus,
				timestamp: order.paymentTime,
				description: order.status === 'paid' ? '等待开始充电' : null,
				stageClass: `stage-${paidStatus}`,
				nodeIconClass: `node-${paidStatus}`,
				lineClass: paidStatus === 'completed' ? 'line-completed' : 'line-pending',
				stageNameClass: `name-${paidStatus}`
			});
			
			// Stage 3: Charging
			const chargingStatus = this.getStageStatus('charging');
			stages.push({
				name: '充电中',
				status: chargingStatus,
				timestamp: order.chargingStartTime,
				description: order.status === 'charging' ? '正在充电...' : null,
				stageClass: `stage-${chargingStatus}`,
				nodeIconClass: `node-${chargingStatus}`,
				lineClass: chargingStatus === 'completed' ? 'line-completed' : 'line-pending',
				stageNameClass: `name-${chargingStatus}`
			});
			
			// Stage 4: Completed
			const completedStatus = this.getStageStatus('completed');
			stages.push({
				name: '充电完成',
				status: completedStatus,
				timestamp: order.chargingEndTime,
				description: order.status === 'completed' && order.refundAmount > 0 ? '处理退款中' : null,
				stageClass: `stage-${completedStatus}`,
				nodeIconClass: `node-${completedStatus}`,
				lineClass: completedStatus === 'completed' ? 'line-completed' : 'line-pending',
				stageNameClass: `name-${completedStatus}`
			});
			
			// Stage 5: Refunded (only if refund exists)
			if (order.refundAmount && order.refundAmount > 0) {
				const refundedStatus = this.getStageStatus('refunded');
				stages.push({
					name: '退款完成',
					status: refundedStatus,
					timestamp: order.refundTime,
					description: this.getRefundDescription(),
					stageClass: `stage-${refundedStatus}`,
					nodeIconClass: `node-${refundedStatus}`,
					lineClass: refundedStatus === 'completed' ? 'line-completed' : 'line-pending',
					stageNameClass: `name-${refundedStatus}`
				});
			}
			
			// Handle cancelled status
			if (order.status === 'cancelled') {
				stages.push({
					name: '订单取消',
					status: 'completed',
					timestamp: order.updatedAt,
					description: '订单已取消，退款处理中',
					stageClass: 'stage-completed',
					nodeIconClass: 'node-completed',
					lineClass: 'line-completed',
					stageNameClass: 'name-completed'
				});
			}
			
			return stages;
		}
	},
	methods: {
		/**
		 * Get stage status based on order status
		 * @param {String} stageName - Stage name
		 * @returns {String} Status: 'completed', 'in-progress', 'pending'
		 */
		getStageStatus(stageName) {
			const order = this.order;
			const currentStatus = order.status;
			
			const statusOrder = ['created', 'paid', 'charging', 'completed', 'refunded', 'cancelled'];
			const currentIndex = statusOrder.indexOf(currentStatus);
			const stageIndex = statusOrder.indexOf(stageName);
			
			if (stageIndex < currentIndex) {
				return 'completed';
			} else if (stageIndex === currentIndex) {
				return 'in-progress';
			} else {
				return 'pending';
			}
		},
		
		/**
		 * Get refund description based on refund status
		 * @returns {String} Refund description
		 */
		getRefundDescription() {
			const order = this.order;
			if (!order.refundStatus) {
				return null;
			}
			
			const statusMap = {
				'processing': '退款处理中',
				'success': `已退款 ¥${this.formatAmount(order.refundAmount)}`,
				'failed': '退款失败',
				'abnormal': '退款异常，请联系客服'
			};
			
			return statusMap[order.refundStatus] || null;
		},
		
		/**
		 * Format amount to 2 decimal places
		 * @param {Number} amount - Amount to format
		 * @returns {String} Formatted amount
		 */
		formatAmount(amount) {
			if (amount === undefined || amount === null) {
				return '0.00';
			}
			return parseFloat(amount).toFixed(2);
		},
		
		/**
		 * Format stage timestamp for display
		 * @param {String} timestamp - Timestamp string
		 * @returns {String} Formatted time
		 */
		formatStageTime(timestamp) {
			if (!timestamp) {
				return '';
			}
			// Format: MM-DD HH:mm
			const date = new Date(timestamp);
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const hours = String(date.getHours()).padStart(2, '0');
			const minutes = String(date.getMinutes()).padStart(2, '0');
			return `${month}-${day} ${hours}:${minutes}`;
		},
		
	}
};
</script>

<style lang="scss" scoped>
.order-timeline {
	padding: 30rpx;
	background-color: #ffffff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
}

.timeline-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
	margin-bottom: 30rpx;
	line-height: 42rpx;
}

.timeline-container {
	position: relative;
}

.timeline-stage {
	display: flex;
	align-items: flex-start;
	position: relative;
	min-height: 80rpx;
	
	&:last-child {
		min-height: auto;
		
		.node-line {
			display: none;
		}
	}
}

.timeline-node {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.node-icon {
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: bold;
	position: relative;
	z-index: 2;
	
	&.node-completed {
		background-color: #67C23A;
		color: #ffffff;
		border: 4rpx solid #67C23A;
	}
	
	&.node-in-progress {
		background-color: #409EFF;
		color: #ffffff;
		border: 4rpx solid #409EFF;
		animation: pulse 1.5s ease-in-out infinite;
	}
	
	&.node-pending {
		background-color: #ffffff;
		color: #DCDFE6;
		border: 4rpx solid #DCDFE6;
	}
}

@keyframes pulse {
	0%, 100% {
		transform: scale(1);
		opacity: 1;
	}
	50% {
		transform: scale(1.1);
		opacity: 0.8;
	}
}

.icon-check {
	font-size: 24rpx;
}

.icon-loading {
	font-size: 16rpx;
	animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
	0%, 100% {
		opacity: 1;
	}
	50% {
		opacity: 0.3;
	}
}

.icon-pending {
	font-size: 20rpx;
}

.node-line {
	width: 4rpx;
	flex: 1;
	min-height: 60rpx;
	margin-top: 8rpx;
	
	&.line-completed {
		background-color: #67C23A;
	}
	
	&.line-pending {
		background-color: #DCDFE6;
	}
}

.timeline-content {
	flex: 1;
	padding-top: 8rpx;
	padding-bottom: 30rpx;
}

.stage-name {
	font-size: 28rpx;
	line-height: 36rpx;
	margin-bottom: 8rpx;
	
	&.name-completed {
		color: #333333;
		font-weight: bold;
	}
	
	&.name-in-progress {
		color: #409EFF;
		font-weight: bold;
	}
	
	&.name-pending {
		color: rgba(51, 51, 51, 0.3);
	}
}

.stage-time {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
	margin-bottom: 6rpx;
}

.stage-description {
	font-size: 24rpx;
	color: rgba(51, 51, 51, 0.5);
	line-height: 32rpx;
	margin-top: 6rpx;
}
</style>
