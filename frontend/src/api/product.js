import request from './request'

export const productApi = {
  getList(params) {
    return request.get('/products', { params })
  },
  getDetail(id) {
    return request.get(`/products/${id}`)
  },
  create(data, images) {
    const formData = new FormData()
    formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    if (images) {
      images.forEach((img) => formData.append('images', img))
    }
    return request.post('/products', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  update(id, data) {
    return request.put(`/products/${id}`, data)
  },
  delete(id) {
    return request.delete(`/products/${id}`)
  },
  getMyProducts(params) {
    return request.get('/products/my', { params })
  },
  getMyReports(params) {
    return request.get('/products/reports/my', { params })
  },
  report(id, reason) {
    return request.post(`/products/${id}/report`, { reason })
  }
}
