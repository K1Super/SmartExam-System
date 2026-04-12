<template>
  <div class="score-statistics-container">
    <el-card class="score-statistics-card">
      <template #header>
        <div class="card-header">
          <span>成绩统计</span>
          <el-select v-model="examId" placeholder="选择考试" style="width: 200px;">
            <el-option v-for="exam in exams" :key="exam.id" :label="exam.title" :value="exam.id"></el-option>
          </el-select>
          <el-button type="primary" @click="handleGenerateStatistics" style="margin-left: 10px;">生成统计</el-button>
        </div>
      </template>
      
      <div v-if="statistics" class="statistics-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ statistics.totalCount }}</div>
                <div class="stat-label">总人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ statistics.submitCount }}</div>
                <div class="stat-label">提交人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ statistics.avgScore }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ statistics.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="chart-container">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>成绩分布</span>
                </template>
                <div ref="scoreChartRef" class="chart"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>成绩占比</span>
                </template>
                <div ref="scorePieChartRef" class="chart"></div>
              </el-card>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="24">
              <el-card class="chart-card">
                <template #header>
                  <span>成绩概况</span>
                </template>
                <div ref="scoreTrendChartRef" class="chart"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      <div v-else class="empty">
        请选择考试并生成统计数据
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '../../api'

const examId = ref('')
const exams = ref([])
const statistics = ref(null)
const distribution = ref(null)
const clazzStatistics = ref([])
const scoreChartRef = ref(null)
const scorePieChartRef = ref(null)
const scoreTrendChartRef = ref(null)
let scoreChart = null
let scorePieChart = null
let scoreTrendChart = null

onMounted(() => {
  fetchExams()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (scoreChart) scoreChart.dispose()
  if (scorePieChart) scorePieChart.dispose()
  if (scoreTrendChart) scoreTrendChart.dispose()
})

const fetchExams = async () => {
  try {
    const response = await api.get('/exams')
    if (response.code === 200) {
      exams.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  }
}

const handleGenerateStatistics = async () => {
  if (!examId.value) {
    ElMessage.warning('请选择考试')
    return
  }
  
  try {
    const [statsResponse, distResponse, clazzResponse] = await Promise.all([
      api.post(`/statistics/generate/${examId.value}`),
      api.get(`/statistics/distribution/${examId.value}`),
      api.get(`/statistics/clazz/${examId.value}`)
    ])
    
    if (statsResponse.code === 200) statistics.value = statsResponse.data
    if (distResponse.code === 200) distribution.value = distResponse.data
    if (clazzResponse.code === 200) clazzStatistics.value = clazzResponse.data
    
    await nextTick()
    initCharts()
    
    ElMessage.success('统计报告生成成功')
  } catch (error) {
    console.error('生成统计失败详情:', error)
    ElMessage.error('生成统计失败，请确保考试已有学生提交答卷')
  }
}

const initCharts = () => {
  initScoreChart()
  initScorePieChart()
  initScoreTrendChart()
}

const initScoreChart = () => {
  if (!scoreChartRef.value) return
  
  if (scoreChart) {
    scoreChart.dispose()
  }
  
  scoreChart = echarts.init(scoreChartRef.value)
  
  const dist = distribution.value || {
    range0_60: 0,
    range60_70: 0,
    range70_80: 0,
    range80_90: 0,
    range90_100: 0
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['0-60', '60-70', '70-80', '80-90', '90-100']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [
          dist.range0_60,
          dist.range60_70,
          dist.range70_80,
          dist.range80_90,
          dist.range90_100
        ],
        type: 'bar',
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  scoreChart.setOption(option)
}

const initScorePieChart = () => {
  if (!scorePieChartRef.value) return
  
  if (scorePieChart) {
    scorePieChart.dispose()
  }
  
  scorePieChart = echarts.init(scorePieChartRef.value)
  
  const dist = distribution.value || {
    range0_60: 0,
    range60_70: 0,
    range70_80: 0,
    range80_90: 0,
    range90_100: 0
  }
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: '60%',
        data: [
          { value: dist.range0_60, name: '0-60' },
          { value: dist.range60_70, name: '60-70' },
          { value: dist.range70_80, name: '70-80' },
          { value: dist.range80_90, name: '80-90' },
          { value: dist.range90_100, name: '90-100' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  scorePieChart.setOption(option)
}

const initScoreTrendChart = () => {
  if (!scoreTrendChartRef.value) return
  
  if (scoreTrendChart) {
    scoreTrendChart.dispose()
  }
  
  scoreTrendChart = echarts.init(scoreTrendChartRef.value)
  
  const stats = statistics.value || {
    avgScore: 0,
    maxScore: 0,
    minScore: 0
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['平均分', '最高分', '最低分']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['本次考试']
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '平均分',
        type: 'line',
        data: [stats.avgScore || 0]
      },
      {
        name: '最高分',
        type: 'line',
        data: [stats.maxScore || 0]
      },
      {
        name: '最低分',
        type: 'line',
        data: [stats.minScore || 0]
      }
    ]
  }
  scoreTrendChart.setOption(option)
}

const handleResize = () => {
  if (scoreChart) scoreChart.resize()
  if (scorePieChart) scorePieChart.resize()
  if (scoreTrendChart) scoreTrendChart.resize()
}
</script>

<style scoped>
.score-statistics-container {
  padding: 20px;
}

.score-statistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-select {
  margin-left: 20px;
}

.statistics-content {
  margin-top: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.chart-container {
  margin-top: 20px;
}

.chart-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: none;
}

.chart {
  width: 100%;
  height: 350px;
}

.empty {
  text-align: center;
  padding: 100px 0;
  color: #909399;
  font-size: 16px;
}
</style>
