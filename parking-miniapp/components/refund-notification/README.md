# RefundNotification Component

## Overview
A Vue component for displaying refund notifications in the charging pile mini-program. Shows refund status updates with the ability to navigate to order details and dismiss the notification.

## Features
- ✅ Display refund status with color-coded styling
- ✅ Show refund amount and custom messages
- ✅ Navigate to order detail page on click
- ✅ Dismiss functionality with animation
- ✅ Auto-dismiss after configurable duration
- ✅ Animated loading icon for processing status
- ✅ Safe area support for notched devices
- ✅ Slide-down animation on appearance

## Props

### refundId
- **Type**: `String`
- **Required**: `true`
- **Default**: `''`
- **Description**: Unique refund identifier

### amount
- **Type**: `Number | String`
- **Required**: `false`
- **Default**: `0`
- **Description**: Refund amount in yuan

### status
- **Type**: `String`
- **Required**: `true`
- **Default**: `'success'`
- **Validator**: Must be one of: `'success'`, `'failed'`, `'processing'`, `'abnormal'`
- **Description**: Current refund status

### message
- **Type**: `String`
- **Required**: `false`
- **Default**: `''`
- **Description**: Custom notification message

### orderId
- **Type**: `String`
- **Required**: `false`
- **Default**: `''`
- **Description**: Order ID for navigation to detail page

### showRefundId
- **Type**: `Boolean`
- **Required**: `false`
- **Default**: `false`
- **Description**: Whether to display refund ID in notification

### showActionHint
- **Type**: `Boolean`
- **Required**: `false`
- **Default**: `true`
- **Description**: Whether to show "点击查看详情" hint

### autoDismiss
- **Type**: `Number`
- **Required**: `false`
- **Default**: `5000`
- **Description**: Auto dismiss duration in milliseconds (0 = no auto dismiss)

## Events

### @click
Emitted when notification is clicked.
- **Payload**: `{ refundId, orderId, status }`

### @dismiss
Emitted when notification is dismissed.
- **Payload**: `{ refundId, status }`

## Usage

### Basic Usage
```vue
<template>
  <refund-notification
    refundId="REF123456"
    :amount="50.00"
    status="success"
    orderId="ORD789"
  />
</template>

<script>
import RefundNotification from '@/components/refund-notification/refund-notification.vue';

export default {
  components: {
    RefundNotification
  }
};
</script>
```

### Success Notification
```vue
<refund-notification
  refundId="REF123456"
  :amount="50.00"
  status="success"
  message="退款已成功到账"
  orderId="ORD789"
  :autoDismiss="5000"
/>
```

### Failed Notification
```vue
<refund-notification
  refundId="REF123456"
  :amount="50.00"
  status="failed"
  message="商户余额不足，请联系客服"
  orderId="ORD789"
  :autoDismiss="0"
/>
```

### Processing Notification
```vue
<refund-notification
  refundId="REF123456"
  :amount="50.00"
  status="processing"
  message="退款申请已提交，预计5分钟内到账"
  orderId="ORD789"
/>
```

### With Event Handlers
```vue
<template>
  <refund-notification
    refundId="REF123456"
    :amount="50.00"
    status="success"
    orderId="ORD789"
    @click="handleNotificationClick"
    @dismiss="handleNotificationDismiss"
  />
</template>

<script>
export default {
  methods: {
    handleNotificationClick(data) {
      console.log('Notification clicked:', data);
      // Custom navigation or tracking logic
    },
    handleNotificationDismiss(data) {
      console.log('Notification dismissed:', data);
      // Clean up or tracking logic
    }
  }
};
</script>
```

### Without Auto Dismiss
```vue
<refund-notification
  refundId="REF123456"
  :amount="50.00"
  status="failed"
  message="退款失败，请重试"
  orderId="ORD789"
  :autoDismiss="0"
/>
```

### With Refund ID Display
```vue
<refund-notification
  refundId="REF123456"
  :amount="50.00"
  status="success"
  orderId="ORD789"
  :showRefundId="true"
/>
```

