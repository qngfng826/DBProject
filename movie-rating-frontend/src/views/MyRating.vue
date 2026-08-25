<template>
  <div class="page-container">
    <div class="section-title">
      <el-icon><Star /></el-icon>
      我的评分
    </div>

    <div v-loading="loading">
      <div v-if="ratings.length" class="rating-list">
        <div v-for="r in ratings" :key="r.ratingId" class="rating-item" @click="goMovie(r.movieId)">
          <img :src="r.posterUrl || defaultPoster" class="movie-thumb" @error="handleImgError" />
          <div class="rating-info">
            <h3>{{ r.title }}</h3>
            <p class="movie-year">{{ r.releaseYear }} · {{ r.genre }}</p>
            <el-rate :model-value="r.score" disabled :max="10" size="small" />
          </div>
          <div class="rating-meta">
            <span class="score-display">{{ r.score }} 分</span>
            <span class="rate-time">{{ formatTime(r.ratingTime) }}</span>
            <el-button text type="danger" size="small" @click.stop="handleDelete(r)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <el-icon><Star /></el-icon>
        <p>你还没有评分过任何电影</p>
        <el-button type="primary" @click="$router.push('/movies')" style="margin-top: 16px">去逛逛电影</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star } from '@element-plus/icons-vue'
import { getUserRatings } from '@/api/rating'
import { deleteRating } from '@/api/rating'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const ratings = ref([])
const loading = ref(false)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="100" height="150" fill="#30363d"><rect width="100" height="150"/><text x="50" y="75" text-anchor="middle" fill="#8b949e" font-size="10">暂无</text></svg>')

function handleImgError(e) {
  e.target.src = defaultPoster
}

function formatTime(t) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

function goMovie(id) {
  router.push(`/movie/${id}`)
}

async function handleDelete(r) {
  const id = r.ratingId
  if (!id) {
    ElMessage.error('后端未返回评分ID，请检查后端Mapper和实体类')
    console.error("当前对象缺少 ratingId，实际数据为：", r)
    return
  }
    try {
        await ElMessageBox.confirm('确定要删除这条评分吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await deleteRating(id) // 假设封装了deleteRating方法
        ElMessage.success('评分已删除')
        fetchRatings() // 重新获取列表
    } catch (error) {
        // 用户取消操作
        if (error !== 'cancel') {
            ElMessage.error(error.message || '删除失败')
        }
    }
}

async function fetchRatings() {
  loading.value = true
  try {
    const res = await getUserRatings()
    let list = []
    if (res.data) {
      list = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
    // 兼容大小写字段
        ratings.value = list.map(r => ({
      ...r,
      ratingId: r.ratingId, 
      movieId: r.movieId,
      title: r.title,
      posterUrl: r.posterUrl,
      releaseYear: r.releaseYear,
      genre: r.genre,
      score: r.score,
      ratingTime: r.ratingTime
    }))
  } catch (e) {
    ratings.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRatings()
})
</script>

<style scoped>
.rating-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
}

.rating-item:hover {
  border-color: var(--accent);
  transform: translateX(4px);
}

.movie-thumb {
  width: 80px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  background: var(--bg-secondary);
}

.rating-info {
  flex: 1;
}

.rating-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.movie-year {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 8px;
}

.rating-meta {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.score-display {
  font-size: 24px;
  font-weight: 800;
  color: var(--accent);
}

.rate-time {
  font-size: 13px;
  color: var(--text-muted);
}
</style>
