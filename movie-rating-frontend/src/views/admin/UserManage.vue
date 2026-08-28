<template>
  <div class="page-container">
    <div class="section-title"><el-icon><UserFilled /></el-icon>用户管理</div>

    <div class="admin-card toolbar-card">
      <el-input v-model="keyword" placeholder="搜索用户名或邮箱" clearable :prefix-icon="Search" style="width: 300px" @keyup.enter="handleSearch" @clear="handleSearch" />
    </div>

    <div class="admin-card table-card">
      <el-table :data="list" v-loading="loading" style="width: 100%" :header-cell-style="{background:'#30363d', color:'#c9d1d9'}">
        <el-table-column prop="userId" label="ID" width="60" sortable />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="registerTime" label="注册时间" width="180" sortable>
          <template #default="{ row }">{{ formatTime(row.registerTime) }}</template>
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

    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>

        <el-form-item label="重置密码">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改密码" />
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
import { UserFilled, Search, Edit, Delete } from '@element-plus/icons-vue'
import { getUserList, updateUser, deleteUser } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = { userId: null, username: '', email: '', password: '' }
const form = reactive({ ...defaultForm })

function formatTime(t) {
  if (!t) return '-'
  const d = typeof t === 'number' ? new Date(t) : new Date(t)
  if (isNaN(d.getTime())) return t
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function isProtected(row) {
  // 不允许删除 admin 账号和当前登录账号
  return row.username === 'admin' || row.userId === userStore.userInfo?.userId
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error("接口报错:", e);
    ElMessage.error('操作失败，请查看控制台');
    list.value = [];
    total.value = 0 } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handlePageChange(val) { currentPage.value = val; fetchData() }

function openDialog(row) {
  editing.value = !!row
  Object.assign(form, defaultForm, row ? { userId: row.userId, username: row.username, email: row.email, password: '' } : {})
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    await updateUser(form.userId, form)
    ElMessage.success('更新成功')
    dialogVisible.value = false; fetchData()
  } catch (e) {
    console.error("接口报错:", e);
    ElMessage.error('操作失败，请查看控制台');
  } finally { saving.value = false }
}

async function handleDelete(row) {
  if (isProtected(row)) {
    ElMessage.warning('不能删除 admin 账号或当前登录账号')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」?`, '提示', { type: 'warning' });
    await deleteUser(row.userId); ElMessage.success('已删除');
    fetchData()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    console.error("接口报错:", e);
    ElMessage.error('操作失败，请查看控制台');
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
