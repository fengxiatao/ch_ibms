<template>
  <view class="wrap">
    <view class="content">
      <u-form :model="form" ref="uForm" label-width="130">
        <view class="title">
          <image style="width: 150rpx; height: 150rpx; margin-bottom: 60rpx; border-radius: 50%;" mode="aspectFit" src="/static/images/无标题.jpg"></image>
          <text style="color: #ed6c60;">Hi Welcome</text>
        </view>
        <!-- 账号密码登录已隐藏，只使用微信登录 -->
        <!-- <u-form-item label="账号" prop="username">
          <u-input class="u-border-bottom" type="text" v-model="form.username" placeholder="请输入账号" />
        </u-form-item>
        <u-form-item label="密码" prop="password">
          <u-input class="u-border-bottom" type="password" v-model="form.password" placeholder="请输入密码" />
        </u-form-item>
        <u-form-item>
          <button :style="[inputStyle]" class="wechat item getCaptcha" @click="login" :loading="flower" style="background-color: #ed6c60;">登录</button>
        </u-form-item> -->
        <u-form-item>
          <button 
            class="wechat-quick-login item" 
            @click="wechatQuickLogin" 
            :loading="wechatLoginLoading" 
            :disabled="!agreementChecked"
            :style="{ backgroundColor: agreementChecked ? '#07c160' : '#cccccc', color: 'white' }">
            <text v-if="!wechatLoginLoading">微信快速登录</text>
          </button>
        </u-form-item>
      </u-form>

      <!-- 用户协议和隐私政策 -->
      <view class="agreement-section">
        <view class="agreement-checkbox" @click="toggleAgreement">
          <view class="checkbox-icon" :class="{ checked: agreementChecked }">
            <text v-if="agreementChecked">✓</text>
          </view>
          <view class="agreement-text">
            <text>我已阅读并同意</text>
            <text class="link" @click.stop="goToAgreement('user')">《用户服务协议》</text>
            <text>和</text>
            <text class="link" @click.stop="goToAgreement('privacy')">《隐私政策》</text>
          </view>
        </view>
      </view>

      <!-- 不提供忘记密码和注册功能，请使用微信快速登录 -->
    </view>
    <u-toast ref="uToast" />
  </view>
</template>

<script>
import url from "../../common/http/URL.js";
import store from "../../store/index.js";
import errorRecovery from "@/common/js/errorRecovery.js";
import wechatBindingService from "@/common/js/wechatBinding.js";

export default {
  data () {
    return {
      form: {
        username: '',
        password: '',
      },
      rules: {
        username: [{
          required: true,
          message: '请输入账号',
          trigger: ['change', 'blur']
        }],
        password: [{
          required: true,
          message: '请输入密码',
          trigger: ['change', 'blur']
        }]
      },
      userInfo: {},
      hasUserInfo: false,
      flower: false,
      flower2: false,
      wechatLoginLoading: false,
      agreementChecked: false  // 用户协议勾选状态
    }
  },
  onShow () {
    wx.hideHomeButton()
  },
  onReady () {
    this.$refs.uForm.setRules(this.rules)
  },
  onLoad () {
    let routes = getCurrentPages();
    let curRoute = routes[routes.length - 1].route
  },
  created () {
    let timestamp = Date.parse(new Date())
  },
  computed: {
    inputStyle () {
      let style = {};
      if (this.username) {
        style.color = "#fff";
        style.backgroundColor = '#1e3c72';
      }
      return style;
    }
  },
  methods: {
    uToast () {
      this.$refs.uToast.show({
        title: '登录失败,请检查网络',
        type: 'error'
      })
    },
    login () {
      this.$refs.uForm.validate(valid => {
        if (valid) {
          this.flower = true
          let params = {
            username: this.form.username,
            password: this.form.password
          }
          this.$u.api.POST(url.chargeLogin, params).then(res => {
            if (res.code == 200) {
              // Save payment intent before clearing storage (if any)
              const paymentIntent = errorRecovery.getPaymentIntent()
              
              // Clear old storage data
              uni.clearStorageSync()
              
              // Restore payment intent if it existed
              if (paymentIntent) {
                uni.setStorageSync('payment_intent', JSON.stringify(paymentIntent))
              }
              
              // Save token to storage (Requirements 3.1)
              uni.setStorageSync('token', res.data.token)
              
              // Save complete userInfo to storage
              uni.setStorageSync('userInfo', res.data)
              
              // Update Vuex store with userInfo (Requirements 3.4)
              this.$store.commit('SET_USERINFO', res.data)
              
              // If openid exists in response, update binding status
              if (res.data.openid) {
                this.$store.commit('SET_WECHAT_BOUND', true)
              }
              
              // Display success message
              uni.showToast({
                title: '登录成功',
                icon: 'success',
                duration: 1500
              })
              
              // Delayed navigation (Requirements 3.4)
              setTimeout(() => {
                this.flower = false
                
                // Check if there's a pending payment intent to recover
                if (errorRecovery.hasPendingPaymentIntent()) {
                  // Redirect to recharge page to recover payment
                  uni.reLaunch({
                    url: "/package/chargingPile/preTopUp"
                  })
                } else {
                  // Normal flow - redirect to index page
                  uni.reLaunch({
                    url: "/pages/index/index"
                  })
                }
              }, 1500)
            } else {
              this.flower = false
              uni.showToast({
                title: res.msg || '登录失败',
                icon: 'none',
                duration: 2000
              })
            }
          }).catch(err => {
            console.error('登录请求失败：', err)
            this.flower = false
            uni.showToast({
              title: '登录失败，请重试',
              icon: 'none',
              duration: 2000
            })
          })
        } else {
          // Validation failed
        }
      });
    },
    
    /**
     * 切换协议勾选状态
     */
    toggleAgreement() {
      this.agreementChecked = !this.agreementChecked
    },
    
    /**
     * 跳转到协议页面
     * @param {string} type - 协议类型: 'user' 用户协议, 'privacy' 隐私政策
     */
    goToAgreement(type) {
      let url = ''
      if (type === 'user') {
        // 用户服务协议
        url = '/pages/login/agreement/agreement?type=user'
      } else if (type === 'privacy') {
        // 隐私政策
        url = '/pages/login/agreement/agreement?type=privacy'
      }
      uni.navigateTo({ url })
    },
    
    /**
     * 微信快速登录
     */
    async wechatQuickLogin() {
      // 检查是否同意协议
      if (!this.agreementChecked) {
        uni.showToast({
          title: '请先阅读并同意用户协议和隐私政策',
          icon: 'none',
          duration: 2000
        })
        return
      }
      
      this.wechatLoginLoading = true
      
      try {
        const result = await wechatBindingService.wechatQuickLogin()
        
        if (result.success && result.bound) {
          // 登录成功（包括已有用户和自动创建的新用户）
          let toastTitle = '登录成功'
          if (result.isNewUser) {
            toastTitle = '自动注册成功'
          }
          
          uni.showToast({
            title: toastTitle,
            icon: 'success',
            duration: 1500
          })
          
          // 如果是新用户，显示账号信息
          if (result.isNewUser && result.userInfo) {
            setTimeout(() => {
              uni.showModal({
                title: '账号信息',
                content: `您的账号：${result.userInfo.username}\n初始密码：123456\n请及时修改密码`,
                showCancel: false,
                confirmText: '知道了',
                success: () => {
                  this.wechatLoginLoading = false
                  this.navigateAfterLogin()
                }
              })
            }, 1600)
          } else {
            setTimeout(() => {
              this.wechatLoginLoading = false
              this.navigateAfterLogin()
            }, 1500)
          }
        }
      } catch (error) {
        console.error('微信快速登录失败：', error)
        this.wechatLoginLoading = false
        
        uni.showToast({
          title: error.message || '微信登录失败，请重试',
          icon: 'none',
          duration: 2000
        })
      }
    },
    
    /**
     * 登录后导航
     */
    navigateAfterLogin() {
      // Check if there's a pending payment intent to recover
      if (errorRecovery.hasPendingPaymentIntent()) {
        // Redirect to recharge page to recover payment
        uni.reLaunch({
          url: "/package/chargingPile/preTopUp"
        })
      } else {
        // Normal flow - redirect to index page
        uni.reLaunch({
          url: "/pages/index/index"
        })
      }
    }
  },
};
</script>

