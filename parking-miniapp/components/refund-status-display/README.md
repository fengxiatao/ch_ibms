# RefundStatusDisplay Component

## Overview
A Vue component for displaying refund status information in the charging pile mini-program. Shows refund amount, status, arrival time, and relevant messages based on the refund state.

## Features
- ✅ Display refund amount with color coding
- ✅ Show refund status with icons and badges
- ✅ Display estimated/actual arrival time
- ✅ Show failure reasons for failed refunds
- ✅ Display abnormal status notices
- ✅ Payment method-specific arrival hints
- ✅ Animated loading icon for processing status
- ✅ Responsive design with proper spacing

## Props

### refundAmount
- **Type**: `Number | String`
- **Required**: `true`
- **Default**: `0`
- **Description**: The refund amount in yuan

### refundStatus
- **Type**: `String`
- **Required**: `true`
- **Default**: `'processing'`
- **Validator**: Must be one of: `'processing'`, `'success'`, `'failed'`, `'abnormal'`
- **Description**: Current refund status

### estimatedArrival
- **Type**: `String`
- **Required**: `false`
- **Default**: `''`
- **Description**: Estimated arrival time description (e.g., "5分钟内", "1-3个工作日")

### actualArrival
- **Type**: `String`
- **Required**: `false`
- **Default**: `''`
- **Description**: Actual arrival time (ISO datetime string or formatted string)

### failureReason
- **Type**: `String`
- **Required**: `false`
- **Default**: `''`
- **Description**: Reason for refund failure (displayed when status is 'failed')

### paymentMethod
- **Type**: `String`
- **Required**: `false`
- **Default**: `'wallet'`
- **Validator**: Must be one of: `'wallet'`, `'card'`
- **Description**: Original payment method, affects arrival time hint

## Usage

### Basic Usage
```vue
<template>
  <refund-status-display
    :refundAmount="50.00"
    refundStatus="processing"
    estimatedArrival="5分钟内"
  />
</template>

<script>
import RefundStatusDisplay from '@/components/refund-status-display/refund-status-display.vue';

export default {
  components: {
    RefundStatusDisplay
  }
};
</script>
```

### Processing Status
```vue
<refund-status-display
  :refundAmount="50.00"
  refundStatus="processing"
  estimatedArrival="5分钟内"
  paymentMethod="wallet"
/>
```

### Success Status
```vue
<refund-status-display
  :refundAmount="50.00"
  refundStatus="success"
  actualArrival="2026-01-02T10:30:00"
/>
```

### Failed Status
```vue
<refund-status-display
  :refundAmount="50.00"
  refundStatus="failed"
  failureReason="商户余额不足"
/>
```

### Abnormal Status
```vue
<refund-status-display
  :refundAmount="50.00"
  refundStatus="abnormal"
/>
```

## Status States

### Processing
- **Color**: Blue (#409EFF)
- **Icon**: Loading spinner (animated)
- **Shows**: Estimated arrival time and payment method hint
- **Description**: "退款申请已提交，正在处理中"

### Success
- **Color**: Green (#67C23A)
- **Icon**: Check mark
- **Shows**: Actual arrival time
- **Description**: "退款已成功到账"

### Failed
- **Color**: Red (#F56C6C)
- **Icon**: Close mark
- **Shows**: Failure reason and customer service contact
- **Description**: "退款处理失败"

### Abnormal
- **Color**: Orange (#E6A23C)
- **Icon**: Warning
- **Shows**: Abnormal notice and customer service contact
- **Description**: "退款处理出现异常"

## Methods

### formatAmount(amount)
Formats amount to 2 decimal places.
- **Parameters**: `amount` (Number|String)
- **Returns**: String (e.g., "50.00")

### formatDateTime(datetime)
Formats datetime string for display.
- **Parameters**: `datetime` (String)
- **Returns**: String (e.g., "2026-01-02 10:30:00")

### getStatusText()
Returns user-friendly status text.
- **Returns**: String

### getArrivalHint()
Returns arrival time hint based on payment method.
- **Returns**: String

## Styling

### Color Scheme
- **Processing**: Blue theme (#409EFF)
- **Success**: Green theme (#67C23A)
- **Failed**: Red theme (#F56C6C)
- **Abnormal**: Orange theme (#E6A23C)

### Layout
- Card-based design with rounded corners
- Shadow for depth
- Proper spacing and padding
- Responsive to different screen sizes

## Integration Example

### In Order Detail Page
```vue
<template>
  <view>
    <order-detail-view :orderId="orderId" />
    
    <refund-status-display
      v-if="order.refundAmount && order.refundStatus"
      :refundAmount="order.refundAmount"
      :refundStatus="order.refundStatus"
      :estimatedArrival="order.estimatedArrival"
      :actualArrival="order.refundTime"
      :failureReason="order.refundFailureReason"
      :paymentMethod="order.paymentMethod"
    />
  </view>
</template>

<script>
import OrderDetailView from '@/components/order-detail-view/order-detail-view.vue';
import RefundStatusDisplay from '@/components/refund-status-display/refund-status-display.vue';

export default {
  components: {
    OrderDetailView,
    RefundStatusDisplay
  },
  data() {
    return {
      orderId: '',
      order: {}
    };
  }
};
</script>
```

## Accessibility
- Clear visual indicators for each status
- Color-coded with icons for better recognition
- Descriptive text for screen readers
- High contrast for readability

## Browser Compatibility
- WeChat Mini Program
- Supports all modern mini-program environments

## Dependencies
- ColorUI icons (cuIcon-*)
- uni-app framework

## Notes
1. Amount is always displayed with 2 decimal places
2. Loading icon animates for processing status
3. Arrival hints differ based on payment method
4. Failed and abnormal statuses show customer service contact
5. Component is self-contained with no external dependencies

## Future Enhancements
- Add click-to-copy for customer service contact
- Add navigation to customer service chat
- Support for multiple refund records
- Add refund history timeline
- Support for partial refunds display

## Version History
- **v1.0.0** (2026-01-02): Initial implementation
  - Basic refund status display
  - Four status states support
  - Arrival time information
  - Failure and abnormal handling
