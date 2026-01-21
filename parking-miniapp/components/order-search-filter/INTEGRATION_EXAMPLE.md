# OrderSearchFilter Integration Example

This document provides a complete example of integrating the `OrderSearchFilter` component into an order history page.

## Complete Order History Page Example

```vue
<template>
  <view class="order-history-page">
    <!-- Navigation Bar -->
    <view class="nav-bar">
      <text class="nav-title">订单历史</text>
    </view>

    <!-- Search and Filter Component -->
    <order-search-filter 
      :initial-filters="filters"
      @filter-change="handleFilterChange"
    />

    <!-- Order List -->
    <view class="order-list">
      <!-- Loading State -->
      <view class="loading-state" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- Order Items -->
      <view v-else-if="filteredOrders.length > 0">
        <order-list-item
          v-for="order in filteredOrders"
          :key="order.orderId"
          :order="order"
          :show-refund-info="true"
          @click="navigateToDetail(order)"
        />
      </view>

      <!-- Empty State -->
      <view class="empty-state" v-else>
        <image 
          class="empty-image" 
          src="/static/images/empty-orders.png"
          mode="aspectFit"
        />
        <text class="empty-text">暂无订单</text>
        <view class="clear-filters-hint" v-if="hasActiveFilters">
          <text class="hint-text">试试清除筛选条件</text>
        </view>
      </view>
    </view>

    <!-- Pagination -->
    <view class="pagination" v-if="totalPages > 1">
      <view 
        class="page-btn" 
        :class="{ 'disabled': currentPage === 1 }"
        @click="previousPage"
      >
        <text>上一页</text>
      </view>
      <text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
      <view 
        class="page-btn"
        :class="{ 'disabled': currentPage === totalPages }"
        @click="nextPage"
      >
        <text>下一页</text>
      </view>
    </view>
  </view>
</template>

<script>
import OrderSearchFilter from '@/components/order-search-filter/order-search-filter.vue';
import OrderListItem from '@/components/order-list-item/order-list-item.vue';

export default {
  name: 'OrderHistoryPage',
  components: {
    OrderSearchFilter,
    OrderListItem
  },
  data() {
    return {
      // Filter state
      filters: {
        searchQuery: '',
        status: '',
        dateRange: []
      },
      
      // Order data
      orders: [],
      filteredOrders: [],
      
      // Pagination
      currentPage: 1,
      pageSize: 20,
      totalPages: 1,
      totalCount: 0,
      
      // Loading state
      loading: false
    };
  },
  computed: {
    /**
     * Check if any filters are active
     */
    hasActiveFilters() {
      return this.filters.searchQuery !== '' || 
             this.filters.status !== '' || 
             (this.filters.dateRange && this.filters.dateRange.length === 2);
    }
  },
  onLoad() {
    // Load initial orders
    this.loadOrders();
  },
  onPullDownRefresh() {
    // Refresh orders on pull down
    this.currentPage = 1;
    this.loadOrders().then(() => {
      uni.stopPullDownRefresh();
    });
  },
  onReachBottom() {
    // Load more orders on reach bottom
    if (this.currentPage < this.totalPages && !this.loading) {
      this.currentPage++;
      this.loadOrders(true);
    }
  },
  methods: {
    /**
     * Handle filter change event
     * @param {Object} filters - Filter values
     */
    handleFilterChange(filters) {
      console.log('Filters changed:', filters);
      
      // Update filter state
      this.filters = filters;
      
      // Reset to first page
      this.currentPage = 1;
      
      // Reload orders with new filters
      this.loadOrders();
    },

    /**
     * Load orders from API
     * @param {Boolean} append - Whether to append to existing orders
     */
    async loadOrders(append = false) {
      if (this.loading) return;
      
      this.loading = true;
      
      try {
        // Build query parameters
        const params = {
          userId: this.getUserId(),
          page: this.currentPage,
          pageSize: this.pageSize
        };
        
        // Add filters if active
        if (this.filters.searchQuery) {
          params.searchQuery = this.filters.searchQuery;
        }
        if (this.filters.status) {
          params.status = this.filters.status;
        }
        if (this.filters.startDate) {
          params.startDate = this.filters.startDate;
        }
        if (this.filters.endDate) {
          params.endDate = this.filters.endDate;
        }
        
        // Call API
        const response = await this.$http.get('/api/order/list', params);
        
        if (response.success) {
          // Update orders
          if (append) {
            this.filteredOrders = [...this.filteredOrders, ...response.orders];
          } else {
            this.filteredOrders = response.orders;
          }
          
          // Update pagination
          this.totalCount = response.total;
          this.totalPages = Math.ceil(response.total / this.pageSize);
        } else {
          uni.showToast({
            title: response.message || '加载失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('Load orders error:', error);
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },

    /**
     * Get current user ID
     * @returns {String} User ID
     */
    getUserId() {
      // Get user ID from store or storage
      return uni.getStorageSync('userId') || '';
    },

    /**
     * Navigate to order detail page
     * @param {Object} order - Order object
     */
    navigateToDetail(order) {
      uni.navigateTo({
        url: `/package/chargingPile/orderFormDetail?orderId=${order.orderId}`
      });
    },

    /**
     * Go to previous page
     */
    previousPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.loadOrders();
        
        // Scroll to top
        uni.pageScrollTo({
          scrollTop: 0,
          duration: 300
        });
      }
    },

    /**
     * Go to next page
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.loadOrders();
        
        // Scroll to top
        uni.pageScrollTo({
          scrollTop: 0,
          duration: 300
        });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.order-history-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.nav-bar {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.order-list {
  padding: 30rpx;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: rgba(51, 51, 51, 0.5);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.empty-image {
  width: 300rpx;
  height: 300rpx;
  margin-bottom: 40rpx;
}

.empty-text {
  font-size: 28rpx;
  color: rgba(51, 51, 51, 0.5);
  margin-bottom: 20rpx;
}

.clear-filters-hint {
  margin-top: 20rpx;
}

.hint-text {
  font-size: 26rpx;
  color: #409EFF;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 30rpx;
  background-color: #ffffff;
}

.page-btn {
  padding: 16rpx 32rpx;
  background-color: #409EFF;
  border-radius: 30rpx;
  margin: 0 20rpx;
  
  text {
    font-size: 26rpx;
    color: #ffffff;
  }
  
  &.disabled {
    background-color: #DCDFE6;
    opacity: 0.6;
  }
}

.page-info {
  font-size: 28rpx;
  color: #333333;
  margin: 0 20rpx;
}
</style>
```

