# OrderDetailView Component

## Overview

The `OrderDetailView` component provides a comprehensive view of charging order details, including complete order information, timeline visualization, consumption breakdown, and refund status.

## Features

- **Complete Order Information**: Displays all order details including station name, order number, charging pile, license plate, and payment method
- **Order Timeline**: Visual timeline showing order lifecycle stages (created → paid → charging → completed → refunded)
- **Time Information**: Shows all relevant timestamps (creation, payment, charging start/end, refund)
- **Consumption Breakdown**: Displays electricity usage, electric charges, service fees, and discounts
- **Refund Information**: Shows refund amount, status, and estimated/actual arrival time
- **Loading & Error States**: Handles loading and error states gracefully
- **Legacy Data Support**: Automatically maps legacy data structure to new format

## Props

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| orderId | String | Yes | The order ID to load and display |

## Usage

```vue
<template>
  <view>
    <order-detail-view :orderId="orderId"></order-detail-view>
  </view>
</template>

<script>
import OrderDetailView from '@/components/order-detail-view/order-detail-view.vue';

export default {
  components: {
    OrderDetailView
  },
  data() {
    return {
      orderId: 'ORDER123456'
    };
  }
};
</script>
```

## Data Structure

### Expected Order Data Format

```javascript
{
  orderId: 'ORDER123456',
  userId: 'USER123',
  chargingStationName: 'Station A',
  chargingPileId: 'PILE001',
  chargingPileName: 'Pile 1',
  licensePlate: '粤A12345',
  transactionId: 'TXN123',
  prepaymentAmount: 50.00,
  actualConsumption: 48.00,
  refundAmount: 2.00,
  refundStatus: 'success', // 'processing', 'success', 'failed', 'abnormal'
  refundTime: '2026-01-01T10:30:00Z',
  status: 'refunded', // 'created', 'paid', 'charging', 'completed', 'refunded', 'cancelled'
  paymentMethod: 'wallet', // 'wallet', 'balance', 'card'
  paymentTime: '2026-01-01T10:00:00Z',
  chargingStartTime: '2026-01-01T10:05:00Z',
  chargingEndTime: '2026-01-01T11:00:00Z',
  chargingDuration: 55,
  electricityUsed: 12.5,
  electricChargeMoney: 40.00,
  serviceMoney: 8.00,
  createdAt: '2026-01-01T09:55:00Z',
  updatedAt: '2026-01-01T11:01:00Z',
  estimatedArrival: 'Refund will arrive within 5 minutes'
}
```

### Legacy Data Support

The component automatically maps legacy data structure:

```javascript
{
  orderNo: 'ORDER123456',
  carNum: '粤A12345',
  totalAmount: 50.00,
  actualArrivalMoney: 48.00,
  transactionStatus: 3, // 1: completed, 2: charging, 3: refunded, 4: processing, 5: completed
  paymentType: 1, // 1: balance, 2: card
  startDateTime: '2026-01-01 10:05:00',
  endDateTime: '2026-01-01 11:00:00',
  totalDuration: 55,
  cumulativeQuantity: 12.5,
  electricChargeMoney: 40.00,
  serviceMoney: 8.00
}
```

## API Integration

The component attempts to load order data from two endpoints:

1. **New API** (if available): `url.getOrderDetail`
   - Parameters: `{ orderId, userId }`
   
2. **Legacy API** (fallback): `url.getHistoryChargeDetails`
   - Parameters: `{ orderNo, userId }`

## Status Mapping

### Order Status

| Status | Display Text | Badge Color |
|--------|-------------|-------------|
| created | 已创建 | Gray (#999999) |
| paid | 已支付 | Blue (#409EFF) |
| charging | 充电中 | Green (#67C23A) |
| completed | 已完成 | Orange (#E6A23C) |
| refunded | 已退款 | Purple (#9B59B6) |
| cancelled | 已取消 | Red (#F56C6C) |

### Refund Status

| Status | Display Text | Color |
|--------|-------------|-------|
| processing | 退款处理中 | Blue (#409EFF) |
| success | 退款成功 | Green (#67C23A) |
| failed | 退款失败 | Red (#F56C6C) |
| abnormal | 退款异常 | Orange (#E6A23C) |

## Methods

### Public Methods

None. The component is self-contained and loads data automatically.

### Internal Methods

- `loadOrderDetail()`: Loads order details from API
- `mapLegacyData(legacyData)`: Maps legacy data structure to new format
- `formatAmount(amount)`: Formats amount to 2 decimal places
- `formatDateTime(datetime)`: Formats datetime for display
- `getStatusBadgeClass()`: Returns CSS class for status badge
- `getStatusText()`: Returns display text for order status
- `getPaymentMethodText()`: Returns display text for payment method
- `getRefundStatusClass()`: Returns CSS class for refund status
- `getRefundStatusText()`: Returns display text for refund status
- `showConsumptionBreakdown()`: Determines if consumption breakdown should be shown
- `hasDiscount()`: Checks if order has discount
- `getDiscountAmount()`: Calculates discount amount

## Styling

The component uses a card-based layout with:
- White background cards with rounded corners
- Subtle shadows for depth
- Color-coded status badges
- Responsive layout
- Consistent spacing and typography

## Requirements Validation

This component validates the following requirements:

- **14.3**: Display complete order information including prepayment, consumption, and refund
- **14.4**: Show all relevant timestamps (payment, charging start/end, refund)
- **14.5**: Display refund status and estimated/actual arrival time
- **15.2**: Visual timeline of order lifecycle stages
- **15.3**: Stage status indicators (completed, in-progress, pending)
- **15.4**: Timestamps for completed stages
- **15.5**: Responsive timeline layout

## Dependencies

- `order-timeline` component (sub-component)
- `vuex` for user state management
- `uView` UI library for loading and button components
- `common/http/URL.js` for API endpoints

## Notes

- The component handles both new and legacy data structures automatically
- Loading and error states are handled gracefully
- The component is fully responsive and works on all screen sizes
- All amounts are formatted to 2 decimal places
- Datetime formatting is consistent throughout the component
