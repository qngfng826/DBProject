<template>
  <div class="page-container" v-loading="loading">
    <div v-if="actor">
      <!-- 演员信息卡 -->
      <div class="person-header">
        <el-avatar :size="120" :src="actor.PhotoUrl" class="person-avatar-lg">
          {{ actor.Name?.charAt(0) }}
        </el-avatar>
        <div class="person-main">
          <h1>{{ actor.Name }}</h1>
          <div class="person-tags">
            <el-tag>{{ actor.Gender }}</el-tag>
            <el-tag type="info">{{ actor.Nationality }}</el-tag>
            <el-tag type="warning">{{ formatDate(actor.BirthDate) }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 参演电影 -->
      <div class="section-title">
        <el-icon><Film /></el-icon>
        参演电影
      </div>

      <div v-loading="moviesLoading">
        <div v-if="movies.length" class="movie-grid">
          <div v-for="m in movies" :key="m.MovieID" class="movie-card" @click="goMovieDetail(m.MovieID)">
            <img :src="m.PosterUrl || defaultPoster" :alt="m.Title" class="movie-poster" @error="handleImgError" />
            <div class="movie-info">
              <h3>{{ m.Title }}</h3>
              <div class="movie-meta">
                <span class="rating-stars">
                  <el-icon color="var(--accent)"><Star /></el-icon>
                  <span class="score">{{ m.Rating?.toFixed(1) }}</span>
                </span>
                <span>{{ m.ReleaseYear }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon><Film /></el-icon>
          <p>暂无参演电影数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Film, Star } from '@element-plus/icons-vue'
import { getActorDetail, getMoviesByActor } from '@/api/actor'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const actor = ref(null)
const movies = ref([])
const loading = ref(false)
const moviesLoading = ref(false)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300" fill="#30363d"><rect width="200" height="300"/><text x="100" y="150" text-anchor="middle" fill="#8b949e" font-size="14">暂无海报</text></svg>')

function formatDate(d) {
  return d ? dayjs(d).format('YYYY-MM-DD') : ''
}

function handleImgError(e) {
  e.target.src = defaultPoster
}

function goMovieDetail(id) {
  if (!id) {
    console.warn('电影ID缺失，无法跳转')
    return
  }
  router.push(`/movie/${id}`)
}

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getActorDetail(route.params.id)
    const actorData = res.data || {}
    actor.value = {
      ...actorData,
      Name: actorData.Name || actorData.name || '未知演员',
      Gender: actorData.Gender || actorData.gender || '男',
      Nationality: actorData.Nationality || actorData.nationality || '未知',
      BirthDate: actorData.BirthDate || actorData.birthDate || null,
      PhotoUrl: actorData.PhotoUrl || actorData.photoUrl || ''
    }

    moviesLoading.value = true
    try {
      const moviesRes = await getMoviesByActor(actor.value.Name)
      // 兼容电影数据字段，必须补充 MovieID 映射
      movies.value = (moviesRes.data || []).map(m => ({
        ...m,
        MovieID: m.MovieID || m.movieId, 
        Title: m.Title || m.title || '未知电影',
        ReleaseYear: m.ReleaseYear || m.releaseYear || '',
        Rating: m.Rating || m.rating || 0,
        PosterUrl: m.PosterUrl || m.posterUrl || ''
      }))
    } catch (e) {
      movies.value = []
    } finally {
      moviesLoading.value = false
    }
  } catch (e) {
    /* error handled */
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
.person-header { display: flex; gap: 32px; align-items: center; margin-bottom: 40px; padding: 32px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius); }
.person-avatar-lg { background: var(--accent); color: #000; font-size: 48px; font-weight: 800; }
.person-main h1 { font-size: 32px; font-weight: 800; color: var(--text-primary); margin-bottom: 16px; }
.person-tags { display: flex; gap: 8px; }
.movie-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 24px; }
.movie-card { cursor: pointer; transition: var(--transition); }
.movie-card:hover { transform: translateY(-4px); }
.movie-poster { width: 100%; aspect-ratio: 2 / 3; object-fit: cover; border-radius: var(--radius); background: var(--bg-secondary); }
.movie-info { padding: 12px 4px; }
.movie-info h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.movie-meta { display: flex; align-items: center; justify-content: space-between; font-size: 13px; color: var(--text-secondary); }
</style>