## API Integration

### Backend API Endpoint

The example assumes the following API endpoint structure:

**GET /api/order/list**

Query Parameters:
- `userId` (required): User ID
- `page` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)
- `searchQuery` (optional): Search text
- `status` (optional): Order status filter
- `startDate` (optional): Start date (YYYY-MM-DD)
- `endDate` (optional): End date (YYYY-MM-DD)

Response:
```json
{
  "success": true,
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "orders": [
    {
      "orderId": "ORDER123",
      "chargingStationName": "Station A",
      "prepaymentAmount": 50.00,
      "actualConsumption": 48.00,
      "refundAmount": 2.00,
      "status": "refunded",
      "refundStatus": "success",
      "createdAt": "2026-01-01T10:00:00Z"
    }
  ]
}
```

## Key Features Demonstrated

1. **Filter Integration**: The component receives filter changes and applies them to the order list
2. **Pagination**: Supports both page-based and infinite scroll pagination
3. **Loading States**: Shows loading indicator while fetching data
4. **Empty States**: Displays appropriate message when no orders match filters
5. **Pull to Refresh**: Supports pull-down refresh gesture
6. **Error Handling**: Gracefully handles API errors with user feedback

## Testing the Integration

1. **Test Search**: Enter order numbers or station names in the search box
2. **Test Status Filter**: Select different statuses and verify filtering
3. **Test Date Range**: Select date ranges and verify filtering
4. **Test Multiple Filters**: Apply multiple filters simultaneously
5. **Test Clear Filters**: Click clear button and verify all filters reset
6. **Test Pagination**: Navigate through pages and verify data loads correctly
7. **Test Empty State**: Apply filters that return no results

## Performance Considerations

- Search input is debounced (500ms) to reduce API calls
- Orders are paginated to avoid loading too much data at once
- Component state is managed efficiently to prevent unnecessary re-renders
- API calls are cancelled if component is destroyed before completion

## Accessibility

- Clear visual feedback for active filters
- Loading states prevent user confusion
- Empty states provide helpful guidance
- Error messages are user-friendly

## Next Steps

After integrating the component:

1. Test with real API endpoints
2. Adjust styling to match your design system
3. Add analytics tracking for filter usage
4. Consider adding filter presets for common searches
5. Implement filter state persistence across sessions
