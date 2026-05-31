<template>
  <div class="chat-page">
    <!-- Conv List -->
    <div v-if="!activeConv" class="conv-view">
      <div class="conv-header">
        <h2 class="conv-title">消息</h2>
      </div>
      <div v-if="conversations.length === 0" class="empty-state">
        <div class="empty-icon">M</div>
        <p>暂无消息</p>
        <p style="font-size:12px;color:var(--color-muted);">浏览商品时联系卖家开始聊天</p>
      </div>
      <div v-for="conv in conversations" :key="conv.conversationId" class="conv-item" @click="openChat(conv)">
        <el-avatar :size="48" :src="conv.targetAvatar"><el-icon :size="24"><UserFilled /></el-icon></el-avatar>
        <div class="conv-info">
          <div class="conv-top">
            <span class="conv-name">{{ conv.targetNickname || '同学' }}</span>
            <span class="conv-time">{{ fmtTime(conv.lastMessageAt) }}</span>
          </div>
          <div class="conv-bottom">
            <span class="conv-msg">{{ conv.lastMessage || '暂无消息' }}</span>
            <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Window -->
    <div v-else class="chat-window">
      <div class="chat-topbar">
        <span class="chat-back" @click="closeChat">返回</span>
        <span class="chat-name">{{ activeConv.targetNickname || '同学' }}</span>
        <span style="width:40px;"></span>
      </div>
      <div class="chat-msgs" ref="msgBox">
        <div v-for="msg in currentMessages" :key="msg.id" class="msg-row" :class="{ mine: isMine(msg) }">
          <div class="msg-bubble" :class="{ mine: isMine(msg) }">{{ msg.content }}</div>
          <div v-if="isMine(msg)" class="msg-read-status">{{ msg.isRead === 1 ? '已读' : '未读' }}</div>
        </div>
        <div ref="msgEnd"></div>
      </div>
      <div class="chat-send">
        <input v-model="inputText" class="chat-input" placeholder="输入消息..." @keyup.enter="sendMsg" />
        <button class="chat-send-btn" @click="sendMsg">发送</button>
      </div>
    </div>
    <TabBar v-if="!activeConv" />
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'

const chatStore = useChatStore()
const userStore = useUserStore()
const route = useRoute()
const { conversations, currentMessages } = storeToRefs(chatStore)
const activeConv = ref(null)
const inputText = ref('')
const msgBox = ref(null)
const msgEnd = ref(null)
const myId = computed(() => userStore.user?.id)

function isMine(msg) {
  return msg.senderId === myId.value
}

function fmtTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const now = new Date()
  const diff = now - d
  if (diff < 86400000) return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString()
}

async function openChat(conv) {
  activeConv.value = conv
  await chatStore.fetchMessages(conv.conversationId)
  await nextTick()
  msgEnd.value?.scrollIntoView({ behavior: 'smooth' })
}

async function openChatFromQuery() {
  const conversationId = Number(route.query.conversationId)
  if (!conversationId) return
  if (conversations.value.length === 0) {
    await chatStore.fetchConversations()
  }
  const conv = conversations.value.find(item => item.conversationId === conversationId)
  if (conv) {
    await openChat(conv)
  }
}

function closeChat() {
  activeConv.value = null
  chatStore.activeConversationId = null
}

async function sendMsg() {
  if (!inputText.value.trim()) return
  await chatStore.sendMessage(activeConv.value.conversationId, inputText.value.trim())
  inputText.value = ''
  await nextTick()
  msgEnd.value?.scrollIntoView({ behavior: 'smooth' })
}

watch(() => currentMessages.value?.length, () => {
  nextTick(() => msgEnd.value?.scrollIntoView({ behavior: 'smooth' }))
})

watch(() => route.query.conversationId, openChatFromQuery, { immediate: true })
</script>

<style scoped>
.chat-page { min-height: 100vh; background: var(--color-warm-white); }

/* Conv List */
.conv-view { padding-bottom: 70px; }
.conv-header { background: var(--color-ink); padding: 16px; }
.conv-title { color: white; font-family: var(--font-display); font-size: 20px; letter-spacing: 2px; margin: 0; }
.conv-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: white; border-bottom: 1px solid var(--color-border); cursor: pointer; }
.conv-item:active { background: var(--color-surface); }
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-weight: 700; }
.conv-time { font-size: 11px; color: var(--color-muted); }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-msg { font-size: 13px; color: var(--color-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 220px; }
.conv-badge { background: var(--color-hot); color: white; font-size: 10px; font-weight: 700; min-width: 18px; height: 18px; line-height: 18px; text-align: center; border-radius: 9px; padding: 0 4px; }

/* Chat Window */
.chat-window { display: flex; flex-direction: column; height: 100vh; }
.chat-topbar { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; background: var(--color-ink); color: white; }
.chat-back { font-size: 14px; cursor: pointer; color: var(--color-accent); }
.chat-name { font-family: var(--font-display); font-size: 16px; }
.chat-msgs { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 8px; }
.msg-row { display: flex; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 75%; padding: 10px 14px; border-radius: 18px; font-size: 14px; line-height: 1.5; word-break: break-all; background: white; border: 1.5px solid var(--color-border); }
.msg-bubble.mine { background: var(--color-primary); color: white; border-color: var(--color-ink); }
.msg-read-status { font-size: 10px; color: var(--color-muted); text-align: right; margin-top: 2px; margin-bottom: 4px; padding-right: 4px; }
.chat-send { display: flex; gap: 8px; padding: 10px 12px; background: white; border-top: 2px solid var(--color-border); }
.chat-input { flex: 1; padding: 10px 16px; border: 2px solid var(--color-border); border-radius: 50px; font-size: 14px; outline: none; background: var(--color-warm-white); }
.chat-input:focus { border-color: var(--color-primary); }
.chat-send-btn { padding: 10px 20px; background: var(--color-primary); color: white; border: 2px solid var(--color-ink); border-radius: 50px; font-family: var(--font-display); font-weight: 700; cursor: pointer; }
.chat-send-btn:active { transform: scale(0.96); }

.empty-icon { width: 64px; height: 64px; border-radius: 50%; background: var(--color-surface); border: 3px dashed var(--color-border); display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 24px; color: var(--color-muted); margin-bottom: 12px; }
</style>
