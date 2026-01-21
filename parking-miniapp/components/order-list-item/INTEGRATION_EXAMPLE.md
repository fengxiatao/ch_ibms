# OrderListItem Integration Example

## How to Integrate into Existing History Order List Page

This document shows how to integrate the `OrderListItem` component into the existing `historyOrderList.vue` page.

## Step 1: Import the Component

Add the import statement at the top of your script section:

```vue
<script>
import OrderListItem from '@/components/order-list-item/order-list-item.vue';

export default {
  components: {
    OrderListItem
  },
  // ... rest of your code
}
</script>
```

## Step 2: Replace the Order List Template

Replace the existing order list rendering code with the new component:

### Before (Old Code):

```vue
<view class="order-list">
  <view class="order-list-item" v-for="(item, index) in dataList" :key="index" @click="orderDetail(item)"
    :class="[item.transactionStatus == 3? 'yituikuan': '', item.transactionStatus == 4? 'tuikuanzhong': '']">
    <view class="list-item-title">
      <span class="item-title-left">
        广州邮通科技园东信科技大厦充电桩
      </span>
      <span class="item-title-right">
        车位：A189
      </span>
    </view>
    <!-- ... more template code ... -->
  </view>
  <view v-if="dataList.length == 0" style="margin-top: 100rpx;text-align: center;color: rgba(51,51,51,0.50);">暂无数据</view>
</view>
```

### After (New Code):

```vue
<view class="order-list">
  <order-list-item 
    v-for="(item, index) in dataList" 
    :key="index"
    :order="item"
    @click="orderDetail"
  />
  <view v-if="dataList.length == 0" style="margin-top: 100rpx;text-align: center;color: rgba(51,51,51,0.50);">暂无数据</view>
</view>
```

## Step 3: Update the orderDetail Method (Optional)

The component already handles navigation, but if you want to keep your custom logic:

```javascript
methods: {
  orderDetail(order) {
    console.log('Order clicked:', order);
    
    // Your custom logic here
    if (order.transactionStatus == 2) {
      this.$u.route({
        url: '/package/chargingPile/pending-order/pending-order',
      });
    } else {
      this.$u.route({
        url: '/package/chargingPile/orderFormDetail',
        params: {
          item: JSON.stringify(order),
        }
      });
    }
  }
}
```

## Step 4: Remove Old Styles (Optional)

You can remove the old `.order-list-item` styles from your page since the component has its own styling:

```scss
// These styles can be removed:
.order-list-item {
  // ... old styles
}

.yituikuan {
  // ... old styles
}

.tuikuanzhong {
  // ... old styles
}
```

## Complete Integration Example

Here's a complete example of the updated `historyOrderList.vue`:

```vue
<template>
  <view class="main">
    <view class="order-top">
      <view class="order-top-left">
        <span>{{cumulativeMoney || 0}}</span>
        <span>累积充电支出（元）</span>
      </view>
      <view class="order-top-line"></view>
      <view class="order-top-right">
        <span>{{cumulativeQuantity || 0}}</span>
        <span style="margin-top: 24rpx;height: 32rpx;font-weight: normal;color: rgba(51,51,51,0.50);">累积充电度数（度）</span>
      </view>
    </view>
    
    <view class="order-list">
      <!-- NEW: Use OrderListItem component -->
      <order-list-item 
        v-for="(item, index) in dataList" 
        :key="index"
        :order="item"
      />
      
      <view v-if="dataList.length == 0" style="margin-top: 100rpx;text-align: center;color: rgba(51,51,51,0.50);">
        暂无数据
      </view>
    </view>
    
    <!-- 回到顶部 -->
    <view class="toast-top" @click="pullTop" v-if="flag">
      <image class="toast-top-img" src="http://ismart.loongtek.cn/image/xcx/charge/back-top.png"></image>
    </view>
  </view>
</template>

<script>
import { mapGetters } from "vuex";
import url from "../../common/http/URL.js";
import OrderListItem from '@/components/order-list-item/order-list-item.vue';

export default {
  components: {
    OrderListItem
  },
  data() {
    return {
      isRefresh: false,
      flag: false,
      dataList: [],
      total: null,
      params: {
        pageNo: 1,
        pageSize: 10,
        userId: ""
      },
      cumulativeMoney: '',
      cumulativeQuantity: ''
    }
  },
  computed: {
    ...mapGetters(['getUser'])
  },
  onLoad() {
    this.params.userId = this.getUser.id;
    this.getList();
  },
  onShow() {
    let pages = getCurrentPages();
    let currPage = pages[pages.length - 1];
    if (currPage.__data__.isRefresh) {
      this.flag = false;
      this.dataList = [];
      this.total = null;
      this.params = {
        pageNo: 1,
        pageSize: 10,
        userId: this.getUser.id
      };
      this.cumulativeMoney = '';
      this.cumulativeQuantity = '';
      this.getList();
      currPage.__data__.isRefresh = false;
    }
  },
  methods: {
    getList() {
      this.$u.api.GET(url.getHistoryCharge, this.params).then(res => {
        if (res.code == 200) {
          console.log('响应：', res.data);
          this.cumulativeMoney = res.data.totalData ? res.data.totalData.cumulativeMoney : 0;
          this.cumulativeQuantity = res.data.totalData ? res.data.totalData.cumulativeQuantity : 0;
          let rows = res.data.historyData.list;
          if (rows && rows.length > 0) {
            this.dataList = [...this.dataList, ...rows];
          }
          this.total = res.data.historyData.total;
        }
      });
    },
    pullTop() {
      uni.pageScrollTo({
        scrollTop: 0,
        duration: 500
      });
    },
    onPageScroll(e) {
      if (e.scrollTop > 390) {
        this.flag = true;
      } else {
        this.flag = false;
      }
    }
  },
  onReachBottom() {
    if (this.params.pageNo * this.params.pageSize >= this.total) {
      uni.showToast({
        title: '没有更多数据了',
        icon: 'none',
        duration: 1000
      });
    } else {
      if (this.params.pageNo <= this.params.pageNo - 1) {
        setTimeout(() => {
          uni.hideLoading();
        }, 200);
      } else {
        uni.showLoading({
          title: '加载中'
        });
        this.params.pageNo++;
        this.getList();
      }
      setTimeout(() => {
        uni.hideLoading();
      }, 200);
    }
  }
}
</script>

<style lang="scss" scoped>
.main {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #e36452, #ffffff 356rpx, #ffffff);
  display: flex;
  flex-direction: column;
  align-items: center;

  .order-top {
    margin-top: 44rpx;
    width: 640rpx;
    height: 180rpx;
    border-radius: 20rpx;
    background-color: #ffffff;
    box-shadow: 2rpx 2rpx 20rpx 0rpx rgba(202, 200, 200, 1.00);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx 20rpx;

    .order-top-left {
      display: flex;
      flex-direction: column;
      align-items: center;

      span:nth-child(1) {
        height: 80rpx;
        font-size: 60rpx;
        font-family: '微软雅黑 Bold', '微软雅黑 Regular', '微软雅黑', sans-serif;
        font-weight: bold;
      }

      span:nth-child(2) {
        margin-top: 24rpx;
        height: 32rpx;
        font-weight: normal;
        color: rgba(51, 51, 51, 0.50);
      }
    }

    .order-top-line {
      width: 2rpx;
      height: 100rpx;
      font-weight: normal;
      background-color: #ed6c60;
    }

    .order-top-right {
      display: flex;
      flex-direction: column;
      align-items: center;

      span:nth-child(1) {
        height: 80rpx;
        font-size: 60rpx;
        font-family: '微软雅黑 Bold', '微软雅黑 Regular', '微软雅黑', sans-serif;
        font-weight: bold;
      }
    }
  }

  .order-list {
    width: 640rpx;
    margin-top: 56rpx;
  }
}

.toast-top {
  width: 75rpx;
  height: 75rpx;
  border-radius: 50%;
  position: fixed;
  z-index: 999;
  right: 8rpx;
  bottom: 90rpx;

  .toast-top-img {
    width: 75rpx;
    height: 75rpx;
    border-radius: 50%;
  }
}
</style>
```

## Benefits of Using the Component

1. **Consistency**: All order items will have the same look and feel
2. **Maintainability**: Changes to order display only need to be made in one place
3. **Reusability**: The component can be used in other pages that display orders
4. **Feature Support**: Built-in support for new features like refund status display
5. **Clean Code**: Reduces template complexity in parent pages

## Testing the Integration

After integration, test the following:

1. ✅ Order list displays correctly
2. ✅ Status badges show correct colors
3. ✅ Amounts are formatted to 2 decimal places
4. ✅ Clicking an order navigates to detail page
5. ✅ Refund information displays when applicable
6. ✅ Empty state shows when no orders
7. ✅ Pagination still works
8. ✅ Pull-to-refresh functionality works

## Troubleshooting

### Component Not Found Error

If you see "Component not found" error, make sure:
1. The import path is correct: `@/components/order-list-item/order-list-item.vue`
2. The component is registered in the `components` object
3. The component file exists in the correct location

### Styling Issues

If styling looks wrong:
1. Check that the component's SCSS is being compiled
2. Verify that parent page styles aren't conflicting
3. Ensure the component has the correct width (640rpx)

### Navigation Not Working

If clicking doesn't navigate:
1. Check that the order object has `orderNo` or `orderId` field
2. Verify the navigation URLs are correct
3. Check console for any JavaScript errors

## Migration Checklist

- [ ] Import OrderListItem component
- [ ] Register component in components object
- [ ] Replace old template with new component
- [ ] Test order list display
- [ ] Test navigation to detail page
- [ ] Test with different order statuses
- [ ] Test with refund data
- [ ] Test empty state
- [ ] Test pagination
- [ ] Remove old styles (optional)
- [ ] Update any related documentation