<style lang="scss" scoped>
  /deep/.u-form-item--left__content::after {
    width: 1rpx;
    height: 30rpx;
    border-right: 6rpx solid #bebebe;
    content: " ";
  }

  /deep/.u-form-item--left__content {
    background: #f2f2f2;
    padding-left: 30rpx;
  }

  /deep/.u-input {
    background: #f2f2f2;
  }

  .wrap {
    font-size: 28rpx;
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    min-height: 100vh;

    .content {
      width: 600rpx;

      .title {
        display: flex;
        flex-direction: column;
        align-items: center;
        font-size: 40rpx;
        font-weight: 500;
        margin: 60rpx 0;
      }

      input {
        text-align: left;
        margin-bottom: 10rpx;
        padding-bottom: 6rpx;
      }

      .getCaptcha {
        background-color: #2980b9;
        color: #ffffff;
        border: none;
        font-size: 30rpx;
        padding: 12rpx 0;

        &::after {
          border: none;
        }
      }

      .wechat::after {
        display: none;
      }
      
      .wechat-quick-login {
        border: none;
        font-size: 30rpx;
        padding: 12rpx 0;
        margin-top: 20rpx;

        &::after {
          border: none;
        }
      }

      .alternative {
        color: $u-tips-color;
        display: flex;
        justify-content: space-between;
        margin-top: 30rpx;
      }
    }

    .buttom {
      .hint {
        padding: 80rpx 40rpx;
        font-size: 20rpx;
        color: $u-tips-color;
        text-align: center;

        .link {
          color: #a0cfff;
        }
      }
    }

    .btn1 {
      color: #ffffff;
      background-color: #6399fe;
      font-size: $uni-font-size-base;
      padding: 12rpx 0;
    }
    
    .agreement-section {
      margin-top: 40rpx;
      padding: 0 20rpx;
      
      .agreement-checkbox {
        display: flex;
        align-items: flex-start;
        
        .checkbox-icon {
          width: 36rpx;
          height: 36rpx;
          border: 2rpx solid #999;
          border-radius: 6rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12rpx;
          flex-shrink: 0;
          margin-top: 4rpx;
          
          &.checked {
            background-color: #07c160;
            border-color: #07c160;
            color: #fff;
          }
          
          text {
            font-size: 24rpx;
            font-weight: bold;
          }
        }
        
        .agreement-text {
          font-size: 24rpx;
          color: #666;
          line-height: 1.5;
          
          .link {
            color: #1890ff;
          }
        }
      }
    }
  }
</style>