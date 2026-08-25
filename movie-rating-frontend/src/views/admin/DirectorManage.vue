<template>
  <div class="page-container">
    <div class="section-title"><el-icon><DataAnalysis /></el-icon>导演管理</div>

    <div class="admin-card toolbar-card">
      <el-input v-model="keyword" placeholder="搜索导演姓名" clearable :prefix-icon="Search" style="width: 300px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-button type="primary" @click="openDialog(null)" :icon="Plus">新增导演</el-button>
    </div>

    <!-- 移除了 stripe 属性 -->
    <div class="admin-card table-card">
      <el-table :data="list" v-loading="loading" style="width: 100%" :header-cell-style="{background:'#30363d', color:'#c9d1d9'}">
        <el-table-column prop="directorId" label="ID" width="60" sortable />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag effect="plain" :type="row.gender === '男' ? 'primary' : 'danger'">{{ row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="出生日期" width="120" sortable />
        <el-table-column prop="nationality" label="国籍" width="100" />
        
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑导演' : '新增导演'" width="450px" destroy-on-close>
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="导演姓名" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="出生日期">
          <el-date-picker 
            v-model="form.birthDate" 
            type="date" 
            placeholder="选择日期" 
            value-format="YYYY-MM-DD" 
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="国籍">
          <el-input v-model="form.nationality" placeholder="如：美国" />
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
import { DataAnalysis, Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getDirectorList, addDirector, updateDirector, deleteDirector } from '@/api/director'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = { directorId: null, name: '', gender: '男', birthDate: '', nationality: '' }
const form = reactive({ ...defaultForm })

async function fetchData() {
  loading.value = true
  try {
    const res = await getDirectorList({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { 
    console.error("接口报错:", e); 
    ElMessage.error('操作失败，请查看控制台'); 
    list.value = []; total.value = 0 } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handlePageChange(val) { currentPage.value = val; fetchData() }

function openDialog(row) {
  editing.value = !!row
  Object.assign(form, row || defaultForm)
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editing.value) { await updateDirector(form.directorId, form); ElMessage.success('更新成功') }
    else { await addDirector(form); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } catch (e) {
    console.error("接口报错:", e); 
    ElMessage.error('操作失败，请查看控制台'); 
  } finally { saving.value = false }
}

async function handleDelete(row) {
  try { 
    await ElMessageBox.confirm(`确定删除「${row.name}」?`, '提示', { type: 'warning' }); 
    await deleteDirector(row.directorId); 
    ElMessage.success('已删除'); fetchData() 
  } catch (e) {
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
  background-color: #1e2126 !important;
}

:deep(.el-table__body tr.hover-row > td) {
  background-color: #30363d !important;
}

:deep(.el-table::before) { background-color: transparent; }
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
</style>
