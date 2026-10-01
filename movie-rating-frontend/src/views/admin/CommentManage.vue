<template>
  <div class="page-container">
    <div class="section-title"><el-icon><ChatDotRound /></el-icon>评论管理</div>

    <div class="admin-card toolbar-card">
      <el-input v-model="keyword" placeholder="搜索评论内容、用户名或电影" clearable :prefix-icon="Search" style="width: 300px" @keyup.enter="handleSearch" @clear="handleSearch" />
    </div>

    <div class="admin-card table-card">
      <el-table :data="list" v-loading="loading" style="width: 100%" :header-cell-style="{background:'#30363d', color:'#c9d1d9'}">
        <el-table-column prop="CommentID" label="ID" width="60" sortable />
        <el-table-column prop="Username" label="用户名" min-width="110" />
        <el-table-column prop="MovieTitle" label="电影" min-width="140" show-overflow-tooltip />
        <el-table-column prop="Content" label="评论内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="CommentTime" label="评论时间" width="160" sortable>
          <template #default="{ row }">{{ formatTime(row.CommentTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑评论" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="电影">
          <el-input v-model="form.movieTitle" disabled />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="评论内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save" :loading="saving">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Search, Edit, Delete } from '@element-plus/icons-vue'
import { getCommentList, adminUpdateComment, adminDeleteComment } from '@/api/comment'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const saving = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({ commentId: null, username: '', movieTitle: '', content: '' })

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCommentList({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error("接口报错:", e);
    list.value = [];
    total.value = 0 } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handlePageChange(val) { currentPage.value = val; fetchData() }

function openDialog(row) {
  Object.assign(form, { commentId: row.CommentID, username: row.Username, movieTitle: row.MovieTitle, content: row.Content })
  dialogVisible.value = true
}

async function save() {
  if (!form.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdateComment(form.commentId, { content: form.content })
    ElMessage.success('更新成功')
    dialogVisible.value = false; fetchData()
  } catch (e) {
    console.error("接口报错:", e);
  } finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.Username}」对电影「${row.MovieTitle}」的评论?`, '提示', { type: 'warning' })
    await adminDeleteComment(row.CommentID)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    console.error("接口报错:", e);
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.admin-card { background: #1e2126; border: 1px solid #30363d; border-radius: 8px; padding: 20px; margin-bottom: 24px; }
.toolbar-card { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 全黑表格样式覆盖 */
:deep(.el-table) {
  --el-table-bg-color: #1e2126;
  --el-table-tr-bg-color: #1e2126;
  --el-table-header-bg-color: #161b22;
  --el-table-row-hover-bg-color: #30363d;
  --el-table-text-color: #c9d1d9;
  --el-table-border-color: #30363d;
}

:deep(.el-table__body tr > td) {
  background-color: #1e2126 !important; /* 强制统一背景 */
}

:deep(.el-table__body tr.hover-row > td) {
  background-color: #30363d !important; /* 悬停高亮 */
}

:deep(.el-table::before) { background-color: transparent; }

/* --- 分页组件样式增强 --- */
.pagination-wrapper :deep(.el-pagination .el-pager li) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  font-weight: 500;
  border-radius: 4px;
}

.pagination-wrapper :deep(.el-pagination .el-pager li:hover) {
  color: var(--accent);
  border-color: var(--accent);
}

.pagination-wrapper :deep(.el-pagination .el-pager li.is-active) {
  background-color: var(--accent) !important;
  color: #fff !important;
  font-weight: bold;
  border-color: var(--accent);
  cursor: default;
}

.pagination-wrapper :deep(.el-pagination button) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}
</style>
