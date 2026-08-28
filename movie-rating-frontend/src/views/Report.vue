<template>
  <div class="page-container">
    <div class="section-title">
      <el-icon><DataAnalysis /></el-icon>
      数据报表
    </div>

    <!-- === 新增：全局统计卡片区域 === -->
    <div class="stats-grid" v-if="globalStats">
      <div class="stat-card">
        <div class="stat-icon stat-icon-gold">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ globalStats.MaxRating?.toFixed(1) || '-' }}</div>
          <div class="stat-label">最高评分</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon stat-icon-blue">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ globalStats.MinRating?.toFixed(1) || '-' }}</div>
          <div class="stat-label">最低评分</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-icon-purple">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ globalStats.TotalComments || 0 }}</div>
          <div class="stat-label">总评论数</div>
        </div>
      </div>
    </div>
    <!-- === 新增结束 === -->

    <!-- 图表区 -->
    <div class="charts-grid">
      <div class="chart-card">
        <h3>各类型电影数量</h3>
        <!--  改回 v-if，确保 DOM 真实渲染且有尺寸 -->
        <div v-if="tableData.length > 0" ref="barChartRef" class="chart"></div>
        <div v-else class="empty-tip">暂无数据</div>
      </div>
      
      <div class="chart-card">
        <h3>各类型平均评分</h3>
        <div v-if="tableData.length > 0" ref="lineChartRef" class="chart"></div>
        <div v-else class="empty-tip">暂无数据</div>
      </div>
    </div>

    <!-- 明细表 -->
    <div class="detail-section">
      <div class="section-title">
        <el-icon><Document /></el-icon>
        类型汇总明细
      </div>
      <!-- 注意：表格中的 MaxRating 和 MinRating 需要后端 SQL 返回对应字段，否则显示为空或0 -->
      <el-table :data="tableData" stripe style="width: 100%" :cell-style="tableCellStyle">
        <el-table-column prop="Genre" label="电影类型" width="180" />
        <el-table-column prop="Count" label="电影数量" width="120" sortable />
        <el-table-column prop="AvgRating" label="平均评分" width="120" sortable>
          <template #default="{ row }">
            <span class="rating-stars">
              <el-icon color="var(--accent)"><Star /></el-icon>
              {{ row.AvgRating?.toFixed(1) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="MaxRating" label="最高评分" width="120" sortable>
          <template #default="{ row }">{{ row.MaxRating?.toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="MinRating" label="最低评分" width="120" sortable>
          <template #default="{ row }">{{ row.MinRating?.toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="TotalComments" label="评论总数" sortable />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
// 新增图标导入
import { DataAnalysis, Document, Star, Trophy, TrendCharts, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getGenreSummary } from '@/api/movie'

//  按需引入，减小体积并避免全量引入的潜在冲突
echarts.use([BarChart, LineChart, GridComponent, TooltipComponent, CanvasRenderer])

// 与电影列表保持一致的电影类型列表
const movieGenres = ['剧情', '喜剧', '动作', '爱情', '科幻', '动画', '悬疑', '犯罪', '奇幻']

const tableData = ref([])
// 新增：全局统计数据
const globalStats = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let barChartInstance = null
let lineChartInstance = null

//  修复表格白底浅字问题
const tableCellStyle = () =>({
  backgroundColor: 'var(--bg-card, #1e2126)',
  color: 'var(--text-primary, #c9d1d9)'
})

//  安全初始化函数，带完整错误捕获
const safeInitChart = (domRef, option) => {
  try {
    if (!domRef || domRef.clientWidth === 0 || domRef.clientHeight === 0) {
      console.warn('图表容器尺寸无效，跳过初始化')
      return null
    }
    const chart = echarts.init(domRef)
    chart.setOption(option)
    return chart
  } catch (e) {
    console.error('ECharts 初始化失败:', e)
    return null
  }
}

const renderCharts = () => {
  if (tableData.value.length === 0) return

  // 从tableData动态提取genres，确保与后端返回的数据一致
  const genres = tableData.value.map(d => d.Genre)

  // 柱状图配置
  const barOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: genres,
      axisLabel: { color: '#8b949e', rotate: 30 },
      axisLine: { lineStyle: { color: '#30363d' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8b949e' },
      splitLine: { lineStyle: { color: '#30363d' } }
    },
    series: [{
      name: '电影数量',
      type: 'bar',
      data: tableData.value.map(d => Number(d.Count) || 0),
      itemStyle: { color: '#f5c518', borderRadius: [4, 4, 0, 0] }
    }]
  }

  // 折线图配置
  const lineOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: genres,
      axisLabel: { color: '#8b949e', rotate: 30 },
      axisLine: { lineStyle: { color: '#30363d' } }
    },
    yAxis: {
      type: 'value', min: 0, max: 10,
      axisLabel: { color: '#8b949e' },
      splitLine: { lineStyle: { color: '#30363d' } }
    },
    series: [{
      name: '平均评分',
      type: 'line',
      data: tableData.value.map(d => Number(d.AvgRating) || 0),
      smooth: true,
      lineStyle: { color: '#f5c518', width: 3 },
      itemStyle: { color: '#f5c518' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245,197,24,0.3)' },
          { offset: 1, color: 'rgba(245,197,24,0)' }
        ])
      }
    }]
  }

  barChartInstance = safeInitChart(barChartRef.value, barOption)
  lineChartInstance = safeInitChart(lineChartRef.value, lineOption)
}

