<template>
  <view class="agreement-container">
    <view class="agreement-content">
      <view class="agreement-title">用户协议</view>
      <view class="agreement-date">生效日期：2025年12月9日</view>
      
      <view class="agreement-section">
        <view class="section-title">一、服务条款的接受</view>
        <view class="section-text">
          欢迎使用宠物家平台（以下简称"本平台"）。在使用本平台提供的服务前，请您仔细阅读本用户协议（以下简称"本协议"）。
          当您注册、登录、使用本平台服务时，即表示您已充分阅读、理解并同意接受本协议的全部内容。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">二、服务说明</view>
        <view class="section-text">
          1. 本平台为用户提供宠物相关的信息发布、服务预约、商品购买、社区交流等服务。
        </view>
        <view class="section-text">
          2. 本平台保留随时修改或中断服务而不需通知用户的权利。本平台行使修改或中断服务的权利，不需对用户或第三方负责。
        </view>
        <view class="section-text">
          3. 用户理解并同意，本平台不对因不可抗力、计算机病毒、黑客攻击、系统不稳定、用户所在位置、用户关机以及其他任何网络、技术、通信线路等原因造成的服务中断或不能满足用户要求承担责任。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">三、用户行为规范</view>
        <view class="section-text">
          1. 用户在使用本平台服务时，必须遵守国家相关法律法规，不得利用本平台从事违法违规活动。
        </view>
        <view class="section-text">
          2. 用户不得发布、传播含有以下内容的信息：
          （1）反对宪法所确定的基本原则的；
          （2）危害国家安全，泄露国家秘密，颠覆国家政权，破坏国家统一的；
          （3）损害国家荣誉和利益的；
          （4）煽动民族仇恨、民族歧视，破坏民族团结的；
          （5）破坏国家宗教政策，宣扬邪教和封建迷信的；
          （6）散布谣言，扰乱社会秩序，破坏社会稳定的；
          （7）散布淫秽、色情、赌博、暴力、凶杀、恐怖或者教唆犯罪的；
          （8）侮辱或者诽谤他人，侵害他人合法权益的；
          （9）含有法律、行政法规禁止的其他内容的。
        </view>
        <view class="section-text">
          3. 用户不得利用本平台进行任何商业活动，不得发布广告、垃圾信息等。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">四、知识产权</view>
        <view class="section-text">
          1. 本平台的所有知识产权，包括但不限于商标、专利、著作权、商业秘密等，均归本平台所有。
        </view>
        <view class="section-text">
          2. 用户在本平台上发布的内容，其知识产权归用户所有。用户授权本平台在全球范围内免费、非独家、可再许可地使用、复制、修改、改编、发布、翻译、分发、执行和展示用户发布的内容。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">五、隐私保护</view>
        <view class="section-text">
          本平台非常重视用户隐私的保护。关于用户隐私的保护，请详见《隐私条款》。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">六、免责声明</view>
        <view class="section-text">
          1. 本平台不对用户发布的内容的真实性、准确性、完整性负责。
        </view>
        <view class="section-text">
          2. 本平台不对因用户使用本平台服务而产生的任何直接、间接、偶然、特殊及后续的损害承担责任。
        </view>
        <view class="section-text">
          3. 本平台不对第三方服务提供商提供的服务承担责任。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">七、协议修改</view>
        <view class="section-text">
          本平台有权随时修改本协议。协议修改后，将在本平台上公布修改后的协议。如果用户不同意修改后的协议，可以停止使用本平台服务。
        </view>
      </view>
      
      <view class="agreement-section">
        <view class="section-title">八、法律适用</view>
        <view class="section-text">
          本协议的订立、执行和解释及争议的解决均应适用中华人民共和国法律。如双方就本协议内容或其执行发生任何争议，双方应尽量友好协商解决；协商不成时，任何一方均可向本平台所在地的人民法院提起诉讼。
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.agreement-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.agreement-content {
  padding: 16rpx;
}

.agreement-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 8rpx;
}

.agreement-date {
  font-size: 22rpx;
  color: #999;
  text-align: center;
  margin-bottom: 20rpx;
}

.agreement-section {
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
