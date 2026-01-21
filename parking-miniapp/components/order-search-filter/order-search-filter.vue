<template>
	<view class="order-search-filter">
		<!-- Filter Row -->
		<view class="filter-row">
			<!-- Date Range Picker -->
			<view class="filter-item date-filter-item" @click="toggleDatePicker">
				<text class="cuIcon-calendar filter-icon"></text>
				<text class="filter-label">{{ getDateRangeLabel() }}</text>
				<text class="cuIcon-unfold filter-arrow"></text>
			</view>

			<!-- Clear Filters Button -->
			<view class="clear-filters-btn" @click="clearFilters" v-if="hasActiveFilters">
				<text class="cuIcon-refresh"></text>
				<text class="clear-text">清除</text>
			</view>
		</view>

		<!-- Date Range Picker Popup -->
		<view class="picker-mask" v-if="showDatePicker" @click="closeDatePicker">
			<view class="picker-content date-picker-content" @click.stop>
				<view class="picker-header">
					<text class="picker-title">选择日期范围</text>
					<text class="picker-close cuIcon-close" @click="closeDatePicker"></text>
				</view>
				<view class="picker-body">
					<!-- 开始日期选择 -->
					<view class="date-input-row">
						<text class="date-label">开始日期</text>
						<picker mode="date" :value="tempStartDate" :end="tempEndDate || today" @change="onStartDateChange">
							<view class="date-input-box">
								<text class="date-input-text" :class="{ 'placeholder': !tempStartDate }">{{ tempStartDate || '请选择开始日期' }}</text>
								<text class="cuIcon-right"></text>
							</view>
						</picker>
					</view>
					
					<!-- 结束日期选择 -->
					<view class="date-input-row">
						<text class="date-label">结束日期</text>
						<picker mode="date" :value="tempEndDate" :start="tempStartDate" :end="today" @change="onEndDateChange">
							<view class="date-input-box">
								<text class="date-input-text" :class="{ 'placeholder': !tempEndDate }">{{ tempEndDate || '请选择结束日期' }}</text>
								<text class="cuIcon-right"></text>
							</view>
						</picker>
					</view>
					
					<!-- 快捷选择按钮 -->
					<view class="date-picker-actions">
						<view class="date-quick-btn" @click="selectQuickDate('today')">今天</view>
						<view class="date-quick-btn" @click="selectQuickDate('week')">最近7天</view>
						<view class="date-quick-btn" @click="selectQuickDate('month')">最近30天</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'OrderSearchFilter',
	props: {
		// Initial filter values
		initialFilters: {
			type: Object,
			default: () => ({})
		}
	},
	data() {
		return {
			// Search query
			searchQuery: '',
			
			// Status filter
			selectedStatus: '',
			
			// Date range filter
			dateRange: [],
			showDatePicker: false,
			
			// Temp date values for picker
			tempStartDate: '',
			tempEndDate: '',
			
			// Today's date
			today: ''
		};
	},
	computed: {
		/**
		 * Check if any filters are active
		 * @returns {Boolean}
		 */
		hasActiveFilters() {
			return this.searchQuery !== '' || 
				   this.selectedStatus !== '' || 
				   (this.dateRange && this.dateRange.length === 2);
		}
	},
	mounted() {
		// Set today's date
		this.today = this.formatDate(new Date());
		
		// Initialize with initial filters if provided
		if (this.initialFilters) {
			this.searchQuery = this.initialFilters.searchQuery || '';
			this.selectedStatus = this.initialFilters.status || '';
			this.dateRange = this.initialFilters.dateRange || [];
			
			// Initialize temp dates
			if (this.dateRange && this.dateRange.length === 2) {
				this.tempStartDate = this.dateRange[0];
				this.tempEndDate = this.dateRange[1];
			}
		}
	},
	methods: {
		/**
		 * Toggle date picker visibility
		 */
		toggleDatePicker() {
			this.showDatePicker = !this.showDatePicker;
			
			// Initialize temp dates when opening
			if (this.showDatePicker) {
				if (this.dateRange && this.dateRange.length === 2) {
					this.tempStartDate = this.dateRange[0];
					this.tempEndDate = this.dateRange[1];
				} else {
					this.tempStartDate = '';
					this.tempEndDate = '';
				}
			}
		},
		
		/**
		 * Close date picker
		 */
		closeDatePicker() {
			this.showDatePicker = false;
		},

		/**
		 * Handle start date change - auto apply when both dates selected
		 * @param {Object} e - Event object
		 */
		onStartDateChange(e) {
			this.tempStartDate = e.detail.value;
			console.log('Start date selected:', this.tempStartDate);
			
			// Auto apply when both dates are selected
			if (this.tempStartDate && this.tempEndDate) {
				this.applyDateRange();
			}
		},
		
		/**
		 * Handle end date change - auto apply when both dates selected
		 * @param {Object} e - Event object
		 */
		onEndDateChange(e) {
			this.tempEndDate = e.detail.value;
			console.log('End date selected:', this.tempEndDate);
			
			// Auto apply when both dates are selected
			if (this.tempStartDate && this.tempEndDate) {
				this.applyDateRange();
			}
		},
		
		/**
		 * Apply date range and trigger filter
		 */
		applyDateRange() {
			this.dateRange = [this.tempStartDate, this.tempEndDate];
			this.showDatePicker = false;
			
			// Show toast feedback
			uni.showToast({
				title: '筛选中...',
				icon: 'loading',
				duration: 500
			});
			
			this.applyFilters();
		},

		/**
		 * Select quick date range
		 * @param {String} type - Quick date type (today, week, month)
		 */
		selectQuickDate(type) {
			const today = new Date();
			const endDate = this.formatDate(today);
			let startDate;

			switch (type) {
				case 'today':
					startDate = endDate;
					break;
				case 'week':
					const weekAgo = new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000);
					startDate = this.formatDate(weekAgo);
					break;
				case 'month':
					const monthAgo = new Date(today.getTime() - 29 * 24 * 60 * 60 * 1000);
					startDate = this.formatDate(monthAgo);
					break;
			}

			this.tempStartDate = startDate;
			this.tempEndDate = endDate;
			this.dateRange = [startDate, endDate];
			this.showDatePicker = false;
			
			// Show toast feedback
			uni.showToast({
				title: '筛选中...',
				icon: 'loading',
				duration: 500
			});
			
			this.applyFilters();
		},

		/**
		 * Format date to YYYY-MM-DD
		 * @param {Date} date - Date object
		 * @returns {String} Formatted date string
		 */
		formatDate(date) {
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			return `${year}-${month}-${day}`;
		},

		/**
		 * Get date range label for display
		 * @returns {String} Date range label
		 */
		getDateRangeLabel() {
			if (!this.dateRange || this.dateRange.length !== 2) {
				return '选择日期';
			}
			// 使用更紧凑的格式：去掉年份前缀，用短横线连接
			const start = this.dateRange[0].substring(5); // MM-DD
			const end = this.dateRange[1].substring(5);   // MM-DD
			return `${start} 至 ${end}`;
		},

		/**
		 * Apply filters and emit change event
		 * Emits filter change event to parent component
		 */
		applyFilters() {
			const filters = {
				searchQuery: this.searchQuery.trim(),
				status: this.selectedStatus,
				dateRange: this.dateRange && this.dateRange.length === 2 ? this.dateRange : null,
				startDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] : null,
				endDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[1] : null
			};

			console.log('Applying filters:', filters);
			
			// Emit filter change event to parent component
			this.$emit('filter-change', filters);
		},

		/**
		 * Clear all filters
		 * Resets all filter values and emits change event
		 */
		clearFilters() {
			this.searchQuery = '';
			this.selectedStatus = '';
			this.dateRange = [];
			this.tempStartDate = '';
			this.tempEndDate = '';
			
			// Show toast feedback
			uni.showToast({
				title: '已清除筛选',
				icon: 'success',
				duration: 1000
			});
			
			this.applyFilters();
		}
	}
};
</script>