async function fetchReport() {
  try {
    const res = await getGenreSummary()

    let list = []
    if (Array.isArray(res.data?.summary)) list = res.data.summary
    else if (Array.isArray(res.data)) list = res.data

    // 处理全局统计数据
    if (res.data?.globalStats) {
        globalStats.value = res.data.globalStats
    }

    // 后端使用IN精确匹配，直接映射数据（避免复合类型重复统计）
    tableData.value = list.map(d => ({
      Genre: d.Genre,
      Count: Number(d.Count) || 0,
      AvgRating: Number(d.AvgRating) || 0,
      MaxRating: Number(d.MaxRating) || 0,
      MinRating: Number(d.MinRating) || 0,
      TotalComments: Number(d.TotalComments) || 0
    })).filter(item => item.Count > 0) // 过滤掉数量为0的类型

    //  双重等待，确保 DOM 完全渲染且有尺寸
    await nextTick()
    setTimeout(() => {
      renderCharts()
    }, 100)
  } catch (e) {
    console.error('获取报表失败:', e)
    tableData.value = []
  }
}

//  窗口缩放时自适应
const handleResize = () => {
  barChartInstance?.resize()
  lineChartInstance?.resize()
}

onMounted(() => {
  fetchReport()
  window.addEventListener('resize', handleResize)
})

//  使用 onBeforeUnmount 更安全
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChartInstance?.dispose()
  lineChartInstance?.dispose()
  barChartInstance = null
  lineChartInstance = null
})
</script>

<style scoped>
/* === 新增：全局统计样式 === */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--bg-card, #1e2126);
  border: 1px solid var(--border-color, #30363d);
  border-radius: var(--radius, 8px);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border-color: var(--accent, #f5c518);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon-gold { background: rgba(245, 197, 24, 0.15); color: #f5c518; }
.stat-icon-blue { background: rgba(64, 158, 255, 0.15); color: #409eff; }
.stat-icon-purple { background: rgba(245, 108, 108, 0.15); color: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary, #c9d1d9);
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted, #8b949e);
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
}
/* === 新增结束 === */


.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}
.chart-card {
  background: var(--bg-card, #1e2126);
  border: 1px solid var(--border-color, #30363d);
  border-radius: var(--radius, 8px);
  padding: 24px;
}
.chart-card h3 {
  font-size: 18px;
  color: var(--text-primary, #c9d1d9);
  margin-bottom: 16px;
}
.chart {
  height: 320px;
  width: 100%;
}
.empty-tip {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b949e;
}
.detail-section { margin-top: 32px; }

/*  强制覆盖 Element Plus 表格样式 */
:deep(.el-table) {
  --el-table-bg-color: var(--bg-card, #1e2126);
  --el-table-tr-bg-color: var(--bg-card, #1e2126);
  --el-table-header-bg-color: var(--bg-header, #161b22);
  --el-table-text-color: var(--text-primary, #c9d1d9);
  --el-table-header-text-color: var(--text-primary, #c9d1d9);
  --el-table-row-hover-bg-color: rgba(245, 197, 24, 0.08);
}

@media (max-width: 768px) {
  .charts-grid { grid-template-columns: 1fr; }
}
</style>
