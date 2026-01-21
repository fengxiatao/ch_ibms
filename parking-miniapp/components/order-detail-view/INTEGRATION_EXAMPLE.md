# OrderDetailView Integration Example

## Basic Integration

### Step 1: Import the Component

```vue
<script>
import OrderDetailView from '@/components/order-detail-view/order-detail-view.vue';

export default {
  components: {
    OrderDetailView
  }
};
</script>
```

### Step 2: Use in Template

```vue
<template>
  <view class="page">
    <order-detail-view :orderId="orderId"></order-detail-view>
  </view>
</template>
```

### Step 3: Pass Order ID

```vue
<script>
export default {
  data() {
    return {
      orderId: ''
    };
  },
  onLoad(options) {
    // Get order ID from route parameters
    this.orderId = options.orderId || options.orderNo;
  }
};
</script>
```

## Complete Page Example

```vue
<template>
  <view class="order-detail-page">
    <!-- Order Detail View -->
    <order-detail-view 
      v-if="orderId" 
      :orderId="orderId"
    ></order-detail-view>
    
    <!-- Additional Actions (Optional) -->
    <view class="action-bar" v-if="showActions">
      <u-button @click="handleRefund" type="primary">申请退款</u-button>
      <u-button @click="handleContact" type="default">联系客服</u-button>
    </view>
  </view>
</template>

<script>
import OrderDetailView from '@/components/order-detail-view/order-detail-view.vue';
import { mapGetters } from 'vuex';

export default {
  components: {
    OrderDetailView
  },
  data() {
    return {
      orderId: '',
      showActions: false
    };
  },
  computed: {
    ...mapGetters(['getUser'])
  },
  onLoad(options) {
    // Parse order ID from route
    if (options.item) {
      try {
        const item = JSON.parse(decodeURIComponent(options.item));
        this.orderId = item.order || item.orderId;
      } catch (e) {
        console.error('Parse order ID error:', e);
      }
    } else {
      this.orderId = options.orderId || options.orderNo;
    }
    
    // Determine if actions should be shown
    this.checkActions();
  },
  methods: {
    checkActions() {
      // Add logic to determine if action buttons should be shown
      // For example, based on order status
      this.showActions = true;
    },
    handleRefund() {
      // Handle refund action
      uni.showModal({
        title: '申请退款',
        content: '确定要申请退款吗？',
        success: (res) => {
          if (res.confirm) {
            // Call refund API
            this.submitRefund();
          }
        }
      });
    },
    handleContact() {
      // Handle contact customer service
      uni.makePhoneCall({
        phoneNumber: '400-123-4567'
      });
    },
    async submitRefund() {
      try {
        // Call refund API
        const response = await this.$u.api.POST('/api/refund/initiate', {
          orderId: this.orderId,
          userId: this.getUser.id
        });
        
        if (response.code === 200) {
          uni.showToast({
            title: '退款申请成功',
            icon: 'success'
          });
          // Refresh order detail
          this.$refs.orderDetail.loadOrderDetail();
        }
      } catch (error) {
        uni.showToast({
          title: '退款申请失败',
          icon: 'none'
        });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: #ffffff;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
  z-index: 100;
  
  .u-button {
    flex: 1;
  }
}
</style>
```

## Navigation Examples

### From Order List

```vue
<template>
  <view class="order-list">
    <order-list-item 
      v-for="order in orders" 
      :key="order.orderId"
      :order="order"
      @click="navigateToDetail(order)"
    ></order-list-item>
  </view>
</template>

<script>
export default {
  methods: {
    navigateToDetail(order) {
      uni.navigateTo({
        url: `/package/chargingPile/orderFormDetail?item=${encodeURIComponent(JSON.stringify({
          order: order.orderId || order.orderNo
        }))}`
      });
    }
  }
};
</script>
```

### From Notification

```vue
<script>
export default {
  methods: {
    handleNotificationClick(notification) {
      // Navigate to order detail from notification
      uni.navigateTo({
        url: `/package/chargingPile/orderFormDetail?orderId=${notification.orderId}`
      });
    }
  }
};
</script>
```

### From QR Code Scan

```vue
<script>
export default {
  methods: {
    async handleScanResult(result) {
      try {
        // Parse QR code result
        const data = JSON.parse(result);
        
        if (data.type === 'order' && data.orderId) {
          // Navigate to order detail
          uni.navigateTo({
            url: `/package/chargingPile/orderFormDetail?orderId=${data.orderId}`
          });
        }
      } catch (error) {
        console.error('Parse QR code error:', error);
      }
    }
  }
};
</script>
```

