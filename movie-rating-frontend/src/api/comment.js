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

export function getMovieComments(movieId) {
  return request.get(`/comment/movie/${movieId}`)
}

export function getUserComments() {
  return request.get('/comment/user')
}

// === 后台评论管理 ===
export function getCommentList(params) {
  return request.get('/comment/search', { params })
}

export function adminUpdateComment(id, data) {
  return request.put(`/comment/admin/${id}`, data)
}

export function adminDeleteComment(id) {
  return request.delete(`/comment/admin/${id}`)
}
