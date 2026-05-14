<template>
  <el-config-provider :locale="zhCnLocale">
    <div id="vue-admin-better">
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup>
import zhCnLocale from '@/locale/element-plus-zh-cn'
</script>
