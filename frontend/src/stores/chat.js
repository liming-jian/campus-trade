import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chatApi } from '@/api/chat'
import { useNotificationStore } from './notification'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref([])
  const currentMessages = ref([])
  const activeConversationId = ref(null)
  const unreadTotal = ref(0)
  let ws = null
  let reconnectTimer = null

  function connect() {
    const token = localStorage.getItem('token')
    if (!token || ws) return

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/ws/chat?token=${token}`

    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      if (reconnectTimer) clearTimeout(reconnectTimer)
    }

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        if (msg.type === 'read') {
          applyReadReceipt(msg)
          return
        }
        if (msg.type === 'warning' || msg.type === 'audit' || msg.type === 'verify') {
          const notifStore = useNotificationStore()
          notifStore.handleWsMessage(msg)
          return
        }
        handleIncomingMessage(msg)
      } catch (e) {
        console.error('WS message parse error', e)
      }
    }

    ws.onclose = () => {
      ws = null
      reconnectTimer = setTimeout(() => connect(), 3000)
    }
  }

  function disconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = null
    if (ws) {
      const socket = ws
      ws = null
      socket.onclose = null
      socket.close()
    }
  }

  function updateUnreadTotal() {
    unreadTotal.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  }

  function applyReadReceipt(receipt) {
    if (receipt.conversationId !== activeConversationId.value) return
    currentMessages.value = currentMessages.value.map(m => ({
      ...m,
      isRead: m.senderId !== receipt.readerId ? 1 : m.isRead
    }))
  }

  function handleIncomingMessage(msg) {
    if (msg.conversationId === activeConversationId.value) {
      const exists = currentMessages.value.some(m => m.id === msg.id)
      if (!exists) {
        currentMessages.value = [...currentMessages.value, msg]
      }
      markAsRead(msg.conversationId)
    }
    fetchConversations()
  }

  async function fetchConversations() {
    try {
      const res = await chatApi.getConversations()
      conversations.value = res.data
      updateUnreadTotal()
    } catch (e) {
      console.error('Failed to fetch conversations', e)
    }
  }

  async function fetchMessages(conversationId) {
    try {
      const res = await chatApi.getMessages(conversationId)
      currentMessages.value = res.data.reverse()
      activeConversationId.value = conversationId
      markAsRead(conversationId)
    } catch (e) {
      console.error('Failed to fetch messages', e)
    }
  }

  function markAsRead(conversationId) {
    const conv = conversations.value.find(c => c.conversationId === conversationId)
    if (conv) conv.unreadCount = 0
    updateUnreadTotal()

    chatApi.markAsRead(conversationId).catch(() => {})
  }

  async function sendMessage(conversationId, content) {
    if (!content.trim()) return
    try {
      const res = await chatApi.sendMessage({ conversationId, content })
      const msg = res.data
      const exists = currentMessages.value.some(m => m.id === msg.id)
      if (!exists) {
        currentMessages.value = [...currentMessages.value, msg]
      }
    } catch (e) {
      console.error('Failed to send message', e)
    }
  }

  async function createConversation(targetUserId) {
    try {
      const res = await chatApi.createConversation(targetUserId)
      return res.data
    } catch (e) {
      console.error('Failed to create conversation', e)
      return null
    }
  }

  return {
    conversations, currentMessages, activeConversationId, unreadTotal,
    connect, disconnect, fetchConversations, fetchMessages, sendMessage, createConversation
  }
})
