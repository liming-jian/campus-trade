import request from './request'

export const adminApi = {
  getDashboard() {
    return request.get('/admin/dashboard')
  },
  getUsers(params) {
    return request.get('/admin/users', { params })
  },
  banUser(id, reason) {
    return request.post(`/admin/users/${id}/ban`, { reason })
  },
  unbanUser(id) {
    return request.post(`/admin/users/${id}/unban`)
  },
  warnUser(id, title, content) {
    return request.post(`/admin/users/${id}/warn`, { title, content })
  },
  getUserWarnings(id) {
    return request.get(`/admin/users/${id}/warnings`)
  },
  verifyUser(id, verifyStatus, reason) {
    return request.post(`/admin/users/${id}/verify`, { verifyStatus, reason })
  },
  getProducts(params) {
    return request.get('/admin/products', { params })
  },
  auditProduct(id, action, reason) {
    return request.post(`/admin/products/${id}/audit`, { action, reason })
  },
  getCategories() {
    return request.get('/categories')
  },
  createCategory(data) {
    return request.post('/categories', data)
  },
  updateCategory(id, data) {
    return request.put(`/categories/${id}`, data)
  },
  deleteCategory(id) {
    return request.delete(`/categories/${id}`)
  },
  getOrders(params) {
    return request.get('/admin/orders', { params })
  },
  getReports(params) {
    return request.get('/admin/reports', { params })
  },
  handleReport(id, action, result) {
    return request.post(`/admin/reports/${id}/handle`, { action, result })
  }
}
