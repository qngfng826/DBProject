import request from './request'

export function addRating(data) {
  return request.post('/rating', data)
}

export function deleteRating(ratingId) {
  return request.delete(`/rating/${ratingId}`)
}

// 新增：通过电影ID删除当前用户的评分
export function deleteRatingByMovieId(movieId) {
  return request({
    url: `/rating/movie/${movieId}`,
    method: 'delete'
  })
}

export function getUserRating(movieId) {
  return request.get(`/rating/movie/${movieId}`)
}

export function getUserRatings() {
  return request.get('/rating/user')
}
