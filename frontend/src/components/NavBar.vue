<template>
  <div class="navbar">
    <div class="navbar-left">
      <span v-if="showBack" class="back-btn" @click="goBack">返回</span>
      <span class="navbar-title">{{ title }}</span>
    </div>
    <div class="navbar-right">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: { type: String, default: '校园二手' },
  showBack: { type: Boolean, default: false }
})

const router = useRouter()
const goBack = () => router.back()
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  background: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
}
.navbar-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.back-btn {
  font-size: 14px;
  color: var(--color-primary);
  margin-right: 12px;
  cursor: pointer;
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
