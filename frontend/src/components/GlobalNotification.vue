<template>
  <teleport to="body">
    <!-- Warning Modal -->
    <div v-if="currentWarning" class="warning-overlay" @click.self="dismiss">
      <div class="warning-modal">
        <div class="warning-icon">!</div>
        <h3 class="warning-title">{{ currentWarning.title }}</h3>
        <p class="warning-content">{{ currentWarning.content }}</p>
        <button class="warning-btn" @click="dismiss">我知道了</button>
      </div>
    </div>

    <!-- Audit toast -->
    <transition-group name="toast-slide" tag="div" class="toast-container">
      <div v-for="(item, i) in auditToasts" :key="item.time" class="audit-toast" :class="'audit-' + item.result">
        <span class="toast-icon">{{ item.result === 'PASS' ? '✓' : item.result === 'REJECT' ? '✗' : '↓' }}</span>
        <span class="toast-text">
          {{ item.result === 'PASS' ? '商品已通过审核' : item.result === 'REJECT' ? '商品审核未通过' : '商品已被下架' }}
          - {{ item.title }}
        </span>
      </div>
    </transition-group>
  </teleport>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useNotificationStore } from '@/stores/notification'

const notifStore = useNotificationStore()

const currentWarning = computed(() => notifStore.warnings[0] || null)
const auditToasts = computed(() => notifStore.auditUpdates.slice(-3))

function dismiss() {
  notifStore.clearWarning(0)
}

watch(() => notifStore.auditUpdates.length, () => {
  setTimeout(() => {
    if (notifStore.auditUpdates.length > 0) {
      notifStore.auditUpdates.shift()
    }
  }, 5000)
})
</script>

<style scoped>
.warning-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.warning-modal {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  max-width: 340px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.warning-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #FFF3E0;
  color: #FF6B00;
  font-size: 28px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.warning-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1a1a1a;
}
.warning-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
}
.warning-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 50px;
  background: #1a1a1a;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.warning-btn:active { opacity: 0.8; }

.toast-container {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 9998;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.audit-toast {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  min-width: 240px;
}
.audit-PASS { background: #E8F5E9; color: #2E7D32; }
.audit-REJECT { background: #FFEBEE; color: #C62828; }
.audit-OFF { background: #FFF3E0; color: #E65100; }
.toast-icon { font-size: 16px; }

.toast-slide-enter-active { transition: all 0.3s ease; }
.toast-slide-leave-active { transition: all 0.3s ease; }
.toast-slide-enter-from { transform: translateX(100%); opacity: 0; }
.toast-slide-leave-to { transform: translateX(100%); opacity: 0; }
</style>
