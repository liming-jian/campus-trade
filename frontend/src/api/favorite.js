import request from './request'

export const favoriteApi = {
  toggle(productId) {
    return request.post(`/favorites/${productId}`)
  },
  getList(params) {
    return request.get('/favorites', { params })
  }
}
