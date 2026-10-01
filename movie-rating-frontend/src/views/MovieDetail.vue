<template>
  <div class="page-container" v-loading="loading">
    <div v-if="movie">
      <MovieHeader
        :movie="movie"
        :directors="directors"
        :actors="actors"
        @updateMovie="fetchDetail"
      />

      <MovieCommentsSection
        :movieId="movieId"
        @updateComments="fetchDetail"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail } from '@/api/movie'

import MovieHeader from '@/components/MovieDetail/MovieHeader.vue'
import MovieCommentsSection from '@/components/MovieDetail/MovieCommentsSection.vue'

const route = useRoute()

const movie = ref(null)
const directors = ref([])
const actors = ref([])
const loading = ref(false)

const movieId = ref(null)

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
    movieId.value = id
    const res = await getMovieDetail(id)

    const movieData = res.data || {}
    movie.value = {
      ...movieData,
      Title: movieData.Title || movieData.title || '未知电影',
      ReleaseYear: movieData.ReleaseYear || movieData.releaseYear || '',
      Duration: movieData.Duration || movieData.duration || 0,
      Genre: movieData.Genre || movieData.genre || '未知',
      Language: movieData.Language || movieData.language || '未知',
      Country: movieData.Country || movieData.country || '未知',
      Synopsis: movieData.Synopsis || movieData.synopsis || '暂无简介',
      Rating: movieData.Rating || movieData.rating || 0,
      PosterUrl: movieData.PosterUrl || movieData.posterUrl || '',
      JumpUrl: movieData.JumpUrl || movieData.jumpUrl || null,
      UserRating: movieData.UserRating || movieData.userRating || null
    }

    directors.value = movieData.directors || movieData.Directors || []
    actors.value = movieData.actors || movieData.Actors || []

  } catch (e) {
    console.error('获取电影信息失败:', e) // 错误提示已由 request.js 拦截器统一弹出
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, () => {
  if (route.params.id) fetchDetail()
})

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
}
</style>
