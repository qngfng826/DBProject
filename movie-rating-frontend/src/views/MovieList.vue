<template>
  <div class="page-container">
    <div class="section-title">电影列表</div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="搜索电影名" clearable :prefix-icon="Search" style="width: 200px" @keyup.enter="handleSearch" />
      <el-select v-model="filters.genre" placeholder="类型" clearable style="width: 150px" @change="handleSearch">
        <el-option v-for="g in genres" :key="g" :label="g" :value="g" />
      </el-select>
      <el-select v-model="filters.year" placeholder="年份" clearable style="width: 120px" @change="handleSearch">
        <el-option v-for="y in years" :key="y" :label="y" :value="y" />
      </el-select>
      <el-select v-model="filters.sort" placeholder="排序" style="width: 150px" @change="handleSearch">
        <el-option label="评分从高到低" value="rating_desc" />
        <el-option label="评分从低到高" value="rating_asc" />
        <el-option label="年份从新到旧" value="year_desc" />
        <el-option label="年份从旧到新" value="year_asc" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
      <el-button @click="resetFilters" :disabled="loading">重置</el-button>
    </div>

    <!-- 电影列表 -->
    <div v-loading="loading" style="min-height: 300px">
      <div v-if="movies.length" class="movie-grid">
        <div v-for="movie in movies" :key="movie.movieId" class="movie-card" @click="goDetail(movie.movieId)">
          <div class="poster-wrapper">
            <img :src="movie.PosterUrl || defaultPoster" :alt="movie.Title" class="movie-poster" loading="lazy" @error="handleImgError" />
            <div class="poster-overlay">
              <el-icon :size="32"><View /></el-icon>
              <span>查看详情</span>
            </div>
          </div>
          <div class="movie-info">
            <h3 class="movie-title">{{ movie.Title }}</h3>
            <div class="movie-meta">
              <span class="rating-stars">
                <el-icon color="var(--accent)"><Star /></el-icon>
                <span class="score">{{ movie.Rating?.toFixed(1) }}</span>
              </span>
              <span>{{ movie.ReleaseYear }}</span>
            </div>
            <el-tag size="small" effect="plain">{{ movie.Genre }}</el-tag>
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
        <el-icon><Film /></el-icon>
        <p v-if="hasSearchCondition">暂无符合条件的电影，请尝试其他搜索条件</p>
        <p v-else>暂无电影数据</p>
        <div v-if="hasSearchCondition" class="empty-actions">
          <el-button type="primary" @click="resetFilters">清除筛选条件</el-button>
          <el-button @click="$router.push('/')">浏览热门推荐</el-button>
        </div>
      </div>
    </div>
     
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Star, View, Film } from '@element-plus/icons-vue'
import { searchMovies } from '@/api/movie'

const router = useRouter()
const route = useRoute()

const movies = ref([])
const loading = ref(false)
const defaultPoster = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300" fill="#30363d"><rect width="200" height="300"/><text x="100" y="150" text-anchor="middle" fill="#8b949e" font-size="14">暂无海报</text></svg>')

const genres = ['剧情', '喜剧', '动作', '爱情', '科幻', '动画', '悬疑', '犯罪', '奇幻']
const years = [2025, 2024, 2023, 2022, 2021, 2020, 2019, 2010, 2000, 1990]

const filters = reactive({
  keyword: route.query.keyword || '',
  genre: '',
  year: '',
  sort: 'rating_desc'
})

// 计算是否有搜索条件
const hasSearchCondition = computed(() => {
  return filters.keyword || filters.genre || filters.year
})

// 新增：分页状态
const pagination = reactive({
  current: 1,
  size: 10
})

// 新增：总记录数（从后端IPage对象中获取）
const total = ref(0)

function handleImgError(e) {
  e.target.src = defaultPoster
}

function goDetail(id) {
  if(!id) return
  router.push(`/movie/${id}`)
}

function handleSearch() {
  fetchMovies()
}

// 新增：每页条数变化处理
function handleSizeChange(val) {
  pagination.size = val
  // 通常当每页条数变化时，回到第一页
  pagination.current = 1
  fetchMovies()
}

// 新增：当前页码变化处理
function handleCurrentChange(val) {
  pagination.current = val
  fetchMovies()
}

// 修改：重置筛选时，重置分页到第一页
function resetFilters() {
  filters.keyword = ''
  filters.genre = ''
  filters.year = ''
  filters.sort = 'rating_desc'
  pagination.current = 1 // 新增：重置页码
  pagination.size = 10    // 新增：重置每页条数（可选）
  fetchMovies()
}

async function fetchMovies() {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword,
      genre: filters.genre,
      year: filters.year,
      sort: filters.sort,
      page: pagination.current,
      size: pagination.size
    }
    const res = await searchMovies(params)

    let list = []
    if (res.data && res.data.records) {
      list = res.data.records
      total.value = res.data.total // 更新总记录数
    } else if (Array.isArray(res?.data)) {
      // 兼容旧接口可能直接返回数组的情况
      list = res.data
      total.value = list.length
    } else {
      list = []
      total.value = 0
    }
    
    // 兼容大小写字段，防止页面渲染崩溃
    movies.value = list.map(m => ({
      ...m,
      movieId: m.movieId || m.MovieID,
      Title: m.Title || m.title || '未知电影',
      Rating: m.Rating || m.rating || 0,
      PosterUrl: m.PosterUrl || m.posterUrl || '',
      ReleaseYear: m.ReleaseYear || m.releaseYear || '',
      Genre: m.Genre || m.genre || '未知'
    }))
    
  } catch (e) {
    console.error('Error fetching movies:', e)
    movies.value = []
  } finally {
    loading.value = false
  }
}

watch(() => route.query.keyword, (val) => {
  filters.keyword = val || ''
  if (val) handleSearch()
})

onMounted(() => {
  fetchMovies()
})
</script>

<style scoped>
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin-bottom: 24px; padding: 20px; background: var(--bg-card); border-radius: var(--radius); border: 1px solid var(--border-color); }
.movie-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 24px; }
.movie-card { cursor: pointer; transition: var(--transition); }
.movie-card:hover { transform: translateY(-4px); }
.poster-wrapper { position: relative; border-radius: var(--radius); overflow: hidden; aspect-ratio: 2 / 3; background: var(--bg-secondary); }
.movie-poster { width: 100%; height: 100%; object-fit: cover; display: block; }
.poster-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.7); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--accent); font-size: 14px; opacity: 0; transition: var(--transition); }
.movie-card:hover .poster-overlay { opacity: 1; }
.movie-info { padding: 12px 4px; }
.movie-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.movie-meta { display: flex; align-items: center; justify-content: space-between; font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
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
