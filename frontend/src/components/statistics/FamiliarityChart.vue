<template>
  <el-card shadow="never" class="familiarity-card" v-loading="loading">
    <template #header>
      <div class="section-header">
        <div>
          <div>熟练度分布</div>
          <div class="section-description">按当前词卡熟练度查看掌握情况</div>
        </div>
        <span class="section-description">共 {{ total }} 张</span>
      </div>
    </template>

    <el-alert
      v-if="errorText"
      :title="errorText"
      type="warning"
      :closable="false"
      show-icon
      class="module-alert"
    />

    <el-empty v-else-if="!loading && items.length === 0" description="熟练度分布暂不可用">
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
    </el-empty>

    <div v-else ref="chartRef" class="chart-container" />
  </el-card>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const FAMILIARITY_HINT_MAP = {
  0: '刚开始学习',
  1: '需要反复巩固',
  2: '已有初步印象',
  3: '进入稳定记忆',
  4: '掌握较稳定',
  5: '接近完全掌握',
}

const FAMILIARITY_COLORS = ['#94a3b8', '#f59e0b', '#3b82f6', '#22c55e', '#8b5cf6', '#ef4444']

const props = defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorText: { type: String, default: '' },
})

defineEmits(['retry'])

const chartRef = ref(null)
let echarts = null
let chart = null

const total = computed(() => props.items.reduce((sum, item) => sum + item.count, 0))

const loadECharts = async () => {
  if (!echarts) { echarts = await import('echarts') }
  return echarts
}

const renderChart = async () => {
  if (!chartRef.value || props.items.length === 0) return
  const echartsLib = await loadECharts()
  if (!chart) { chart = echartsLib.init(chartRef.value) }

  const displayItems = props.items.map((item, idx) => ({
    label: `Lv.${item.level}`,
    hint: FAMILIARITY_HINT_MAP[item.level] || '',
    count: item.count,
    color: FAMILIARITY_COLORS[idx] || '#94a3b8',
  }))

  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter(p) {
        return `${p.name}<br/>${p.marker} ${p.value} 张 (${p.percent}%)`
      },
    },
    legend: {
      orient: 'vertical',
      right: 16,
      top: 'center',
      formatter(name) {
        const item = displayItems.find((i) => i.label === name)
        return item ? `${name} ${item.hint}` : name
      },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
      },
      data: displayItems.map((item) => ({
        name: item.label,
        value: item.count,
        itemStyle: { color: item.color },
      })),
    }],
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
.familiarity-card {
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

.section-description {
  font-size: 13px;
  font-weight: 400;
  color: var(--ws-text-muted);
}

.module-alert {
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
