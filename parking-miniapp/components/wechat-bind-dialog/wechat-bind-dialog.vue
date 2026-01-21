<template>
  <u-modal 
    v-model="show" 
    :show-cancel-button="true"
    :show-confirm-button="true"
    :confirm-text="confirmText"
    :cancel-text="cancelText"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <view class="bind-dialog-content">
      <view class="bind-icon">
        <image src="/static/icon/wechat-icon.png" mode="aspectFit"></image>
      </view>
      <view class="bind-title">{{ title }}</view>
      <view class="bind-desc">{{ description }}</view>
      <view v-if="loading" class="bind-loading">
        <u-loading mode="circle"></u-loading>
        <text>绑定中...</text>
      </view>
    </view>
  </u-modal>
</template>

<script>
export default {
  name: 'WechatBindDialog',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '绑定微信账号'
    },
    description: {
      type: String,
      default: '使用微信支付需要先绑定微信账号，是否立即绑定？'
    },
    confirmText: {
      type: String,
      default: '立即绑定'
    },
    cancelText: {
      type: String,
      default: '取消'
    }
  },
  data() {
    return {
      loading: false
    }
  },
  computed: {
    show: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  methods: {
    handleConfirm() {
      this.loading = true
      this.$emit('confirm')
    },
    handleCancel() {
      this.$emit('cancel')
      this.show = false
    },
    hideLoading() {
      this.loading = false
    }
  }
}
</script>

<style lang="scss" scoped>
.bind-dialog-content {
  padding: 40rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .bind-icon {
    width: 120rpx;
    height: 120rpx;
    margin-bottom: 30rpx;
    
    image {
      width: 100%;
      height: 100%;
    }
  }
  
  .bind-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .bind-desc {
    font-size: 28rpx;
    color: #666;
    line-height: 40rpx;
    text-align: center;
    margin-bottom: 20rpx;
  }
  
  .bind-loading {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
    
    text {
      margin-left: 15rpx;
      font-size: 28rpx;
      color: #999;
    }
  }
}
</style>
