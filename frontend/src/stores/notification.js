import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const warnings = ref([])
  const auditUpdates = ref([])
  const verifyUpdates = ref([])

  function handleWsMessage(msg) {
    if (msg.type === 'warning') {
      warnings.value.push({ title: msg.title, content: msg.content, time: Date.now() })
    } else if (msg.type === 'audit') {
      auditUpdates.value.push({ productId: msg.productId, title: msg.title, result: msg.result, reason: msg.reason, time: Date.now() })
    } else if (msg.type === 'verify') {
      verifyUpdates.value.push({ result: msg.result, reason: msg.reason, time: Date.now() })
    }
  }

  function clearWarning(index) {
    warnings.value.splice(index, 1)
  }

  return { warnings, auditUpdates, verifyUpdates, handleWsMessage, clearWarning }
})
