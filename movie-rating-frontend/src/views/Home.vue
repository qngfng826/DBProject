<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">发现好电影</h1>
        <p class="hero-subtitle">探索经典与新作，分享你的观影感受</p>
        <div class="hero-search">
          <el-input v-model="searchKeyword" placeholder="搜索电影名称..." size="large" :prefix-icon="Search" @keyup.enter="handleSearch" />
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </section>

    <!-- 热门推荐 -->
    <section class="page-container">
      <div class="section-title">
        <el-icon color="var(--accent)"><Star /></el-icon>
        热门推荐
      </div>
      <div v-loading="loading">
        <div v-if="hotMovies.length" class="movie-grid">
          <div v-for="movie in hotMovies" :key="movie.movieId" class="movie-card" @click="goDetail(movie.movieId)">
            <div class="poster-wrapper">
              <img :src="movie.posterUrl || defaultPoster" :alt="movie.title" class="movie-poster" loading="lazy" @error="handleImgError" />
              <div class="poster-overlay">
                <el-icon :size="32"><View /></el-icon>
                <span>查看详情</span>
              </div>
            </div>
          <div class="movie-info">
            <h3 class="movie-title">{{ movie.title }}</h3>
            <div class="movie-meta">
              <span class="rating-stars">
                <el-icon color="var(--accent)"><Star /></el-icon>
                <span class="score">{{ movie.rating?.toFixed(1) || '0.0' }}</span>
              </span>
              <span class="comments-count">
                <el-icon><ChatDotRound /></el-icon>
                {{ movie.CommentCount || 0 }}
              </span>
            </div>
          </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon><Film /></el-icon>
          <p>暂无热门电影</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Star, View, ChatDotRound, Film } from '@element-plus/icons-vue'
import { getHotMovies } from '@/api/movie'

const router = useRouter()
const searchKeyword = ref('')
const hotMovies = ref([])
const loading = ref(false)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300" fill="#30363d"><rect width="200" height="300"/><text x="100" y="150" text-anchor="middle" fill="#8b949e" font-size="14">暂无海报</text></svg>')

function handleImgError(e) {
  e.target.src = defaultPoster
}

function goDetail(id) {
  if (!id) {
    console.error('Movie ID is empty, cannot navigate')
    return
  }
  console.log('Navigating to movie detail:', `/movie/${id}`)
  router.push(`/movie/${id}`)
}

function handleSearch() {
  router.push({ path: '/movies', query: { keyword: searchKeyword.value } })
}

async function fetchHotMovies() {
  loading.value = true
  try {
    const res = await getHotMovies()
    let list = []
    if (Array.isArray(res.data)) {
      list = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      list = res.data.records
    } else if (res.data && Array.isArray(res.data.list)) {
      list = res.data.list
    }
    console.log('Hot movies data:', list)
    hotMovies.value = list.map(m => ({
      ...m,
      movieId: m.movieId,
      title: m.title || '未知电影',
      rating: m.rating || 0,
      posterUrl: m.posterUrl || '',
      CommentCount: m.CommentCount || 0
    }))
    console.log('Mapped movies:', hotMovies.value)
  } catch (e) {
    console.error('Error fetching hot movies:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotMovies()
})
</script>

<style scoped>
.hero {
  position: relative;
  height: 400px;
  background: url('https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1920&q=80') center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(13,17,23,0.9) 0%, rgba(13,17,23,0.6) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  width: 100%;
  max-width: 600px;
  padding: 0 24px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.hero-search {
  display: flex;
  gap: 12px;
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

.poster-wrapper {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  aspect-ratio: 2 / 3;
  background: var(--bg-secondary);
}

.movie-poster {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.poster-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--accent);
  font-size: 14px;
  opacity: 0;
  transition: var(--transition);
}

.movie-card:hover .poster-overlay {
  opacity: 1;
}

.movie-info {
  padding: 12px 4px;
}

.movie-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
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

.movie-meta .el-icon {
  margin-right: 4px;
}

.comments-count {
  display: flex;
  align-items: center;
}
</style>
