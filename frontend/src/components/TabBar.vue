<template>
  <nav class="tabbar">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="tabbar-item"
      :class="{ active: isActive(tab.path) }"
    >
      <div class="tab-icon-wrap">
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
        <span v-if="tab.path === '/chat' && badgeCount > 0" class="tab-badge">{{ badgeCount > 99 ? '99' : badgeCount }}</span>
      </div>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import { useChatStore } from '@/stores/chat'

const props = defineProps({ badge: { type: Number, default: 0 } })
const route = useRoute()
const chatStore = useChatStore()
const { unreadTotal } = storeToRefs(chatStore)
const badgeCount = computed(() => props.badge || unreadTotal.value)

const tabs = [
  { path: '/home', label: '首页', icon: 'House' },
  { path: '/chat', label: '消息', icon: 'ChatDotRound' },
  { path: '/publish', label: '发布', icon: 'CirclePlus' },
  { path: '/orders', label: '订单', icon: 'Document' },
  { path: '/profile', label: '我的', icon: 'User' }
]

const isActive = (path) => route.path === path || (path === '/home' && route.path === '/')
</script>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 60px;
  background: white;
  border-top: 2px solid var(--color-ink);
  padding-bottom: env(safe-area-inset-bottom, 0);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-decoration: none;
  color: var(--color-muted);
  padding: 4px 14px;
  transition: all 0.15s;
  position: relative;
}

.tabbar-item.active {
  color: var(--color-ink);
}

.tab-icon-wrap {
  position: relative;
  transition: transform 0.2s;
}
.tabbar-item.active .tab-icon-wrap {
  transform: translateY(-2px);
}

.tab-label {
  font-size: 10px;
  font-weight: 600;
}

.tab-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--color-hot);
  color: white;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 4px;
  border: 1.5px solid white;
}

@media (min-width: 768px) {
  .tabbar {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
