<template>
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
          <div class="comment-actions" v-if="(c.UserID || c.userId) === myUserId">
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
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMovieComments, addComment, updateComment, deleteComment as delComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const props = defineProps({
  movieId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['updateComments'])

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const commentText = ref('')
const submitting = ref(false)
const editingComment = ref(null)

// 登录接口返回驼峰 userId，评论区数据返回大写 UserID，两种都要兼容
const myUserId = computed(() =>
  userStore.userInfo?.userId || userStore.userInfo?.UserID || userStore.userInfo?.id
)

function formatTime(t) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function fetchComments() {
  try {
    const commentRes = await getMovieComments(props.movieId)
    let rawComments = commentRes.data || []
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
    ElMessage.error('获取评论失败')
  }
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
      await addComment({ movieId: props.movieId, content: commentText.value })
      ElMessage.success('评论已发表')
    }
    commentText.value = ''
    fetchComments()
    emit('updateComments')
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

function editComment(c) {
  const commentUserId = c.UserID || c.userId
  if (commentUserId !== myUserId.value) {
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
    if (commentUserId !== myUserId.value) {
      ElMessage.error('你没有权限删除这条评论')
      return
    }
    await delComment(c.CommentID)
    ElMessage.success('评论已删除')
    fetchComments()
    emit('updateComments')
  } catch (e) {
    // 用户取消
  }
}

// immediate：父组件 v-if 等电影详情加载完才挂载本组件且 movieId 不再变化，
// 必须挂载即拉取一次评论；否则首屏永远显示“暂无评论”
watch(() => props.movieId, () => {
  if (props.movieId) fetchComments()
  // 切换电影时清理输入框和编辑态，避免跨电影残留
  commentText.value = ''
  editingComment.value = null
}, { immediate: true })
</script>

<style scoped>
.comment-section {
  margin-top: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.comment-input {
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
}

.comment-avatar {
  flex-shrink: 0;
  background: var(--accent);
  color: #000;
  font-weight: 700;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  color: var(--text-muted);
  font-size: 13px;
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-state p {
  margin: 0;
}
</style>
