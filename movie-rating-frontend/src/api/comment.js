import request from './request'

export function addComment(data) {
  return request.post('/comment', data)
}

export function updateComment(id, data) {
  return request.put(`/comment/${id}`, data)
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}

export function getUserComments() {
  return request.get(`/comment/user`)
}
