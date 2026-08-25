<template>
  <div class="page-container" v-loading="loading">
    <div v-if="director">
      <!-- 导演信息卡 -->
      <div class="person-header">
        <el-avatar :size="120" class="person-avatar-lg">
          {{ director.Name?.charAt(0) }}
        </el-avatar>
        <div class="person-main">
          <h1>{{ director.Name }}</h1>
          <div class="person-tags">
            <el-tag>{{ director.Gender }}</el-tag>
            <el-tag type="info">{{ director.Nationality }}</el-tag>
            <el-tag type="warning">{{ formatDate(director.BirthDate) }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 执导电影 -->
      <div class="section-title">
        <el-icon><Film /></el-icon>
        执导电影
      </div>

      <div v-loading="moviesLoading">
        <div v-if="movies.length" class="movie-grid">
          <div v-for="m in movies" :key="m.MovieID" class="movie-card" @click="goMovie(m.MovieID)">
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
          <p>暂无执导电影数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Film, Star } from '@element-plus/icons-vue'
import { getDirectorDetail, getMoviesByDirector } from '@/api/director'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const director = ref(null)
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

function goMovie(id) {
  if (!id) {
    console.warn('电影ID不存在，无法跳转')
    return
  }
  router.push(`/movie/${id}`)
}

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getDirectorDetail(route.params.id)
    const directorData = res.data || {}
    director.value = {
      ...directorData,
      Name: directorData.Name || directorData.name || '未知导演',
      Gender: directorData.Gender || directorData.gender || '男',
      Nationality: directorData.Nationality || directorData.nationality || '未知',
      BirthDate: directorData.BirthDate || directorData.birthDate || null
    }
    
    moviesLoading.value = true
    try {
      const moviesRes = await getMoviesByDirector(director.value.Name)
      // 兼容电影数据字段
      movies.value = (moviesRes.data || []).map(m => ({
        ...m,
        MovieID: m.MovieID || m.movieId || m.id,
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
    // Handle error if needed
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
.person-header {
  display: flex;
  gap: 32px;
  align-items: center;
  margin-bottom: 40px;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
}

.person-avatar-lg {
  background: var(--accent);
  color: #000;
  font-size: 48px;
  font-weight: 800;
}

.person-main h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.person-tags {
  display: flex;
  gap: 8px;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
}

.movie-card {
  cursor: pointer;
  transition: var(--transition);
}

.movie-card:hover {
  transform: translateY(-4px);
}

.movie-poster {
  width: 100%;
  aspect-ratio: 2 / 3;
  object-fit: cover;
  border-radius: var(--radius);
  background: var(--bg-secondary);
}

.movie-info {
  padding: 12px 4px;
}

.movie-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.movie-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
