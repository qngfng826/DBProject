import request from './request'

export function getMovieDetail(id) {
  return request.get(`/movie/${id}`)
}

export function searchMovies(params) {
  return request.get('/movie/search', { 
    params
  })
}

export function getHotMovies() {
  return request.get('/movie/hot')
}

export function addMovie(data) {
  return request.post('/movie', data)
}

export function updateMovie(id, data) {
  return request.put(`/movie/${id}`, data)
}

export function deleteMovie(id) {
  return request.delete(`/movie/${id}`)
}

export function getMovieComments(movieId) {
  return request.get(`/comment/movie/${movieId}`)
}

export function getGenreSummary() {
  return request.get('/report/genre-summary')
}
