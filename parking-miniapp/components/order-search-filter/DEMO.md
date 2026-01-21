# OrderSearchFilter Component Demo

## Quick Demo Page

To test the OrderSearchFilter component, you can create a simple demo page:

```vue
<template>
  <view class="demo-page">
    <view class="demo-header">
      <text class="demo-title">OrderSearchFilter Demo</text>
    </view>

    <!-- Component Demo -->
    <order-search-filter 
      @filter-change="handleFilterChange"
    />

    <!-- Filter Output Display -->
    <view class="filter-output">
      <view class="output-header">
        <text class="output-title">Current Filters:</text>
      </view>
      <view class="output-content">
        <view class="output-row">
          <text class="output-label">Search Query:</text>
          <text class="output-value">{{ currentFilters.searchQuery || '(empty)' }}</text>
        </view>
        <view class="output-row">
          <text class="output-label">Status:</text>
          <text class="output-value">{{ currentFilters.status || '(all)' }}</text>
        </view>
        <view class="output-row">
          <text class="output-label">Date Range:</text>
          <text class="output-value">
            {{ currentFilters.dateRange && currentFilters.dateRange.length === 2 
               ? `${currentFilters.dateRange[0]} to ${currentFilters.dateRange[1]}` 
               : '(none)' }}
          </text>
        </view>
      </view>
    </view>

    <!-- Sample Order List -->
    <view class="sample-orders">
      <view class="sample-header">
        <text class="sample-title">Sample Orders ({{ filteredSampleOrders.length }})</text>
      </view>
      <view class="sample-list">
        <view 
          class="sample-order" 
          v-for="order in filteredSampleOrders" 
          :key="order.orderId"
        >
          <text class="order-id">{{ order.orderId }}</text>
          <text class="order-station">{{ order.chargingStationName }}</text>
          <text class="order-status">{{ order.status }}</text>
          <text class="order-date">{{ order.createdAt }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import OrderSearchFilter from './order-search-filter.vue';

export default {
  name: 'OrderSearchFilterDemo',
  components: {
    OrderSearchFilter
  },
  data() {
    return {
      currentFilters: {
        searchQuery: '',
        status: '',
        dateRange: null
      },
      sampleOrders: [
        {
          orderId: 'ORDER001',
          chargingStationName: '广州邮通科技园东信科技大厦充电桩',
          status: 'completed',
          createdAt: '2026-01-01'
        },
        {
          orderId: 'ORDER002',
          chargingStationName: '广州邮通科技园东信科技大厦充电桩',
          status: 'refunded',
          createdAt: '2026-01-02'
        },
        {
          orderId: 'ORDER003',
          chargingStationName: '广州邮通科技园东信科技大厦充电桩',
          status: 'charging',
          createdAt: '2026-01-03'
        },
        {
          orderId: 'ORDER004',
          chargingStationName: '广州邮通科技园东信科技大厦充电桩',
          status: 'paid',
          createdAt: '2025-12-28'
        },
        {
          orderId: 'ORDER005',
          chargingStationName: '广州邮通科技园东信科技大厦充电桩',
          status: 'completed',
          createdAt: '2025-12-25'
        }
      ]
    };
  },
  computed: {
    filteredSampleOrders() {
      let filtered = [...this.sampleOrders];

      // Apply search filter
      if (this.currentFilters.searchQuery) {
        const query = this.currentFilters.searchQuery.toLowerCase();
        filtered = filtered.filter(order => 
          order.orderId.toLowerCase().includes(query) ||
          order.chargingStationName.toLowerCase().includes(query)
        );
      }

      // Apply status filter
      if (this.currentFilters.status) {
        filtered = filtered.filter(order => 
          order.status === this.currentFilters.status
        );
      }

      // Apply date range filter
      if (this.currentFilters.startDate && this.currentFilters.endDate) {
        filtered = filtered.filter(order => {
          const orderDate = order.createdAt;
          return orderDate >= this.currentFilters.startDate && 
                 orderDate <= this.currentFilters.endDate;
        });
      }

      return filtered;
    }
  },
  methods: {
    handleFilterChange(filters) {
      console.log('Filter changed:', filters);
      this.currentFilters = filters;
    }
  }
};
</script>

<style lang="scss" scoped>
.demo-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.demo-header {
  background-color: #409EFF;
  padding: 40rpx 30rpx;
}

.demo-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

.filter-output {
  margin: 30rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
}

.output-header {
  margin-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
  padding-bottom: 20rpx;
}

.output-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.output-content {
  display: flex;
  flex-direction: column;
}

.output-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.output-label {
  font-size: 26rpx;
  color: rgba(51, 51, 51, 0.5);
}

.output-value {
  font-size: 26rpx;
  color: #333333;
  font-weight: bold;
  max-width: 400rpx;
  text-align: right;
  word-break: break-all;
}

.sample-orders {
  margin: 30rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
}

.sample-header {
  margin-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
  padding-bottom: 20rpx;
}

.sample-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.sample-list {
  display: flex;
  flex-direction: column;
}

.sample-order {
  display: flex;
  flex-direction: column;
  padding: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.order-id {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.order-station {
  font-size: 26rpx;
  color: rgba(51, 51, 51, 0.7);
  margin-bottom: 8rpx;
}

.order-status {
  font-size: 24rpx;
  color: #409EFF;
  margin-bottom: 8rpx;
}

.order-date {
  font-size: 24rpx;
  color: rgba(51, 51, 51, 0.5);
}
</style>
```