## Status States

### Success
- **Color**: Green (#67C23A)
- **Icon**: Check mark
- **Title**: "退款成功"
- **Border**: Left green border

### Failed
- **Color**: Red (#F56C6C)
- **Icon**: Close mark
- **Title**: "退款失败"
- **Border**: Left red border

### Processing
- **Color**: Blue (#409EFF)
- **Icon**: Loading spinner (animated)
- **Title**: "退款处理中"
- **Border**: Left blue border

### Abnormal
- **Color**: Orange (#E6A23C)
- **Icon**: Warning
- **Title**: "退款异常"
- **Border**: Left orange border

## Methods

### formatAmount(amount)
Formats amount to 2 decimal places.
- **Parameters**: `amount` (Number|String)
- **Returns**: String (e.g., "50.00")

### getTitle()
Returns status-specific title.
- **Returns**: String

### handleClick()
Handles notification click, emits event and navigates to order detail.

### handleDismiss()
Handles dismiss action, clears timer and hides notification.

## Styling

### Position
- Fixed at top of screen
- 20rpx from top (with safe area padding)
- 30rpx from left and right

### Animation
- Slide down on appearance (0.3s)
- Fade out on dismiss

### Layout
- Card-based design
- Rounded corners (16rpx)
- Shadow for depth
- Color-coded left border (8rpx)

## Integration Examples

### In App.vue (Global Notifications)
```vue
<template>
  <view>
    <refund-notification
      v-if="refundNotification"
      :refundId="refundNotification.refundId"
      :amount="refundNotification.amount"
      :status="refundNotification.status"
      :message="refundNotification.message"
      :orderId="refundNotification.orderId"
      @dismiss="clearRefundNotification"
    />
    
    <!-- Rest of app content -->
  </view>
</template>

<script>
import RefundNotification from '@/components/refund-notification/refund-notification.vue';

export default {
  components: {
    RefundNotification
  },
  data() {
    return {
      refundNotification: null
    };
  },
  methods: {
    showRefundNotification(data) {
      this.refundNotification = data;
    },
    clearRefundNotification() {
      this.refundNotification = null;
    }
  }
};
</script>
```

### With Vuex Store
```javascript
// store/modules/notification.js
export default {
  state: {
    refundNotification: null
  },
  mutations: {
    SET_REFUND_NOTIFICATION(state, data) {
      state.refundNotification = data;
    },
    CLEAR_REFUND_NOTIFICATION(state) {
      state.refundNotification = null;
    }
  },
  actions: {
    showRefundNotification({ commit }, data) {
      commit('SET_REFUND_NOTIFICATION', data);
    },
    clearRefundNotification({ commit }) {
      commit('CLEAR_REFUND_NOTIFICATION');
    }
  }
};
```

## Accessibility
- Clear visual indicators for each status
- Color-coded with icons for better recognition
- Descriptive text for screen readers
- High contrast for readability
- Large touch target for dismiss button

## Browser Compatibility
- WeChat Mini Program
- Supports all modern mini-program environments
- Safe area support for notched devices

## Dependencies
- ColorUI icons (cuIcon-*)
- uni-app framework

## Notes
1. Notification appears at top of screen with slide-down animation
2. Auto-dismisses after 5 seconds by default (configurable)
3. Click anywhere on notification to navigate to order detail
4. Click dismiss button to close without navigation
5. Loading icon animates for processing status
6. Safe area padding for devices with notch

## Best Practices
1. Use appropriate status for each notification type
2. Provide clear, concise messages
3. Include order ID for navigation
4. Set reasonable auto-dismiss duration
5. Handle dismiss event for cleanup
6. Test on devices with notch

## Future Enhancements
- Add sound/vibration on notification
- Support for notification queue
- Add swipe-to-dismiss gesture
- Support for action buttons
- Add notification history
- Support for rich media (images)

## Version History
- **v1.0.0** (2026-01-02): Initial implementation
  - Basic notification display
  - Four status states support
  - Navigation and dismiss functionality
  - Auto-dismiss feature
  - Animation support
