<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ref } from 'vue'
import en from 'element-plus/es/locale/lang/en'

const locale = ref(en)
</script>

<style>
#app {
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
