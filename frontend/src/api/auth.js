import request from './request'

export const authApi = {
  sendCode(data) {
    return request.post('/auth/send-code', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  login(phone, password) {
    return request.post('/auth/login', { phone, password })
  },
  getMe() {
    return request.get('/auth/me')
  },
  updateProfile(data) {
    return request.put('/auth/profile', data)
  },
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.put('/auth/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  verifyStudent(formData) {
    return request.post('/auth/verify', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  resetPassword(data) {
    return request.post('/auth/reset-password', data)
  },
  adminLogin(username, password) {
    return request.post('/admin/login', { username, password })
  }
}