<style lang="scss" scoped>
.order-search-filter {
	background-color: #ffffff;
	padding: 20rpx 30rpx;
}

/* Filter Row */
.filter-row {
	display: flex;
	align-items: center;
	flex-wrap: nowrap;
}

.filter-item {
	display: flex;
	align-items: center;
	padding: 16rpx 20rpx;
	background-color: #f5f5f5;
	border-radius: 30rpx;
	margin-right: 16rpx;
	flex-shrink: 0;
}

.date-filter-item {
	flex: 1;
	min-width: 0;
}

.filter-icon {
	font-size: 26rpx;
	color: #e36452;
	margin-right: 8rpx;
	flex-shrink: 0;
}

.filter-label {
	flex: 1;
	font-size: 24rpx;
	color: #333333;
	margin-right: 6rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.filter-arrow {
	font-size: 22rpx;
	color: rgba(51, 51, 51, 0.5);
	flex-shrink: 0;
}

.clear-filters-btn {
	display: flex;
	align-items: center;
	padding: 16rpx 20rpx;
	background-color: #e36452;
	border-radius: 30rpx;
	flex-shrink: 0;
	
	text {
		color: #ffffff;
		font-size: 24rpx;
	}
	
	.clear-text {
		margin-left: 6rpx;
	}
}

/* Picker Mask and Content */
.picker-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: flex-end;
	justify-content: center;
}

.picker-content {
	width: 100%;
	max-height: 70vh;
	background-color: #ffffff;
	border-radius: 30rpx 30rpx 0 0;
	overflow: hidden;
	animation: slideUp 0.3s ease-out;
}

.date-picker-content {
	max-height: 80vh;
}

@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
}

.picker-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.picker-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
}

.picker-close {
	font-size: 36rpx;
	color: rgba(51, 51, 51, 0.5);
	padding: 10rpx;
}

.picker-body {
	max-height: 60vh;
	overflow-y: auto;
	padding-bottom: 40rpx;
}

/* Date Input Rows */
.date-input-row {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.date-label {
	width: 160rpx;
	font-size: 28rpx;
	color: #333333;
	font-weight: 500;
}

.date-input-box {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 24rpx;
	background-color: #f8f8f8;
	border-radius: 12rpx;
	border: 2rpx solid #eee;
}

.date-input-text {
	font-size: 28rpx;
	color: #333333;
	
	&.placeholder {
		color: rgba(51, 51, 51, 0.4);
	}
}

/* Date Picker Actions */
.date-picker-actions {
	display: flex;
	justify-content: space-around;
	padding: 30rpx 30rpx;
	margin-top: 20rpx;
}

.date-quick-btn {
	padding: 20rpx 40rpx;
	background-color: #fff5f4;
	border: 2rpx solid #e36452;
	border-radius: 30rpx;
	font-size: 26rpx;
	color: #e36452;
	
	&:active {
		background-color: #e36452;
		color: #ffffff;
	}
}
</style>
