# OrderTimeline Component

## Overview

The `OrderTimeline` component provides a visual timeline representation of the charging order lifecycle, showing the progression through different stages with status indicators and timestamps.

## Features

- **Visual Timeline**: Clear visual representation of order progression
- **Stage Status Indicators**: Different icons for completed, in-progress, and pending stages
- **Timestamps**: Display timestamps for completed stages
- **Stage Descriptions**: Additional context for each stage
- **Animated States**: Pulse animation for in-progress stages
- **Responsive Layout**: Works on all screen sizes
- **Refund Support**: Includes refund stage when applicable

## Props

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| order | Object | Yes | The order object containing status and timestamp information |

## Usage

```vue
<template>
  <view>
    <order-timeline :order="orderData"></order-timeline>
  </view>
</template>

<script>
import OrderTimeline from '@/components/order-timeline/order-timeline.vue';

export default {
  components: {
    OrderTimeline
  },
  data() {
    return {
      orderData: {
        status: 'completed',
        createdAt: '2026-01-01T09:55:00Z',
        paymentTime: '2026-01-01T10:00:00Z',
        chargingStartTime: '2026-01-01T10:05:00Z',
        chargingEndTime: '2026-01-01T11:00:00Z',
        refundAmount: 2.00,
        refundStatus: 'success',
        refundTime: '2026-01-01T11:01:00Z'
      }
    };
  }
};
</script>
```

## Timeline Stages

The timeline displays the following stages based on order status:

1. **订单创建 (Order Created)**
   - Timestamp: `order.createdAt`
   - Description: "等待支付" when status is 'created'

2. **支付完成 (Payment Completed)**
   - Timestamp: `order.paymentTime`
   - Description: "等待开始充电" when status is 'paid'

3. **充电中 (Charging)**
   - Timestamp: `order.chargingStartTime`
   - Description: "正在充电..." when status is 'charging'

4. **充电完成 (Charging Completed)**
   - Timestamp: `order.chargingEndTime`
   - Description: "处理退款中" when status is 'completed' and refund exists

5. **退款完成 (Refund Completed)** *(conditional)*
   - Only shown if `order.refundAmount > 0`
   - Timestamp: `order.refundTime`
   - Description: Varies based on `order.refundStatus`

6. **订单取消 (Order Cancelled)** *(conditional)*
   - Only shown if `order.status === 'cancelled'`
   - Timestamp: `order.updatedAt`
   - Description: "订单已取消，退款处理中"

## Stage Status

Each stage can have one of three statuses:

### Completed
- **Icon**: ✓ (checkmark)
- **Color**: Green (#67C23A)
- **Line**: Green solid line
- **Text**: Bold, dark color

### In Progress
- **Icon**: ● (filled circle with pulse animation)
- **Color**: Blue (#409EFF)
- **Line**: Gray dashed line
- **Text**: Bold, blue color
- **Animation**: Pulse effect

### Pending
- **Icon**: ○ (empty circle)
- **Color**: Gray (#DCDFE6)
- **Line**: Gray dashed line
- **Text**: Light gray color

## Data Structure

### Required Order Fields

```javascript
{
  status: 'completed', // 'created', 'paid', 'charging', 'completed', 'refunded', 'cancelled'
  createdAt: '2026-01-01T09:55:00Z',
  paymentTime: '2026-01-01T10:00:00Z',
  chargingStartTime: '2026-01-01T10:05:00Z',
  chargingEndTime: '2026-01-01T11:00:00Z',
  refundAmount: 2.00, // Optional, triggers refund stage
  refundStatus: 'success', // Optional: 'processing', 'success', 'failed', 'abnormal'
  refundTime: '2026-01-01T11:01:00Z', // Optional
  updatedAt: '2026-01-01T11:01:00Z' // Used for cancelled orders
}
```

## Methods

### Computed Properties

- `stages`: Generates array of timeline stages based on order data

### Internal Methods

- `getStageStatus(stageName)`: Determines status of a stage (completed/in-progress/pending)
- `getRefundDescription()`: Returns description text for refund stage
- `formatAmount(amount)`: Formats amount to 2 decimal places
- `formatStageTime(timestamp)`: Formats timestamp as "MM-DD HH:mm"
- `getStageClass(stage)`: Returns CSS class for stage container
- `getNodeIconClass(stage)`: Returns CSS class for node icon
- `getLineClass(stage)`: Returns CSS class for connecting line
- `getStageNameClass(stage)`: Returns CSS class for stage name

## Styling

### Layout
- Vertical timeline with nodes on the left
- Content aligned to the right of nodes
- Connecting lines between stages
- Responsive spacing

### Colors
- **Completed**: Green (#67C23A)
- **In Progress**: Blue (#409EFF)
- **Pending**: Gray (#DCDFE6)

### Animations
- **Pulse**: Applied to in-progress node icon
- **Blink**: Applied to loading indicator

## Requirements Validation

This component validates the following requirements:

- **15.2**: Timeline visualization of order lifecycle stages
- **15.3**: Stage status indicators (completed: checkmark, in-progress: loading, pending: grayed)
- **15.4**: Display timestamps for completed stages
- **15.5**: Responsive timeline layout

## Example Scenarios

### Scenario 1: Order in Charging State
```javascript
{
  status: 'charging',
  createdAt: '2026-01-01T09:55:00Z',
  paymentTime: '2026-01-01T10:00:00Z',
  chargingStartTime: '2026-01-01T10:05:00Z'
}
```
Timeline shows:
- ✓ 订单创建 (completed)
- ✓ 支付完成 (completed)
- ● 充电中 (in-progress, animated)
- ○ 充电完成 (pending)

### Scenario 2: Completed Order with Refund
```javascript
{
  status: 'refunded',
  createdAt: '2026-01-01T09:55:00Z',
  paymentTime: '2026-01-01T10:00:00Z',
  chargingStartTime: '2026-01-01T10:05:00Z',
  chargingEndTime: '2026-01-01T11:00:00Z',
  refundAmount: 2.00,
  refundStatus: 'success',
  refundTime: '2026-01-01T11:01:00Z'
}
```
Timeline shows:
- ✓ 订单创建 (completed)
- ✓ 支付完成 (completed)
- ✓ 充电中 (completed)
- ✓ 充电完成 (completed)
- ✓ 退款完成 (completed) - "已退款 ¥2.00"

### Scenario 3: Cancelled Order
```javascript
{
  status: 'cancelled',
  createdAt: '2026-01-01T09:55:00Z',
  paymentTime: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:10:00Z'
}
```
Timeline shows:
- ✓ 订单创建 (completed)
- ✓ 支付完成 (completed)
- ○ 充电中 (pending)
- ○ 充电完成 (pending)
- ✓ 订单取消 (completed) - "订单已取消，退款处理中"

## Dependencies

- None (self-contained component)

## Notes

- The component automatically determines which stages to show based on order data
- Timestamps are formatted in a compact format (MM-DD HH:mm) for better readability
- The refund stage is only shown when `refundAmount > 0`
- The cancelled stage is only shown when `status === 'cancelled'`
- All animations are CSS-based for optimal performance
- The component is fully responsive and works on all screen sizes
