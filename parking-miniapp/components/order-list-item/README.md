# OrderListItem Component

## Overview

The `OrderListItem` component is a reusable Vue component designed to display charging order summaries in the order history list. It provides a comprehensive view of order information including status, amounts, refund details, and timestamps.

## Features

- **Order Summary Display**: Shows order number, station name, charging pile information
- **Status Badge**: Color-coded status indicators (created, paid, charging, completed, refunded, cancelled)
- **Amount Formatting**: Automatically formats amounts to 2 decimal places
- **Refund Information**: Displays refund amount and status when applicable
- **Click Navigation**: Navigates to order detail page on click
- **Responsive Design**: Adapts to different screen sizes
- **Legacy Support**: Compatible with existing order data structure

## Props

### `order` (Object, Required)

The order data object containing all order information.

**Expected Fields:**
```javascript
{
  // Order Identification
  orderId: String,           // Unique order ID
  orderNo: String,           // Order number (alternative to orderId)
  
  // Station Information
  chargingStationName: String,  // Charging station name
  chargingStationId: String,    // Charging station ID
  chargingPileName: String,     // Charging pile name
  chargingPileId: String,       // Charging pile ID
  code: String,                 // Pile code (legacy)
  gunNumber: String,            // Gun number
  
  // Status
  status: String,               // Order status: 'created', 'paid', 'charging', 'completed', 'refunded', 'cancelled'
  transactionStatus: Number,    // Legacy status field (1-5)
  
  // Payment Information
  prepaymentAmount: Number,     // Prepayment amount in yuan
  actualConsumption: Number,    // Actual consumption amount
  actualArrivalMoney: Number,   // Actual arrival money (legacy)
  
  // Refund Information
  refundAmount: Number,         // Refund amount in yuan
  refundStatus: String,         // Refund status: 'processing', 'success', 'failed', 'abnormal'
  refundTime: String,           // Refund timestamp
  
  // Time Information
  paymentTime: String,          // Payment timestamp
  chargingStartTime: String,    // Charging start time
  chargingEndTime: String,      // Charging end time
  startTime: String,            // Start time (legacy)
  endTime: String,              // End time (legacy)
  
  // Electricity Information
  electricityUsed: Number,      // Electricity used in kWh
  dosage: Number                // Dosage (legacy)
}
```

### `showRefundInfo` (Boolean, Optional)

Controls whether to display refund information.

- **Default**: `true`
- **Usage**: Set to `false` to hide refund details

## Events

### `@click`

Emitted when the order item is clicked.

**Payload**: The complete order object

**Example:**
```vue
<order-list-item 
  :order="order" 
  @click="handleOrderClick"
/>
```

## Usage Examples

### Basic Usage

```vue
<template>
  <view>
    <order-list-item 
      v-for="order in orders" 
      :key="order.orderId"
      :order="order"
    />
  </view>
</template>

<script>
import OrderListItem from '@/components/order-list-item/order-list-item.vue';

export default {
  components: {
    OrderListItem
  },
  data() {
    return {
      orders: []
    };
  }
};
</script>
```

### With Custom Click Handler

```vue
<template>
  <view>
    <order-list-item 
      :order="order"
      @click="handleOrderClick"
    />
  </view>
</template>

<script>
export default {
  methods: {
    handleOrderClick(order) {
      console.log('Order clicked:', order);
      // Custom navigation or action
    }
  }
};
</script>
```

### Hide Refund Information

```vue
<template>
  <order-list-item 
    :order="order"
    :show-refund-info="false"
  />
</template>
```

### Integration with Existing History Page

```vue
<template>
  <view class="order-list">
    <order-list-item 
      v-for="(item, index) in dataList" 
      :key="index"
      :order="item"
    />
  </view>
</template>

<script>
import OrderListItem from '@/components/order-list-item/order-list-item.vue';

export default {
  components: {
    OrderListItem
  },
  data() {
    return {
      dataList: []
    };
  },
  methods: {
    getList() {
      // Fetch order list from API
      this.$u.api.GET(url.getHistoryCharge, this.params).then(res => {
        if (res.code == 200) {
          this.dataList = res.data.historyData.list;
        }
      });
    }
  }
};
</script>
```

