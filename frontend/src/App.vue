<template>
  <router-view />
  <GlobalNotification />
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import GlobalNotification from '@/components/GlobalNotification.vue'

const userStore = useUserStore()
const chatStore = useChatStore()
const { token } = storeToRefs(userStore)

async function startRealtimeChat() {
  if (!token.value) return
  if (!userStore.user) {
    await userStore.fetchUser()
  }
  chatStore.connect()
  chatStore.fetchConversations()
}

onMounted(startRealtimeChat)

watch(token, (value) => {
  if (value) {
    startRealtimeChat()
  } else {
    chatStore.disconnect()
  }
})
</script>
