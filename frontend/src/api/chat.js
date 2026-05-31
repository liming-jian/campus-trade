import request from './request'

export const chatApi = {
  getConversations() {
    return request.get('/conversations')
  },
  getMessages(conversationId, params) {
    return request.get(`/conversations/${conversationId}/messages`, { params })
  },
  createConversation(targetUserId) {
    return request.post('/conversations', { targetUserId })
  },
  sendMessage(data) {
    return request.post('/messages', data)
  },
  markAsRead(conversationId) {
    return request.put(`/conversations/${conversationId}/read`)
  }
}
