<template>
  <view class="privacy-container">
    <view class="privacy-content">
      <view class="privacy-title">隐私条款</view>
      <view class="privacy-date">生效日期：2025年12月9日</view>
      
      <view class="privacy-section">
        <view class="section-title">一、信息收集</view>
        <view class="section-text">
          为了向您提供更好的服务，我们可能会收集以下信息：
        </view>
        <view class="section-text">
          1. 您主动提供的信息：包括但不限于注册信息、个人资料、宠物信息、订单信息等。
        </view>
        <view class="section-text">
          2. 自动收集的信息：包括但不限于设备信息、日志信息、位置信息等。
        </view>
        <view class="section-text">
          3. 第三方提供的信息：包括但不限于微信登录信息、支付信息等。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">二、信息使用</view>
        <view class="section-text">
          我们可能将收集的信息用于以下目的：
        </view>
        <view class="section-text">
          1. 向您提供、维护、改进我们的服务。
        </view>
        <view class="section-text">
          2. 向您发送服务通知、营销信息等（您可以选择不接收）。
        </view>
        <view class="section-text">
          3. 进行数据分析，以改进我们的产品和服务。
        </view>
        <view class="section-text">
          4. 保护我们的合法权益，防止欺诈、滥用等行为。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">三、信息共享</view>
        <view class="section-text">
          我们不会向第三方出售、出租或以其他方式披露您的个人信息，但以下情况除外：
        </view>
        <view class="section-text">
          1. 获得您的明确同意。
        </view>
        <view class="section-text">
          2. 法律法规要求或司法机关、行政机关依法要求披露的。
        </view>
        <view class="section-text">
          3. 为提供服务所必需，向我们的合作伙伴、服务提供商披露（我们会要求他们保护您的个人信息）。
        </view>
        <view class="section-text">
          4. 在紧急情况下，为保护用户或公众的生命、财产安全。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">四、信息安全</view>
        <view class="section-text">
          我们采用行业标准的安全措施来保护您的个人信息，包括但不限于：
        </view>
        <view class="section-text">
          1. 数据加密传输和存储。
        </view>
        <view class="section-text">
          2. 访问控制和权限管理。
        </view>
        <view class="section-text">
          3. 定期安全审计和漏洞修复。
        </view>
        <view class="section-text">
          尽管我们采取了合理的安全措施，但请注意，任何安全措施都无法做到绝对安全。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">五、您的权利</view>
        <view class="section-text">
          您对自己的个人信息享有以下权利：
        </view>
        <view class="section-text">
          1. 访问权：您可以随时查看您的个人信息。
        </view>
        <view class="section-text">
          2. 更正权：您可以更正您的个人信息。
        </view>
        <view class="section-text">
          3. 删除权：您可以要求我们删除您的个人信息（法律法规要求保留的除外）。
        </view>
        <view class="section-text">
          4. 撤回同意权：您可以撤回您对个人信息处理的同意。
        </view>
        <view class="section-text">
          5. 注销权：您可以注销您的账号，注销后我们将删除您的个人信息（法律法规要求保留的除外）。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">六、Cookie和类似技术</view>
        <view class="section-text">
          我们可能使用Cookie和类似技术来收集信息、改善用户体验。您可以通过浏览器设置拒绝Cookie，但这可能影响某些功能的正常使用。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">七、未成年人保护</view>
        <view class="section-text">
          我们非常重视对未成年人个人信息的保护。如果您是未成年人，请在您的监护人同意和指导下使用我们的服务。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">八、隐私条款的更新</view>
        <view class="section-text">
          我们可能会不时更新本隐私条款。更新后，我们会在本平台上公布更新后的隐私条款。如果更新涉及重大变更，我们会通过适当方式通知您。
        </view>
      </view>
      
      <view class="privacy-section">
        <view class="section-title">九、联系我们</view>
        <view class="section-text">
          如果您对本隐私条款有任何疑问、意见或建议，或需要行使您的权利，请通过以下方式联系我们：
        </view>
        <view class="section-text">
          邮箱：csiyu8596@gmail.com
        </view>
        <view class="section-text">
          电话：15278562765
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.privacy-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.privacy-content {
  padding: 16rpx;
}

.privacy-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 8rpx;
}

.privacy-date {
  font-size: 22rpx;
  color: #999;
  text-align: center;
  margin-bottom: 20rpx;
}

.privacy-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.section-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}
</style>