## Status Badge Colors

The component uses color-coded badges to indicate order status:

| Status | Color | Hex Code | Description |
|--------|-------|----------|-------------|
| Created | Gray | #999999 | Order created but not paid |
| Paid | Blue | #409EFF | Payment successful |
| Charging | Green | #67C23A | Charging in progress |
| Completed | Orange | #E6A23C | Charging completed |
| Refunded | Purple | #9B59B6 | Refund processed |
| Cancelled | Red | #F56C6C | Order cancelled |

## Refund Status Colors

Refund status is displayed with the following colors:

| Status | Color | Hex Code | Description |
|--------|-------|----------|-------------|
| Processing | Blue | #409EFF | Refund being processed |
| Success | Green | #67C23A | Refund successful |
| Failed | Red | #F56C6C | Refund failed |
| Abnormal | Orange | #E6A23C | Refund in abnormal state |

## Methods

### `formatAmount(amount)`

Formats a numeric amount to 2 decimal places.

**Parameters:**
- `amount` (Number): The amount to format

**Returns:** String - Formatted amount (e.g., "50.00")

**Example:**
```javascript
this.formatAmount(50); // "50.00"
this.formatAmount(48.5); // "48.50"
this.formatAmount(null); // "0.00"
```

### `formatDateTime(datetime)`

Formats a datetime string for display.

**Parameters:**
- `datetime` (String): The datetime string to format

**Returns:** String - Formatted datetime

**Example:**
```javascript
this.formatDateTime("2026-01-01T10:00:00Z"); // "2026-01-01 10:00:00"
this.formatDateTime(null); // "N/A"
```

### `getStatusBadgeClass()`

Returns the CSS class for the status badge based on order status.

**Returns:** String - CSS class name

### `getStatusText()`

Returns the display text for the order status.

**Returns:** String - Status text in Chinese

### `getRefundStatusClass()`

Returns the CSS class for refund status styling.

**Returns:** String - CSS class name

### `getRefundStatusText()`

Returns the display text for the refund status.

**Returns:** String - Refund status text in Chinese

### `handleClick()`

Handles the click event and navigates to the order detail page.

## Styling

The component uses SCSS for styling with the following key classes:

- `.order-list-item`: Main container
- `.item-header`: Header section with station info and status badge
- `.item-details`: Order details section
- `.item-footer`: Footer with amount information
- `.refund-status`: Refund status display section

### Background Images

The component supports background images for refund status:

- **Already Refunded**: `yituikuan` class
- **Refunding**: `tuikuanzhong` class

These images are positioned at the top-right corner of the order item.

## Compatibility

- **Framework**: uni-app
- **Vue Version**: Vue 2.x
- **Platform**: WeChat Mini Program, H5, App
- **Legacy Support**: Compatible with existing `transactionStatus` field

## Requirements Validation

This component satisfies the following requirements:

- ✅ **Requirement 14.2**: Display order number, station name, amounts, and status
- ✅ **Requirement 15.1**: Status badge with color coding
- ✅ Amount formatting to 2 decimal places
- ✅ Click handler to navigate to order detail page
- ✅ Display refund information when applicable

## Notes

1. The component automatically handles both new (`status`) and legacy (`transactionStatus`) status fields
2. All amounts are formatted to 2 decimal places for consistency
3. The component emits a `click` event before navigating, allowing parent components to intercept if needed
4. Refund information is only displayed when `showRefundInfo` is true and refund data exists
5. The component gracefully handles missing or null data fields

## Future Enhancements

Potential improvements for future versions:

1. Add loading state indicator
2. Support for custom status colors via props
3. Configurable date/time format
4. Support for multiple currencies
5. Accessibility improvements (ARIA labels)
6. Animation on status changes
7. Swipe actions (delete, archive, etc.)
