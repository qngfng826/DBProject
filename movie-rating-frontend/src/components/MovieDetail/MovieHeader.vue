<template>
  <div class="movie-header">
    <div class="movie-poster-large">
      <img :src="movie.PosterUrl || defaultPoster" :alt="movie.Title" loading="eager" @error="handleImgError" />
    </div>
    <div class="movie-main">
      <h1 class="movie-title" @click="handleTitleClick">
        <a href="javascript:void(0)">{{ movie.Title }}</a>
      </h1>
      <div class="movie-tags">
        <el-tag>{{ movie.ReleaseYear }}</el-tag>
        <el-tag type="info">{{ movie.Duration }}分钟</el-tag>
        <el-tag type="warning">{{ movie.Genre }}</el-tag>
        <el-tag type="success">{{ movie.Language }}</el-tag>
      </div>
      <div class="movie-rating">
        <div class="rating-display">
          <span class="rating-num">{{ movie.Rating?.toFixed(1) }}</span>
          <el-rate :model-value="(movie.Rating || 0) / 2" disabled size="large" />
        </div>
      </div>
      <div class="movie-detail-info">
        <p><strong>国家：</strong>{{ movie.Country }}</p>
        <p><strong>导演：</strong>
          <span v-for="(d, i) in directors" :key="i">
            <a @click="goDirector(d.DirectorID || d.directorId)">{{ d.Name || d.name }}</a><span v-if="i < directors.length - 1"> / </span>
          </span>
        </p>
        <p><strong>主演：</strong>
          <span v-for="(a, i) in actors" :key="i">
            <a @click="goActor(a.ActorID || a.actorId)">{{ a.Name || a.name }}</a><span v-if="i < actors.length - 1"> / </span>
          </span>
        </p>
      </div>
      <div class="movie-synopsis">
        <h3>剧情简介</h3>
        <p>{{ movie.Synopsis }}</p>
      </div>

      <!-- 评分操作 -->
      <div class="rating-section" v-if="userStore.isLogin">
        <h3>我的评分</h3>
        <div class="rate-area">
          <el-rate v-model="myScore" :max="10" size="large" @change="handleRate" />
          <span class="rate-text">{{ myScore ? myScore + ' 分' : '点击打分' }}</span>
          <el-button v-if="myScore" text type="danger" @click="removeRating">取消评分</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addRating, deleteRatingByMovieId, getUserRating } from '@/api/rating'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  movie: {
    type: Object,
    required: true
  },
  directors: {
    type: Array,
    default: () => []
  },
  actors: {
    type: Array,
    default: () => []
  }
})

// 是否已经加载过用户评分（防止重复加载）
const hasLoadedUserRating = ref(false)

const emit = defineEmits(['updateMovie'])

const router = useRouter()
const userStore = useUserStore()

function handleTitleClick() {
  if (props.movie.jumpUrl) {
    window.open(props.movie.jumpUrl, '_blank')
  }
}

const myScore = ref(0)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300" fill="#30363d"><rect width="200" height="300"/><text x="100" y="150" text-anchor="middle" fill="#8b949e" font-size="14">暂无海报</text></svg>')

function handleImgError(e) {
  e.target.src = defaultPoster
}

function goActor(id) {
  if (id) router.push(`/actor/${id}`)
}

function goDirector(id) {
  if (id) router.push(`/director/${id}`)
}

async function fetchUserRating() {
  if (!userStore.isLogin) return

  // 优先使用后端返回的用户评分
  if (props.movie.UserRating != null && props.movie.UserRating > 0 && !hasLoadedUserRating.value) {
    myScore.value = props.movie.UserRating
    hasLoadedUserRating.value = true
    return
  }

  // 如果后端没有返回用户评分，则调用API获取
  if (!hasLoadedUserRating.value) {
    try {
      const ratingRes = await getUserRating(props.movie.MovieID || props.movie.movieId)
      myScore.value = ratingRes.data?.Score || ratingRes.data?.score || 0
      hasLoadedUserRating.value = true
    } catch (e) {
      myScore.value = 0
    }
  }
}

async function handleRate(score) {
  try {
    await addRating({ movieId: props.movie.MovieID || props.movie.movieId, score: score })
    ElMessage.success('评分成功')
    fetchUserRating()
    emit('updateMovie')
  } catch (e) {
    ElMessage.error('评分失败')
  }
}

async function removeRating() {
  try {
    await ElMessageBox.confirm('确定取消评分吗？', '提示')
    await deleteRatingByMovieId(props.movie.MovieID || props.movie.movieId)
    myScore.value = 0
    ElMessage.success('已取消评分')
    emit('updateMovie')
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  fetchUserRating()
})
</script>

<style scoped>
.movie-header {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
}

.movie-poster-large {
  flex-shrink: 0;
  width: 280px;
  border-radius: var(--radius);
  overflow: hidden;
}

.movie-poster-large img {
  width: 100%;
  display: block;
}

.movie-main {
  flex: 1;
}

.movie-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 16px;
  cursor: pointer;
}

.movie-title a {
  color: inherit;
  text-decoration: none;
}

.movie-title:hover a {
  text-decoration: underline;
  color: var(--info);
}

.movie-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.movie-rating {
  margin-bottom: 20px;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rating-num {
  font-size: 36px;
  font-weight: 800;
  color: var(--accent);
}

.movie-detail-info p {
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.movie-detail-info a {
  color: var(--info);
  cursor: pointer;
}

.movie-detail-info a:hover {
  color: var(--accent);
}

.movie-synopsis {
  margin-top: 24px;
}

.movie-synopsis h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.movie-synopsis p {
  color: var(--text-secondary);
  line-height: 1.8;
}

.rating-section {
  margin-top: 32px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
}

.rating-section h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.rate-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rate-text {
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .movie-header {
    flex-direction: column;
  }
  .movie-poster-large {
    width: 200px;
    margin: 0 auto;
  }
}
</style>
