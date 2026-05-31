import request from './request'

export const orderApi = {
  create(data) {
    return request.post('/orders', data)
  },
  getList(params) {
    return request.get('/orders', { params })
  },
  getDetail(id) {
    return request.get(`/orders/${id}`)
  },
  pay(id) {
    return request.put(`/orders/${id}/pay`)
  },
  ship(id) {
    return request.put(`/orders/${id}/ship`)
  },
  confirm(id) {
    return request.put(`/orders/${id}/confirm`)
  },
  cancel(id, reason) {
    return request.put(`/orders/${id}/cancel`, { reason })
  }
}