## Testing Instructions

1. **Create Demo Page**: Copy the above code to a new page in your project
2. **Add to pages.json**: Register the demo page in your pages.json
3. **Navigate to Demo**: Open the demo page in your mini-program

## What to Test

### Search Functionality
1. Type "ORDER001" in search box
2. Wait 500ms (debounce)
3. Verify only ORDER001 appears in sample list
4. Clear search and verify all orders return

### Status Filter
1. Click "全部状态" button
2. Select "completed" status
3. Verify only completed orders appear
4. Select "全部状态" to clear filter

### Date Range Filter
1. Click "选择日期" button
2. Select date range: 2026-01-01 to 2026-01-03
3. Verify only orders in that range appear
4. Try quick date buttons (今天, 最近7天, 最近30天)

### Multiple Filters
1. Enter search query: "暨南大学"
2. Select status: "completed"
3. Select date range: 2026-01-01 to 2026-01-03
4. Verify only orders matching ALL filters appear

### Clear Filters
1. Apply multiple filters
2. Click "清除" button
3. Verify all filters reset and all orders appear

## Expected Behavior

- Search input should debounce (wait 500ms after typing stops)
- Status picker should show modal popup with all statuses
- Date picker should show modal with date range selector
- Multiple filters should work together (AND logic)
- Clear button should reset all filters at once
- Filter output should update in real-time
- Sample order list should filter based on current filters

## Console Output

Check the browser/mini-program console for filter change events:

```javascript
Filter changed: {
  searchQuery: "ORDER001",
  status: "completed",
  dateRange: ["2026-01-01", "2026-01-03"],
  startDate: "2026-01-01",
  endDate: "2026-01-03"
}
```

## Troubleshooting

### Date Picker Not Showing
- Ensure uni-datetime-picker is installed in uni_modules
- Check if component is properly imported

### Icons Not Displaying
- Verify ColorUI icon font is loaded
- Check if icon class names are correct

### Debouncing Not Working
- Check console for errors
- Verify timer is being created and cleared properly

### Filters Not Applying
- Check if filter-change event is being emitted
- Verify parent component is handling the event
- Check console for filter object structure

## Performance Testing

1. **Rapid Typing**: Type quickly in search box and verify only one API call after 500ms
2. **Multiple Clicks**: Click status filter multiple times and verify no performance issues
3. **Date Selection**: Select dates multiple times and verify smooth operation
4. **Clear Filters**: Click clear button multiple times and verify no memory leaks

## Accessibility Testing

1. **Touch Targets**: Verify all buttons are easy to tap (minimum 70rpx height)
2. **Visual Feedback**: Verify active states are clearly visible
3. **Loading States**: Verify loading indicators appear when appropriate
4. **Error Messages**: Verify error messages are clear and helpful
