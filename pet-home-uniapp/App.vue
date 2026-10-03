<template>
  <view class="app">
    <!-- 路由出口 -->
    <router-view />
  </view>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style lang="scss">
/* App.vue */
@import "common/css/common.scss";

.app {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}
</style>
