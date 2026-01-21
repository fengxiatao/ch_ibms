<template>
  <view class="agreement-container">
    <scroll-view scroll-y class="agreement-scroll">
      <!-- 用户服务协议 -->
      <view v-if="type === 'user'" class="agreement-content">
        <view class="agreement-title">用户服务协议</view>
        <view class="update-time">更新日期：2026年1月1日</view>
        
        <view class="section">
          <view class="section-title">一、服务条款的确认和接纳</view>
          <view class="section-content">
            欢迎使用本充电桩小程序（以下简称"本服务"）。请您在使用本服务前，仔细阅读并充分理解本协议各条款。您使用本服务即表示您已阅读并同意受本协议的约束。
          </view>
        </view>

        <view class="section">
          <view class="section-title">二、服务内容</view>
          <view class="section-content">
            1. 本服务为用户提供电动车充电服务，包括但不限于查找充电桩、启动充电、结束充电、支付充电费用等功能。
            <text>\n</text>
            2. 用户需通过微信授权登录使用本服务，登录后可享受完整的充电服务功能。
            <text>\n</text>
            3. 本服务保留随时修改或中断服务的权利，无需另行通知用户。
          </view>
        </view>

        <view class="section">
          <view class="section-title">三、用户账号</view>
          <view class="section-content">
            1. 用户通过微信授权登录后，系统将自动为用户创建账号。
            <text>\n</text>
            2. 用户应妥善保管账号信息，因账号保管不善造成的损失由用户自行承担。
            <text>\n</text>
            3. 用户不得将账号转让、出借给他人使用。
          </view>
        </view>

        <view class="section">
          <view class="section-title">四、用户权利与义务</view>
          <view class="section-content">
            1. 用户有权使用本服务提供的各项充电功能。
            <text>\n</text>
            2. 用户应遵守相关法律法规，不得利用本服务从事违法违规活动。
            <text>\n</text>
            3. 用户应按照实际使用情况支付充电费用。
            <text>\n</text>
            4. 用户应爱护充电设备，如因人为原因造成设备损坏，用户应承担相应赔偿责任。
          </view>
        </view>

        <view class="section">
          <view class="section-title">五、服务变更与终止</view>
          <view class="section-content">
            1. 本服务有权根据业务发展需要，变更服务内容或终止服务。
            <text>\n</text>
            2. 如用户违反本协议规定，本服务有权暂停或终止向该用户提供服务。
          </view>
        </view>

        <view class="section">
          <view class="section-title">六、免责声明</view>
          <view class="section-content">
            1. 因不可抗力或本服务无法控制的原因造成的服务中断，本服务不承担责任。
            <text>\n</text>
            2. 用户在使用充电服务过程中，应注意用电安全，因用户自身原因造成的人身或财产损失，本服务不承担责任。
          </view>
        </view>

        <view class="section">
          <view class="section-title">七、协议修改</view>
          <view class="section-content">
            本服务保留随时修改本协议的权利。修改后的协议一经公布即生效，用户继续使用本服务即视为接受修改后的协议。
          </view>
        </view>

        <view class="section">
          <view class="section-title">八、联系方式</view>
          <view class="section-content">
            如您对本协议有任何疑问，请通过小程序内的"帮助"功能联系我们。
          </view>
        </view>
      </view>

      <!-- 隐私政策 -->
      <view v-else-if="type === 'privacy'" class="agreement-content">
        <view class="agreement-title">隐私政策</view>
        <view class="update-time">更新日期：2026年1月1日</view>
        
        <view class="section">
          <view class="section-title">引言</view>
          <view class="section-content">
            本充电桩小程序（以下简称"我们"）深知个人信息对您的重要性，我们将按照法律法规的规定，保护您的个人信息及隐私安全。我们制定本隐私政策，向您说明我们如何收集、使用、存储、保护您的个人信息。
            <text>\n\n</text>
            请您在使用我们的服务前，仔细阅读并充分理解本隐私政策的全部内容。
          </view>
        </view>

        <view class="section">
          <view class="section-title">一、我们收集的信息</view>
          <view class="section-content">
            为了向您提供服务，我们需要收集以下信息：
            <text>\n\n</text>
            <text class="bold">1. 微信账号信息</text>
            <text>\n</text>
            当您使用微信登录时，我们会获取您的微信OpenID，用于识别您的身份和关联您的账户。
            <text>\n\n</text>
            <text class="bold">2. 充电记录信息</text>
            <text>\n</text>
            包括您的充电时间、充电时长、充电电量、充电费用等信息，用于为您提供充电服务和历史记录查询。
            <text>\n\n</text>
            <text class="bold">3. 支付信息</text>
            <text>\n</text>
            当您使用微信支付进行充值或支付时，我们会记录您的支付金额和交易时间，但我们不会存储您的银行卡、密码等敏感支付信息。
            <text>\n\n</text>
            <text class="bold">4. 设备信息</text>
            <text>\n</text>
            为了保障服务安全和优化用户体验，我们可能会收集您的设备型号、操作系统版本等基本设备信息。
          </view>
        </view>

        <view class="section">
          <view class="section-title">二、信息的使用目的</view>
          <view class="section-content">
            我们收集的信息将用于以下目的：
            <text>\n\n</text>
            1. <text class="bold">提供充电服务</text>：识别您的身份，为您提供充电桩查找、启动充电、结束充电等服务。
            <text>\n</text>
            2. <text class="bold">账户管理</text>：管理您的账户余额、充电记录、优惠券等信息。
            <text>\n</text>
            3. <text class="bold">客户服务</text>：处理您的咨询、投诉和建议。
            <text>\n</text>
            4. <text class="bold">安全保障</text>：保护您的账户安全，预防和处理欺诈行为。
            <text>\n</text>
            5. <text class="bold">服务优化</text>：分析服务使用情况，改进和优化我们的服务。
          </view>
        </view>

        <view class="section">
          <view class="section-title">三、信息的存储与保护</view>
          <view class="section-content">
            1. <text class="bold">存储位置</text>：您的个人信息存储于中华人民共和国境内的服务器。
            <text>\n\n</text>
            2. <text class="bold">存储期限</text>：我们只会在实现本政策所述目的所需的期限内保留您的个人信息，除非法律法规另有要求。
            <text>\n\n</text>
            3. <text class="bold">安全措施</text>：我们采取了符合业界标准的安全措施保护您的个人信息，包括数据加密、访问控制等技术手段。
          </view>
        </view>

        <view class="section">
          <view class="section-title">四、信息的共享与披露</view>
          <view class="section-content">
            我们不会向第三方出售您的个人信息。但在以下情况下，我们可能会共享您的信息：
            <text>\n\n</text>
            1. 获得您的明确同意后。
            <text>\n</text>
            2. 根据法律法规的规定或政府主管部门的要求。
            <text>\n</text>
            3. 为完成支付功能，需要与微信支付平台共享必要的交易信息。
          </view>
        </view>

        <view class="section">
          <view class="section-title">五、您的权利</view>
          <view class="section-content">
            您对您的个人信息享有以下权利：
            <text>\n\n</text>
            1. <text class="bold">查询权</text>：您可以查询您的充电记录、账户余额等信息。
            <text>\n</text>
            2. <text class="bold">更正权</text>：如发现您的个人信息有误，您可以联系我们进行更正。
            <text>\n</text>
            3. <text class="bold">删除权</text>：在符合法律规定的情况下，您可以要求我们删除您的个人信息。
            <text>\n</text>
            4. <text class="bold">撤回同意权</text>：您可以通过取消关注小程序等方式撤回您的授权同意。
          </view>
        </view>

        <view class="section">
          <view class="section-title">六、未成年人保护</view>
          <view class="section-content">
            我们的服务主要面向成年人。如果您是未成年人，请在法定监护人的陪同和同意下使用我们的服务。
          </view>
        </view>

        <view class="section">
          <view class="section-title">七、隐私政策的更新</view>
          <view class="section-content">
            我们可能会适时修订本隐私政策。当隐私政策发生重大变更时，我们会在小程序内以适当方式通知您。
          </view>
        </view>

        <view class="section">
          <view class="section-title">八、联系我们</view>
          <view class="section-content">
            如您对本隐私政策有任何疑问、意见或建议，请通过小程序内的"帮助"功能联系我们，我们将在15个工作日内回复您。
          </view>
        </view>
      </view>

      <!-- 默认内容 -->
      <view v-else class="agreement-content">
        <view class="empty-tip">协议内容加载中...</view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      type: 'user'  // 协议类型：user-用户协议，privacy-隐私政策
    };
  },
  onLoad(options) {
    this.type = options.type || 'user'
    // 设置页面标题
    if (this.type === 'privacy') {
      uni.setNavigationBarTitle({
        title: '隐私政策'
      })
    } else {
      uni.setNavigationBarTitle({
        title: '用户服务协议'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.agreement-container {
  width: 100%;
  height: 100vh;
  background-color: #f8f8f8;
}

.agreement-scroll {
  width: 100%;
  height: 100%;
}

.agreement-content {
  padding: 30rpx;
  background-color: #fff;
  min-height: 100%;
}

.agreement-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 20rpx;
}

.update-time {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  margin-bottom: 40rpx;
  padding-bottom: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.section-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  text-align: justify;
  
  .bold {
    font-weight: bold;
    color: #333;
  }
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 100rpx 0;
}
</style>
