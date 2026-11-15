<template>
  <!-- 模板部分省略，结构同上，只需移除 el-table 的 stripe 属性 -->
  <div class="admin-page">
    <div class="section-title"><el-icon><DataAnalysis /></el-icon>演员管理</div>

    <div class="admin-card toolbar-card">
      <el-input v-model="keyword" placeholder="搜索演员姓名" clearable :prefix-icon="Search" style="width: 300px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-button type="primary" @click="openDialog(null)" :icon="Plus">新增演员</el-button>
    </div>

    <!-- 移除了 stripe 属性 -->
    <div class="admin-card table-card">
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="actorId" label="ID" width="60" sortable />
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

    <!-- Dialog 部分省略，保持不变 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑演员' : '新增演员'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="演员姓名" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender" size="default">
            <el-radio value="男"><el-icon><Male /></el-icon> 男</el-radio>
            <el-radio value="女"><el-icon><Female /></el-icon> 女</el-radio>
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
          <el-input v-model="form.nationality" placeholder="如：中国" />
        </el-form-item>
        
        <el-form-item label="照片链接">
          <el-input v-model="form.photoUrl" placeholder="粘贴头像链接" />
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
import { getActorList, addActor, updateActor, deleteActor } from '@/api/actor'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = { actorId: null, name: '', gender: '男', birthDate: '', nationality: '', photoUrl: '' }
const form = reactive({ ...defaultForm })

async function fetchData() {
  loading.value = true
  try {
    const res = await getActorList({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
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
  editing.value = !!row
  Object.assign(form, row || defaultForm)
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editing.value) { await updateActor(form.actorId, form); ElMessage.success('更新成功') }
    else { await addActor(form); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } catch (e) {
    console.error("接口报错:", e);
  } finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」?`, '提示', { type: 'warning' })
    await deleteActor(row.actorId)
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
/* 暗色表格/卡片/分页公共样式已抽取至 src/assets/admin-theme.css（AdminLayout 引入） */
.image-slot { width: 50px; height: 50px; background: #30363d; border-radius: 50%; display: flex; justify-content: center; align-items: center; color: #8b949e; }
</style>
