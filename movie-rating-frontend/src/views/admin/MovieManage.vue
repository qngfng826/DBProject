<template>
  <div class="admin-page">
    <div class="section-title">
      <el-icon><DataAnalysis /></el-icon>
      电影管理
    </div>

    <div class="admin-card toolbar-card">
      <el-input
        v-model="keyword"
        placeholder="搜索电影名"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="openDialog(null)" :icon="Plus">新增电影</el-button>
    </div>

    <div class="admin-card table-card">
      <el-table
        :data="movies"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="movieId" label="ID" width="60" sortable />
        
        <el-table-column label="海报" width="100">
          <template #default="{ row }">
            <el-image
              style="width: 50px; height: 75px; border-radius: 4px; cursor: pointer"
              :src="row.posterUrl"
              :preview-src-list="[row.posterUrl]"
              fit="cover"
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="releaseYear" label="年份" width="80" sortable />
        <el-table-column prop="genre" label="类型" width="120" />
        
        <el-table-column prop="rating" label="评分" width="100" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.rating" effect="dark" round :type="row.rating >= 8 ? 'success' : (row.rating >= 6 ? 'primary' : 'warning')">
              {{ row.rating?.toFixed(1) }}
            </el-tag>
            <span v-else>-</span>
          </template>
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
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑电影' : '新增电影'" width="700px" class="custom-dialog">
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-row :gutter="20">
          <el-col :span="24"><el-form-item label="标题"><el-input v-model="form.title" clearable /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上映年份"><el-input-number v-model="form.releaseYear" :min="1900" :max="2030" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时长(分)"><el-input-number v-model="form.duration" :min="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-input v-model="form.genre" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="语言"><el-input v-model="form.language" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家"><el-input v-model="form.country" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="海报链接"><el-input v-model="form.posterUrl" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="外部链接"><el-input v-model="form.jumpUrl" placeholder="点击电影标题跳转的URL（选填）" clearable /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="导演">
              <el-select v-model="form.directorIds" multiple placeholder="请选择导演" style="width: 100%">
                <el-option v-for="d in directorOptions" :key="d.directorId" :label="d.name" :value="d.directorId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="演员与角色">
              <div v-for="(item, index) in form.actors" :key="index" style="display:flex; margin-bottom:8px; width:100%;">
                <el-select v-model="item.actorId" placeholder="选择演员" style="width: 40%; margin-right: 10px;">
                  <el-option v-for="a in actorOptions" :key="a.actorId" :label="a.name" :value="a.actorId" />
                </el-select>
                <el-input v-model="item.roleName" placeholder="饰演角色名 (如：安迪)" style="width: 50%; margin-right: 10px;" />
                <el-button type="danger" :icon="Delete" @click="form.actors.splice(index, 1)" circle />
              </div>
              <el-button type="primary" plain size="small" @click="form.actors.push({ actorId: null, roleName: '' })">+ 添加演员</el-button>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="简介"><el-input v-model="form.synopsis" type="textarea" :rows="4" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMovie" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, Search, Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { searchMovies, addMovie, updateMovie, deleteMovie, getMovieDetail } from '@/api/movie'
import { getActorList } from '@/api/actor'
import { getDirectorList } from '@/api/director'

const movies = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)

const actorOptions = ref([])
const directorOptions = ref([])

// 新增分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = {
  movieId: null, title: '', releaseYear: null, duration: null, genre: '',
  language: '', country: '', synopsis: '', posterUrl: '', jumpUrl: '',
  directorIds: [], actors: [], directors: []
}
const form = reactive({ ...defaultForm })

async function loadOptions() {
  try {
    const aRes = await getActorList({ page: 1, size: 9999 })
    actorOptions.value = aRes.data?.records || []
    const dRes = await getDirectorList({ page: 1, size: 9999 })
    directorOptions.value = dRes.data?.records || []
  } catch (e) {
    console.error('加载演员/导演选项失败:', e) // 错误提示已由 request.js 拦截器统一弹出
  }
}

// 修改数据获取：支持分页参数
async function fetchData() {
  loading.value = true
  try {
    // searchMovies 内部调用 /movie/search 接口，传递分页参数
    const res = await searchMovies({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    movies.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { 
    console.error("接口报错:", e);
    movies.value = [] 
    total.value = 0
  } finally { 
    loading.value = false 
  }
}

function handleSearch() {
  currentPage.value = 1 // 搜索时回到第一页
  fetchData()
}

function handleSizeChange(val) { pageSize.value = val; fetchData() }
function handlePageChange(val) { currentPage.value = val; fetchData() }

async function openDialog(row) {
  editing.value = !!row
  if (row) {
    // 列表接口不返回关联的导演/演员，需请求详情接口回显
    try {
      const res = await getMovieDetail(row.movieId)
      const detail = res.data || {}
      Object.assign(form, defaultForm, detail, {
        directorIds: (detail.directors || []).map(d => d.directorId),
        actors: (detail.actors || []).map(a => ({ actorId: a.actorId, roleName: a.roleName || '' }))
      })
    } catch (e) {
      console.error('获取电影详情失败:', e)
      Object.assign(form, defaultForm, row, { directorIds: [], actors: [] })
    }
  } else {
    // 数组字段必须赋新数组：Object.assign 是浅拷贝，
    // 直接拷贝 defaultForm 的引用会被 push/splice 污染，导致下次打开仍残留
    Object.assign(form, defaultForm, { directorIds: [], actors: [], directors: [] })
  }
  dialogVisible.value = true
}

async function saveMovie() {
  saving.value = true
  try {
    form.directors = form.directorIds.map(id => ({ directorId: id }))
    if (editing.value) {
      await updateMovie(form.movieId, form)
      ElMessage.success('更新成功')
    } else {
      await addMovie(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error("接口报错:", e);
  } finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { type: 'warning' })
    await deleteMovie(row.movieId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    console.error("接口报错:", e)
  }
}

onMounted(() => {
  fetchData()
  loadOptions()
})
</script>


<style scoped>
/* 暗色表格/卡片/分页公共样式已抽取至 src/assets/admin-theme.css（AdminLayout 引入） */
.image-slot {
  width: 50px; height: 75px;
  display: flex; justify-content: center; align-items: center;
  background: #30363d; border-radius: 4px;
  color: #8b949e; font-size: 20px;
}
</style>
