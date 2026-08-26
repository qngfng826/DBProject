import request from './request'

export function getActorList(params) {
  return request.get('/actor/search', { params })
}

export function getActorDetail(id) {
  return request.get(`/actor/${id}`)
}

export function getMoviesByActor(name) {
  return request.get(`/actor/${name}/movies`)
}

export function addActor(data) {
  return request.post('/actor', data)
}

export function updateActor(id, data) {
  return request.put(`/actor/${id}`, data)
}

export function deleteActor(id) {
  return request.delete(`/actor/${id}`)
}
