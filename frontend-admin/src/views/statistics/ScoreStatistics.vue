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
          <el-card class="chart-card">
            <template #header>
              <span>成绩分布</span>
            </template>
            <div ref="scoreChartRef" class="chart"></div>
          </el-card>
        </div>
      </div>
      <div v-else class="empty">
        请选择考试并生成统计数据
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '../../api'

const examId = ref('')
const exams = ref([])
const statistics = ref(null)
const scoreChartRef = ref(null)

onMounted(() => {
  fetchExams()
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
    const response = await api.post(`/statistics/generate/${examId.value}`)
    if (response.code === 200) {
      statistics.value = response.data
      initScoreChart()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('生成统计失败')
  }
}

const initScoreChart = () => {
  if (!scoreChartRef.value) return
  
  const chart = echarts.init(scoreChartRef.value)
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
        data: [10, 15, 20, 12, 8],
        type: 'bar',
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  chart.setOption(option)
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
  margin-top: 30px;
}

.chart-card {
  height: 400px;
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
