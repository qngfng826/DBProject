<template>
  <div class="page-container" v-loading="loading">
    <div v-if="movie">
      <!-- 电影主信息 -->
      <div class="movie-header">
        <div class="movie-poster-large">
          <img :src="movie.PosterUrl || defaultPoster" :alt="movie.Title" @error="handleImgError" />
        </div>
        <div class="movie-main">
          <h1 class="movie-title">{{ movie.Title }}</h1>
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

      <!-- 评论区 -->
      <div class="comment-section">
        <div class="section-title">
          <el-icon><ChatDotRound /></el-icon>
          全部评论 ({{ comments.length }})
        </div>

        <div class="comment-input" v-if="userStore.isLogin">
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="写下你的观影感受..." maxlength="500" show-word-limit />
          <el-button type="primary" @click="submitComment" :loading="submitting" style="margin-top: 12px">发表评论</el-button>
        </div>
        <el-alert v-else type="info" :closable="false" style="margin-bottom: 16px">
          <router-link to="/login">登录</router-link> 后即可评论
        </el-alert>

        <div class="comment-list">
          <div v-for="c in comments" :key="c.CommentID || c.commentId" class="comment-item">
            <el-avatar :size="40" class="comment-avatar">{{ c.Username?.charAt(0).toUpperCase() || c.username?.charAt(0).toUpperCase() }}</el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-user">{{ c.Username || c.username }}</span>
                <span class="comment-time">{{ formatTime(c.CommentTime || c.commentTime) }}</span>
              </div>
              <p class="comment-content">{{ c.Content || c.content }}</p>
              <div class="comment-actions" v-if="(c.UserID || c.userId) === userStore.userInfo?.UserID">
                <el-button text size="small" @click="editComment(c)">编辑</el-button>
                <el-button text size="small" type="danger" @click="deleteComment(c)">删除</el-button>
              </div>
            </div>
          </div>
          <div v-if="!comments.length" class="empty-state">
            <el-icon><ChatDotRound /></el-icon>
            <p>暂无评论，来抢沙发吧</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { getMovieDetail, getMovieComments } from '@/api/movie'
import { addRating, deleteRatingByMovieId, getUserRating } from '@/api/rating'
import { addComment, updateComment, deleteComment as delComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const movie = ref(null)
const directors = ref([])
const actors = ref([])
const comments = ref([])
const loading = ref(false)
const myScore = ref(0)
const commentText = ref('')
const submitting = ref(false)
const editingComment = ref(null)

const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300" fill="#30363d"><rect width="200" height="300"/><text x="100" y="150" text-anchor="middle" fill="#8b949e" font-size="14">暂无海报</text></svg>')

function handleImgError(e) { e.target.src = defaultPoster }
function formatTime(t) { return dayjs(t).format('YYYY-MM-DD HH:mm') }
function goActor(id) { if(id) router.push(`/actor/${id}`) }
function goDirector(id) { if(id) router.push(`/director/${id}`) }

function getCurrentUserId() {
  const userInfo = userStore.userInfo
  return userInfo?.userId || userInfo?.UserID || userInfo?.id
}

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
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
      PosterUrl: movieData.PosterUrl || movieData.posterUrl || ''
    }
    
    directors.value = movieData.directors || movieData.Directors || []
    actors.value = movieData.actors || movieData.Actors || []
    
    if (userStore.isLogin) {
      try {
        const ratingRes = await getUserRating(id)
        myScore.value = ratingRes.data?.Score || ratingRes.data?.score || 0
      } catch (e) { 
        myScore.value = 0 
      }
    }
    
    const commentRes = await getMovieComments(id)
    let rawComments = commentRes.data || []
    // 兼容评论字段大小写
    comments.value = rawComments.map(c => ({
      ...c,
      CommentID: c.CommentID || c.commentId,
      UserID: c.UserID || c.userId,
      Username: c.Username || c.username,
      Content: c.Content || c.content,
      CommentTime: c.CommentTime || c.commentTime
    }))

  } catch (e) {
    console.error(e)
    ElMessage.error('获取电影信息失败')
  } finally {
    loading.value = false
  }
}