## API Configuration

### Add New Endpoint (Optional)

If you want to use the new API endpoint, add it to `common/http/URL.js`:

```javascript
module.exports = {
  // ... existing endpoints
  
  // New order detail endpoint
  getOrderDetail: "/api/order/detail",
  
  // ... other endpoints
};
```

### API Response Format

The component expects the following response format:

```javascript
{
  code: 200,
  msg: "Success",
  data: {
    orderId: "ORDER123456",
    userId: "USER123",
    chargingStationName: "Station A",
    chargingPileId: "PILE001",
    chargingPileName: "Pile 1",
    licensePlate: "粤A12345",
    transactionId: "TXN123",
    prepaymentAmount: 50.00,
    actualConsumption: 48.00,
    refundAmount: 2.00,
    refundStatus: "success",
    refundTime: "2026-01-01T10:30:00Z",
    status: "refunded",
    paymentMethod: "wallet",
    paymentTime: "2026-01-01T10:00:00Z",
    chargingStartTime: "2026-01-01T10:05:00Z",
    chargingEndTime: "2026-01-01T11:00:00Z",
    chargingDuration: 55,
    electricityUsed: 12.5,
    electricChargeMoney: 40.00,
    serviceMoney: 8.00,
    createdAt: "2026-01-01T09:55:00Z",
    updatedAt: "2026-01-01T11:01:00Z",
    estimatedArrival: "Refund will arrive within 5 minutes"
  }
}
```

## Customization Examples

### Custom Loading State

```vue
<template>
  <view class="custom-page">
    <view v-if="loading" class="custom-loading">
      <image src="/static/loading.gif" class="loading-gif"></image>
      <text>加载订单详情中...</text>
    </view>
    <order-detail-view 
      v-else 
      :orderId="orderId"
    ></order-detail-view>
  </view>
</template>
```

### With Pull-to-Refresh

```vue
<template>
  <scroll-view 
    class="page" 
    scroll-y 
    refresher-enabled 
    :refresher-triggered="refreshing"
    @refresherrefresh="onRefresh"
  >
    <order-detail-view 
      ref="orderDetail"
      :orderId="orderId"
    ></order-detail-view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
      refreshing: false
    };
  },
  methods: {
    async onRefresh() {
      this.refreshing = true;
      await this.$refs.orderDetail.loadOrderDetail();
      this.refreshing = false;
    }
  }
};
</script>
```

### With Share Functionality

```vue
<template>
  <view class="page">
    <order-detail-view :orderId="orderId"></order-detail-view>
    
    <view class="share-button" @click="handleShare">
      <text>分享订单</text>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    handleShare() {
      uni.share({
        provider: 'weixin',
        scene: 'WXSceneSession',
        type: 0,
        href: `pages/order/detail?orderId=${this.orderId}`,
        title: '我的充电订单',
        summary: '查看我的充电订单详情',
        imageUrl: '/static/share-icon.png',
        success: () => {
          uni.showToast({
            title: '分享成功',
            icon: 'success'
          });
        }
      });
    }
  }
};
</script>
```

## Troubleshooting

### Issue: Order data not loading

**Solution**: Check if the API endpoint is configured correctly in `common/http/URL.js` and verify the user is authenticated.

### Issue: Timeline not showing correctly

**Solution**: Ensure the order object has the required status and timestamp fields. Check the console for any errors.

### Issue: Refund information not displaying

**Solution**: Verify that `order.refundAmount > 0` and `order.refundStatus` is set correctly.

### Issue: Legacy data not mapping correctly

**Solution**: Check the `mapLegacyData` method in the component and ensure all legacy fields are mapped correctly.

## Best Practices

1. **Always pass a valid order ID**: Ensure the order ID is not null or undefined before rendering the component.

2. **Handle navigation properly**: Use proper URL encoding when passing order IDs through navigation.

3. **Provide user feedback**: Show loading states and error messages to improve user experience.

4. **Test with different order states**: Test the component with orders in different states (created, paid, charging, completed, refunded, cancelled).

5. **Handle API errors gracefully**: Implement proper error handling and provide retry options.

6. **Keep the component updated**: If the API response format changes, update the component accordingly.

7. **Use the component consistently**: Use the same component across all pages that need to display order details for consistency.
