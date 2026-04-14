<template>
  <el-card shadow="never" class="trend-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <div>
          <div>学习趋势</div>
          <div class="section-description">{{ description }}</div>
        </div>
        <div class="range-date">{{ dateRange }}</div>
      </div>
    </template>

    <el-empty v-if="!loading && items.length === 0" description="当前范围暂无趋势数据">
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
    </el-empty>

    <div v-else ref="chartRef" class="chart-container" />
  </el-card>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  description: { type: String, default: '' },
  dateRange: { type: String, default: '--' },
})

defineEmits(['retry'])

const chartRef = ref(null)
let echarts = null
let chart = null

const loadECharts = async () => {
  if (!echarts) { echarts = await import('echarts') }
  return echarts
}

const renderChart = async () => {
  if (!chartRef.value || props.items.length === 0) return
  const echartsLib = await loadECharts()
  if (!chart) { chart = echartsLib.init(chartRef.value) }

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter(params) {
        const date = params[0]?.axisValueLabel || ''
        let html = `<strong>${date}</strong><br/>`
        params.forEach((p) => { html += `${p.marker} ${p.seriesName}: ${p.value}<br/>` })
        return html
      },
    },
    legend: { data: ['学习数', '答对数'], bottom: 0 },
    grid: { left: 48, right: 48, top: 16, bottom: 40 },
    xAxis: {
      type: 'category',
      data: props.items.map((i) => i.label),
      axisLabel: { fontSize: 11, color: 'var(--ws-text-muted)' },
      axisLine: { lineStyle: { color: 'rgba(43, 107, 230, 0.08)' } },
      axisTick: { show: false },
    },
    yAxis: [{
      type: 'value',
      name: '题数',
      minInterval: 1,
      axisLabel: { fontSize: 11, color: 'var(--ws-text-muted)' },
      splitLine: { lineStyle: { color: 'rgba(43, 107, 230, 0.06)' } },
    }],
    series: [
      {
        name: '学习数',
        type: 'bar',
        data: props.items.map((i) => i.studyCount),
        barMaxWidth: 24,
        itemStyle: { color: 'var(--ws-primary)', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '答对数',
        type: 'line',
        data: props.items.map((i) => i.correctCount),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#22c55e', width: 2 },
        itemStyle: { color: '#22c55e' },
      },
    ],
  }, true)
}

const handleResize = () => chart?.resize()

watch(() => props.items, () => renderChart(), { deep: true })

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.trend-card {
  min-height: 420px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  font-weight: 600;
}

.section-description,
.range-date {
  font-size: 13px;
  font-weight: 400;
  color: var(--ws-text-muted);
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