// 修复评分：使用 addRating
async function handleRate(score) {
  try {
    await addRating({ movieId: route.params.id, score: score })
    ElMessage.success('评分成功')
    fetchDetail()
  } catch (e) { /* error handled */ }
}

async function removeRating() {
  try {
    await ElMessageBox.confirm('确定取消评分吗？', '提示')
    await deleteRatingByMovieId(route.params.id)
    myScore.value = 0
    ElMessage.success('已取消评分')
    fetchDetail()
  } catch (e) { /* cancelled */ }
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    if (editingComment.value) {
      await updateComment(editingComment.value.CommentID, { content: commentText.value })
      ElMessage.success('评论已更新')
      editingComment.value = null
    } else {
      // 使用兼容字段提交
      await addComment({ movieId: route.params.id, content: commentText.value })
      ElMessage.success('评论已发表')
    }
    commentText.value = ''
    fetchDetail()
  } catch (e) { /* error handled */ } finally {
    submitting.value = false
  }
}

function editComment(c) {
  const commentUserId = c.UserID || c.userId
  if (commentUserId !== getCurrentUserId()) {
    ElMessage.error('你没有权限编辑这条评论')
    return
  }
  editingComment.value = c
  commentText.value = c.Content
}

async function deleteComment(c) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示')
    const commentUserId = c.UserID || c.userId
    if (commentUserId !== getCurrentUserId()) {
      ElMessage.error('你没有权限删除这条评论')
      return
    }
    await delComment(c.CommentID)
    ElMessage.success('评论已删除')
    fetchDetail()
  } catch (e) { /* cancelled */ }
}

watch(() => route.params.id, () => {
  if (route.params.id) fetchDetail()
})

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.movie-header { display: flex; gap: 32px; margin-bottom: 40px; }
.movie-poster-large { flex-shrink: 0; width: 280px; border-radius: var(--radius); overflow: hidden; }
.movie-poster-large img { width: 100%; display: block; }
.movie-main { flex: 1; }
.movie-title { font-size: 32px; font-weight: 800; color: var(--text-primary); margin-bottom: 16px; }
.movie-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
.movie-rating { margin-bottom: 20px; }
.rating-display { display: flex; align-items: center; gap: 16px; }
.rating-num { font-size: 36px; font-weight: 800; color: var(--accent); }
.movie-detail-info p { margin-bottom: 8px; color: var(--text-secondary); }
.movie-detail-info a { color: var(--info); cursor: pointer; }
.movie-detail-info a:hover { color: var(--accent); }
.movie-synopsis { margin-top: 24px; }
.movie-synopsis h3 { font-size: 18px; color: var(--text-primary); margin-bottom: 8px; }
.movie-synopsis p { color: var(--text-secondary); line-height: 1.8; }
.rating-section { margin-top: 32px; padding: 20px; background: var(--bg-card); border-radius: var(--radius); border: 1px solid var(--border-color); }
.rating-section h3 { font-size: 18px; color: var(--text-primary); margin-bottom: 12px; }
.rate-area { display: flex; align-items: center; gap: 16px; }
.rate-text { color: var(--text-secondary); }
.comment-section { margin-top: 40px; }
.comment-input { margin-bottom: 24px; }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { display: flex; gap: 16px; padding: 20px; background: var(--bg-card); border-radius: var(--radius); border: 1px solid var(--border-color); }
.comment-avatar { flex-shrink: 0; background: var(--accent); color: #000; font-weight: 700; }
.comment-body { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.comment-user { font-weight: 600; color: var(--text-primary); }
.comment-time { color: var(--text-muted); font-size: 13px; }
.comment-content { color: var(--text-secondary); line-height: 1.6; }
.comment-actions { margin-top: 8px; }
@media (max-width: 768px) { .movie-header { flex-direction: column; } .movie-poster-large { width: 200px; margin: 0 auto; } }
</style>
