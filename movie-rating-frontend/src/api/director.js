import request from './request'

export function getDirectorList(params) {
  return request.get('/director/search', { params })
}

export function getDirectorDetail(id) {
  return request.get(`/director/${id}`)
}

export function getMoviesByDirector(name) {
  return request.get(`/director/${name}/movies`)
}

export function addDirector(data) {
  return request.post('/director', data)
}

export function updateDirector(id, data) {
  return request.put(`/director/${id}`, data)
}

export function deleteDirector(id) {
  return request.delete(`/director/${id}`)
}
