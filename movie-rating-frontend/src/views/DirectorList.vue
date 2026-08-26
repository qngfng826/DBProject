<template>
  <div class="page-container">
    <div class="section-title">导演列表</div>

    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索导演姓名" clearable :prefix-icon="Search" @keyup.enter="handleSearch" style="width: 250px" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <div v-loading="loading" style="min-height: 300px">
      <div v-if="directors.length" class="card-grid">
        <div v-for="d in directors" :key="d.directorId" class="person-card" @click="goDetail(d.directorId)">
          <div class="person-avatar">
            <el-avatar :size="100">{{ d.name?.charAt(0) }}</el-avatar>
          </div>
          <div class="person-info">
            <h3>{{ d.name }}</h3>
            <p>{{ d.gender }} · {{ d.nationality }}</p>
            <p class="birth">{{ formatDate(d.birthDate) }}</p>
          </div>
        </div>
      </div>
      <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
      <div v-else class="empty-state">
        <el-icon><User /></el-icon>
        <p>暂无导演数据</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Search, User } from '@element-plus/icons-vue'
import { getDirectorList } from '@/api/director'
import dayjs from 'dayjs'

const router = useRouter()
const directors = ref([])
const loading = ref(false)
const keyword = ref('')

// 新增：分页状态
const pagination = reactive({
  current: 1,
  size: 10
})

// 新增：总记录数（从后端IPage对象中获取）
const total = ref(0)

function formatDate(d) {
  return d ? dayjs(d).format('YYYY-MM-DD') : ''
}

function goDetail(id) {
  router.push(`/director/${id}`)
}

function handleSearch() {
  pagination.current = 1 // 重置到第一页
  fetchDirectors()
}

// 新增：每页条数变化处理
function handleSizeChange(val) {
  pagination.size = val
  // 通常当每页条数变化时，回到第一页
  pagination.current = 1
  fetchDirectors()
}

// 新增：当前页码变化处理
function handleCurrentChange(val) {
  pagination.current = val
  fetchDirectors()
}

async function fetchDirectors() {
  loading.value = true
  try {
    const res = await getDirectorList({ keyword: keyword.value, page: pagination.current, size: pagination.size })
    let list = []
    if (res.data) {
      list = res.data.records || res.data.list || (Array.isArray(res.data) ? res.data : [])
      total.value = res.data.total || list.length || 0 // 更新总记录数
    }
    // 兼容大小写字段
    directors.value = list.map(d => ({
      ...d,
      directorId: d.directorId || d.DirectorID,
      name: d.name || d.Name,
      gender: d.gender || d.Gender,
      nationality: d.nationality || d.Nationality,
      birthDate: d.birthDate || d.BirthDate
    }))
  } catch (e) {
    directors.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDirectors()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.person-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
}

.person-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent);
}

.person-avatar {
  margin-bottom: 16px;
}

.person-avatar :deep(.el-avatar) {
  background: var(--accent);
  color: #000;
  font-size: 32px;
  font-weight: 700;
}

.person-info {
  text-align: center;
}

.person-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.person-info p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
}

.person-info .birth {
  color: var(--text-muted);
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* --- 分页组件样式增强 --- */

/* 1. 普通页码：增加边框，使用次要背景色，使其从卡片背景中凸显 */
.pagination-wrapper :deep(.el-pagination .el-pager li) {
  background-color: var(--bg-secondary); /* 使用次级背景色 */
  color: var(--text-primary);
  border: 1px solid var(--border-color); /* 添加边框 */
  font-weight: 500;
  border-radius: 4px; /* 圆角 */
}

/* 2. 鼠标悬停：显示主题色边框 */
.pagination-wrapper :deep(.el-pagination .el-pager li:hover) {
  color: var(--accent);
  border-color: var(--accent);
}

/* 3. 当前页（激活状态）：最明显的颜色 */
.pagination-wrapper :deep(.el-pagination .el-pager li.is-active) {
  background-color: var(--accent) !important; /* 强制背景为主题色 */
  color: #fff !important; /* 文字改为白色，对比度高 */
  font-weight: bold; /* 加粗 */
  border-color: var(--accent);
  cursor: default;
}

/* 4. 上一页/下一页按钮样式 */
.pagination-wrapper :deep(.el-pagination button) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.pagination-wrapper :deep(.el-pagination button:hover) {
  color: var(--accent);
  border-color: var(--accent);
}

</style>
