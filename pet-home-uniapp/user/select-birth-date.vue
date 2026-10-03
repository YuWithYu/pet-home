<template>
  <view class="redirect-wrap" />
</template>

<script>
/**
 * 已移除「选择出生日期」流程；保留本页用于兼容旧版入口/缓存编译。
 * 进入后立即跳转到选择体重。
 */
export default {
  onLoad() {
    uni.redirectTo({
      url: '/user/select-weight',
      fail: () => {
        uni.navigateTo({ url: '/user/select-weight' })
      }
    })
  }
}
</script>

<style scoped>
.redirect-wrap {
  min-height: 100vh;
  background: #f8f8f8;
}
</style>
