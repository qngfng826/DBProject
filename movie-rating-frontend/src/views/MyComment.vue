<template>
  <div class="page-container">
    <div class="section-title">
      <el-icon><ChatDotRound /></el-icon>
      我的评论
    </div>

    <div v-loading="loading">
      <div v-if="comments.length" class="comment-list">
        <div v-for="c in comments" :key="c.commentId" class="comment-item">
          <img :src="c.posterUrl || defaultPoster" class="movie-thumb" @click="goMovie(c.movieId)" @error="handleImgError" />
          <div class="comment-body">
            <div class="comment-header">
              <h3 @click="goMovie(c.movieId)">{{ c.Title }}</h3>
              <span class="comment-time">{{ formatTime(c.commentTime) }}</span>
            </div>
            <p class="comment-content">{{ c.content }}</p>
            <div class="comment-actions">
              <el-button text size="small" @click="editComment(c)">编辑</el-button>
              <el-button text size="small" type="danger" @click="handleDelete(c)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <el-icon><ChatDotRound /></el-icon>
        <p>你还没有发表过评论</p>
        <el-button type="primary" @click="$router.push('/movies')" style="margin-top: 16px">去看电影</el-button>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑评论" width="500px">
      <el-input v-model="editForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { updateComment, deleteComment, getUserComments } from '@/api/comment'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const comments = ref([])
const loading = ref(false)
const editDialogVisible = ref(false)
const saving = ref(false)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="100" height="150" fill="#30363d"><rect width="100" height="150"/><text x="50" y="75" text-anchor="middle" fill="#8b949e" font-size="10">暂无</text></svg>')

const editForm = reactive({ id: null, content: '' })

//统一权限检查时机
function checkCommentPermission(c) {
  const commentUserId = c.userId || c.UserID || c.userId
  if (commentUserId !== getCurrentUserId()) {
    ElMessage.error('你没有权限操作这条评论')
    return false
  }
  return true
}

// 在 script setup 顶部添加
function getCurrentUserId() {
  // 优先从userStore获取，兼容不同字段名
  const userInfo = userStore.userInfo
  return userInfo?.userId || userInfo?.UserID || userInfo?.id
}

function handleImgError(e) {
  e.target.src = defaultPoster
}

function formatTime(t) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

function goMovie(id) {
  router.push(`/movie/${id}`)
}

function editComment(c) {
  if (!checkCommentPermission(c)) return
  editForm.id = c.commentId
  editForm.content = c.content
  editDialogVisible.value = true
}

async function saveEdit() {
  if (!editForm.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  saving.value = true
  try {
    await updateComment(editForm.id, { content: editForm.content })
    ElMessage.success('评论已更新')
    editDialogVisible.value = false
    fetchComments()
  } catch (e) { /* error handled */ } finally {
    saving.value = false
  }
}

async function handleDelete(c) {
  if (!checkCommentPermission(c)) return
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
    const commentUserId = c.userId || c.UserID || c.userId
    if (commentUserId !== getCurrentUserId()) {
      ElMessage.error('你没有权限删除这条评论')
      return
    }
    await deleteComment(c.commentId || c.CommentID)
    ElMessage.success('评论已删除')
    fetchComments()
  } catch (e) { /* cancelled */ }
}

async function fetchComments() {
  loading.value = true
  try {
    const res = await getUserComments()
    let list = []
    if (res.data) {
      list = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
    // 兼容大小写字段
    comments.value = list.map(c => ({
      ...c,
      commentId: c.commentId || c.CommentID,
      movieId: c.movieId || c.MovieID,
      Title: c.Title || c.title,
      commentTime: c.commentTime || c.CommentTime,
      content: c.content || c.Content,
      posterUrl: c.posterUrl || c.PosterUrl
    }))
  } catch (e) {
    console.error('获取评论失败', e)
    comments.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
}

.movie-thumb {
  width: 80px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  background: var(--bg-secondary);
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  cursor: pointer;
}

.comment-header h3:hover {
  color: var(--accent);
}

.comment-time {
  font-size: 13px;
  color: var(--text-muted);
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}
</style>
