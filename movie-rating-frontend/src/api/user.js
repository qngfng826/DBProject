import request from './request'

export function getUserList(params) {
  return request.get('/user/search', { params })
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
