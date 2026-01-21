# OrderSearchFilter Component

## Overview

The `OrderSearchFilter` component provides a comprehensive filtering interface for order lists. It includes text search with debouncing, status filtering, and date range selection capabilities.

## Features

- **Text Search**: Search orders by order number or charging station name with 500ms debouncing
- **Status Filter**: Filter orders by status (created, paid, charging, completed, refunded, cancelled)
- **Date Range Picker**: Select date ranges with quick options (today, last 7 days, last 30 days)
- **Clear Filters**: One-click button to reset all filters
- **Filter Events**: Emits filter change events to parent component

## Usage

### Basic Usage

```vue
<template>
  <view>
    <order-search-filter 
      @filter-change="handleFilterChange"
    />
    
    <!-- Your order list here -->
  </view>
</template>

<script>
import OrderSearchFilter from '@/components/order-search-filter/order-search-filter.vue';

export default {
  components: {
    OrderSearchFilter
  },
  methods: {
    handleFilterChange(filters) {
      console.log('Filters changed:', filters);
      // filters object contains:
      // - searchQuery: string
      // - status: string
      // - dateRange: array [startDate, endDate]
      // - startDate: string (YYYY-MM-DD)
      // - endDate: string (YYYY-MM-DD)
      
      // Apply filters to your order list
      this.loadOrders(filters);
    }
  }
};
</script>
```

### With Initial Filters

```vue
<template>
  <order-search-filter 
    :initial-filters="initialFilters"
    @filter-change="handleFilterChange"
  />
</template>

<script>
export default {
  data() {
    return {
      initialFilters: {
        searchQuery: '',
        status: 'completed',
        dateRange: ['2026-01-01', '2026-01-31']
      }
    };
  }
};
</script>
```

## Props

| Prop | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| initialFilters | Object | No | `{}` | Initial filter values to populate the component |

### initialFilters Object Structure

```javascript
{
  searchQuery: '',      // Search text
  status: '',          // Order status
  dateRange: []        // Date range array [startDate, endDate]
}
```

## Events

### filter-change

Emitted when any filter value changes (with debouncing for search input).

**Payload:**

```javascript
{
  searchQuery: '',     // Search text (trimmed)
  status: '',         // Selected status value
  dateRange: [],      // Date range array or null
  startDate: '',      // Start date (YYYY-MM-DD) or null
  endDate: ''         // End date (YYYY-MM-DD) or null
}
```

## Status Options

The component supports the following order statuses:

- `''` - All statuses (default)
- `'created'` - 已创建 (Created)
- `'paid'` - 已支付 (Paid)
- `'charging'` - 充电中 (Charging)
- `'completed'` - 已完成 (Completed)
- `'refunded'` - 已退款 (Refunded)
- `'cancelled'` - 已取消 (Cancelled)

## Date Range Features

### Quick Date Selection

The component provides three quick date selection options:

1. **Today** - Sets date range to current day
2. **Last 7 Days** - Sets date range to last 7 days
3. **Last 30 Days** - Sets date range to last 30 days

### Custom Date Range

Users can also select custom date ranges using the date picker component.

## Implementation Details

### Debouncing

The search input uses a 500ms debounce to prevent excessive filter events while the user is typing. This improves performance and reduces unnecessary API calls.

### Filter State Management

The component maintains its own internal state for all filters and emits changes to the parent component. This allows for flexible integration without requiring the parent to manage filter state.

### Cleanup

The component properly cleans up the debounce timer in the `beforeDestroy` lifecycle hook to prevent memory leaks.

## Styling

The component uses ColorUI icons and follows the project's design system:

- Search input with rounded corners
- Filter pills with rounded corners
- Modal popups with slide-up animation
- Active state highlighting for selected filters

## Requirements Validation

This component satisfies the following requirements:

- **16.1**: Text search filtering by order number and charging station name
- **16.2**: Status filter dropdown with all order statuses
- **16.3**: Date range picker with custom and quick selection options
- **16.4**: Multiple filter support (all filters can be applied simultaneously)
- **16.5**: Clear filters functionality with visual indicator

## Dependencies

- `uni-datetime-picker` - Used for date range selection
- ColorUI icons - Used for UI icons

## Browser/Platform Support

This component is designed for uni-app and supports:

- WeChat Mini Program
- H5
- App (iOS/Android)

## Example Integration

See `INTEGRATION_EXAMPLE.md` for a complete example of integrating this component into an order history page.
